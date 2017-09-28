package com.n8cats.share;

import com.n8cats.lib_gwt.LibAllGwt;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

public class Logic {
public static final int UPDATE_MS = 40;
public static final float UPDATE_S = UPDATE_MS / LibAllGwt.MILLIS_IN_SECCOND;
public static final int MIN_SIZE = 15;
public static final int FOOD_SIZE = 7;
private static final float MIN_RADIUS = 1f;

abstract public static class Player {
	abstract public Id getId();
	public static class Id {
		@SuppressWarnings("unused") public Id() {
		}
		public Id(int id) {
			this.id = id;
		}
		public int id;
		public int hashCode() {
			return id;
		}
		public boolean equals(Object o) {
			return o != null && (o == this || o.getClass() == Id.class && ((Id) o).id == id);
		}
		public String toString() {
			return String.valueOf(id);
		}
	}
}
public static abstract class PosObject {
	public XY pos = new XY();
}
public static abstract class SpeedObject extends PosObject {
	public XY speed = new XY();
}
public static abstract class EatMe extends SpeedObject {
	public int size;
	public float radius() {
		return (float) (Math.sqrt(size) * 5f) + MIN_RADIUS;
	}
}
public static class Food extends EatMe {
	public Food() {
		size = FOOD_SIZE;
	}
	public Food(XY pos) {
		this();
		this.pos = pos;
	}
}
public static class Reactive extends EatMe {
	public Player.Id owner;
	public int ticks;
	@SuppressWarnings("unused")
	public Reactive() {
	}
	public Reactive(Player.Id owner, int size, XY pos, XY speed) {
		this.size = size;
		this.pos = pos;
		this.speed = speed;
		this.owner = owner;
	}
}
public static class Car extends EatMe {
	public Car() {
	}
	public Player.Id owner;
}
public static class Action {
	@SuppressWarnings("unused") public Action() {
	}
	public Action(Angle direction) {
		this.direction = direction;
	}
	public Angle direction;
}
public interface InStateAction {
	void act(State state, GetCarById getCar);
}
public interface GetCarById {
	Car getCar(Player.Id id);
}
public static class PlayerAction implements InStateAction {
	public Logic.Player.Id id;
	public Logic.Action action;
	@SuppressWarnings("unused") public PlayerAction() {
	}
	public PlayerAction(Player.Id id, Action action) {
		this.id = id;
		this.action = action;
	}
	public void act(State state, GetCarById getCar) {
		final float scl = 100f;
		Car car = getCar.getCar(id);
		if(car == null) return;//todo handle null ?
		car.speed = car.speed.add(action.direction.xy().scale(scl));
		int s = car.size / 15 + 1;
		if(car.size - s >= MIN_SIZE) car.size -= s;
		state.reactive.add(new Reactive(id, s, new XY(car.pos), new XY(action.direction.add(new DegreesAngle(180)).xy().scale(3f * scl))));
	}
	public BigAction toBig() {
		BigAction result = new BigAction();
		result.p = this;
		return result;
	}
}
public static class NewCarAction implements InStateAction {
	public Player.Id id;
	@SuppressWarnings("unused") public NewCarAction() {
	}
	public NewCarAction(Player.Id id) {
		this.id = id;
	}
	public void act(State state, GetCarById getCar) {
		Car car = new Car();
		car.pos = new XY();
		car.owner = id;
		car.size = MIN_SIZE * 4;
		state.cars.add(car);
	}
	public BigAction toBig() {
		BigAction result = new BigAction();
		result.n = this;
		return result;
	}
}
public static class BigAction implements InStateAction {//todo redundant because Json serialization
	@Nullable public NewCarAction n;
	@Nullable public PlayerAction p;
	public void act(State state, GetCarById getCar) {
		if(n != null) n.act(state, getCar);
		if(p != null) p.act(state, getCar);
	}
}
public static class State {
	public ArrayList<Car> cars = new ArrayList<>();
	public ArrayList<Food> foods = new ArrayList<>();
	public ArrayList<Reactive> reactive = new ArrayList<>();
	public float width = 3000;
	public float height = 3000;
	public int random;
	@SuppressWarnings("unused") public State() {
	}
	public State act(Iterator<? extends InStateAction> iterator) {
		class Cache implements GetCarById {
			public Car getCar(Logic.Player.Id id) {
				for(Car car : cars) if(id.equals(car.owner)) return car;
				return null;
			}
		}
		Cache cache = new Cache();
		while(iterator.hasNext()) {
			InStateAction p = iterator.next();
			p.act(this, cache);
		}
		return this;
	}
	private float distance(XY a, XY b) {
		float dx = Math.min(Math.abs(b.x - a.x), b.x + width - a.x);
		dx = Math.min(dx, a.x + width - b.x);
		float dy = Math.min(Math.abs(b.y - a.y), b.y + height - a.y);
		dy = Math.min(dy, a.y + height - b.y);
		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	public State tick() {
		CompositeIterator<SpeedObject> iterator = new CompositeIterator<SpeedObject>(cars, reactive);
		while(iterator.hasNext()) {
			SpeedObject o = iterator.next();
			o.pos = o.pos.add(o.speed.scale(UPDATE_S));
			if(o.pos.x >= width) o.pos.x -= width;
			else if(o.pos.x < 0) o.pos.x += width;
			if(o.pos.y >= height) o.pos.y -= height;
			else if(o.pos.y < 0) o.pos.y += height;
			o.speed = o.speed.scale(0.98f);
		}
		Iterator<Reactive> reactItr = reactive.iterator();
		while(reactItr.hasNext()) if(reactItr.next().ticks++ > 60) reactItr.remove();
		for(Car car : cars) {
			Iterator<Food> foodItr = foods.iterator();
			while(foodItr.hasNext()) {
				Food f = foodItr.next();
				if(distance(car.pos, f.pos) <= car.radius()) {
					car.size += f.size;
					foodItr.remove();
				}
			}
			reactItr = reactive.iterator();
			while(reactItr.hasNext()) {
				Reactive r = reactItr.next();
				if(!r.owner.equals(car.owner) && distance(car.pos, r.pos) <= car.radius()) {
					car.size += r.size;
					reactItr.remove();
				}
			}
		}
		if(foods.size() < 200) foods.add(new Food(rndPos()));
		return this;
	}
	private int rnd(int min, int max) {
		random = (random * 1664525 + 1013904223) & 0x7fffffff;
		return min + random % (max - min + 1);
	}
	private int rnd(int max) {
		return rnd(0, max);
	}
	private float rndf(float min, float max) {
		return min + rnd(999) / 1000f * (max - min);//todo optimize
	}
	private float rndf(float max) {
		return rndf(0, max);
	}
	private float rndf() {
		return rndf(1f);
	}
	private XY rndPos() {
		return new XY(rndf(width), rndf(height));
	}
}
public static class Angle {
	static {
		if(false) {//todo move to tests
			Angle pi = new Angle(2 * Math.PI);
			if(Math.abs(pi.radians) > 0.0001f) throw new RuntimeException("test fail");
			Angle minusPi = new Angle(-2.00001 * Math.PI);
			if(Math.abs(minusPi.radians) > 0.01f) throw new RuntimeException("test fail");
			Angle angle1 = new Angle(2 * Math.PI + 0.5f);
			if(angle1.radians < 0 || angle1.radians > 2 * Math.PI)
				throw new RuntimeException("test fail");
			Angle angle2 = new Angle(-2 * Math.PI - 0.5f);
			if(angle2.radians < 0 || angle2.radians > 2 * Math.PI)
				throw new RuntimeException("test fail");
		}
	}
	private float radians;
	@SuppressWarnings("unused") public Angle() {
	}
	public Angle(double radians) {
		this.radians = (float) radians;
		fix();
	}
	public Angle(float radians) {
		this.radians = radians;
		fix();
	}
	private void fix() {
		int circles = (int) (radians / (2 * Math.PI));
		if(Math.abs(circles) > 0) {
			int a = 1 + 1;//todo breakpoint
		}
//	radians -= circles * 2 * Math.PI;
//	if(radians < 0) {
//		radians += 2 * Math.PI;
//	}
	}
	public float getRadians() {
		return radians;
	}
	public float getDegrees() {
		return (float) (radians * 180 / Math.PI);
	}
	public float getTransformRotation() {
		return getDegrees();
	}
	public float sin() {
		return (float) Math.sin(radians);
	}
	public float cos() {
		return (float) Math.cos(radians);
	}
	public XY xy() {
		return new XY(cos(), sin());
	}
	public Angle add(double radians) {
		return new Angle(this.radians + radians);
	}
	public void addThis(double radians) {
		this.radians += radians;
		fix();
		throw new RuntimeException("bad");//todo
	}
	public Angle add(Angle deltaAngle) {
		return new Angle(this.radians + deltaAngle.radians);
	}
	public Angle subtract(Angle sub) {
		return new Angle(this.radians - sub.radians);
	}
}
public static class DegreesAngle extends Logic.Angle {
	public DegreesAngle(double degrees) {
		super(degrees / 180 * Math.PI);
	}
}
public static class XY {//todo immutable?
	public float x;
	public float y;
	public final boolean mutable;
	public XY(boolean mutable) {
		this.mutable = mutable;
		x = y = 0;
	}
	public XY() {
		this(false);
	}
	public XY(float x, float y, boolean mutable) {
		this(mutable);
		this.x = x;
		this.y = y;
	}
	public XY(float x, float y) {
		this(x, y, false);
	}
	public XY(double x, double y, boolean mutable) {
		this((float) x, (float) y, mutable);
	}
	public XY(double x, double y) {
		this(x, y, false);
	}
	public XY(XY pos, boolean mutable) {
		this(pos.x, pos.y, mutable);
	}
	@Deprecated
	public XY(XY pos) {
		this(pos, false);
	}
	public XY add(XY a) {
		XY result = mutable ? this : new XY(this, false);
		result.x += a.x;
		result.y += a.y;
		return result;
	}
	public XY sub(XY a) {
		XY result = mutable ? this : new XY(this, false);
		result.x -= a.x;
		result.y -= a.y;
		return result;
	}
	public XY scale(float scl) {
		XY result = mutable ? this : new XY(this, false);
		result.x *= scl;
		result.y *= scl;
		return result;
	}
	public double dst(XY xy) {
		return Math.sqrt((xy.x - x) * (xy.x - x) + (xy.y - y) * (xy.y - y));
	}
	public double len() {
		return dst(new XY(0, 0));
	}
	public XY rotate(Logic.Angle angleA) {
		XY result = mutable ? this : new XY(false);
		Logic.Angle angle = calcAngle().add(angleA);
		double len = len();
		result.x = (float) (len * angle.cos());
		result.y = (float) (len * angle.sin());
		return result;
	}
	public Logic.Angle calcAngle() {
		if(true) return new Angle(Math.atan2(y, x));
		else try {
			return new Logic.Angle(Math.atan(y / x)).add(new DegreesAngle(x < 0 ? 180 : 0));
		} catch(Throwable t) {return new Logic.DegreesAngle(LibAllGwt.Fun.sign(y) * 90);}
	}
}
}
