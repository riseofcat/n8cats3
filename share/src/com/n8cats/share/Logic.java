package com.n8cats.share;

import com.n8cats.lib_gwt.LibAllGwt;

import java.util.ArrayList;
import java.util.Iterator;

public class Logic {
public static final int UPDATE_MS = 40;
public static final float UPDATE_S = UPDATE_MS * 0.001f;
public static float width = 1000;
public static float height = 1000;
public static final int MIN_SIZE = 15;
public static final int FOOD_SIZE = 7;
private static final float MIN_RADIUS = 1f;

abstract public static class Player {
	abstract public Id getId();
	public static class Id /*implements Serializable*/ {
		@SuppressWarnings("unused")
		public Id() {
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
		size = MIN_SIZE * 2;
	}
	public Player.Id owner;
	/*public Car clone() {
		try {
			return (Car) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}*/
}

public static class Action {
	@SuppressWarnings("unused")
	public Action() {
	}
	public Action(Angle direction) {
		this.direction = direction;
	}
	public Angle direction;
}

public static class PlayerAction {
	public Logic.Player.Id id;
	public Logic.Action action;
	@SuppressWarnings("unused")
	public PlayerAction() {
	}
	public PlayerAction(Player.Id id, Action action) {
		this.id = id;
		this.action = action;
	}
}

public static class State /*implements Serializable, LibAllGwt.Cloneable<State>*/ {
	public ArrayList<Car> cars = new ArrayList<>();
	public ArrayList<Food> foods = new ArrayList<>();
	public ArrayList<Reactive> reactive = new ArrayList<>();
	public int random;
	public State act(Iterator<? extends PlayerAction> iterator) {
		class Cache {
			public Car getCar(Logic.Player.Id id) {
				for(Car car : cars) {
					if(id.equals(car.owner)) {
						return car;
					}
				}
				return null;
			}
		}
		Cache cache = new Cache();
		while(iterator.hasNext()) {
			PlayerAction p = iterator.next();
			Car car = cache.getCar(p.id);
			if(car == null) continue;
			float scl = 100f;
			car.speed = car.speed.add(p.action.direction.xy().scale(scl));
			int s = car.size / 15 + 1;
			if(car.size - s >= MIN_SIZE) {
				car.size -= s;
			}
			reactive.add(new Reactive(p.id, s, new XY(car.pos), new XY(p.action.direction.add(new DegreesAngle(180)).xy().scale(3f * scl))));
		}
		return this;
	}
	/*public State clone() {
		State result = new State();
		result.cars = new ArrayList<>();
		for(Car car : cars) {
			result.cars.add(LibAllGwt.clone(car));
		}
		return result;
	}*/
	public State tick() {
		CompositeIterator<SpeedObject> iterator = new CompositeIterator<SpeedObject>(cars, reactive);
		while(iterator.hasNext()) {
			SpeedObject o = iterator.next();
			o.pos = o.pos.add(o.speed.scale(UPDATE_S));
			if(o.pos.x > width) {
				o.pos.x -= width;
			} else if(o.pos.x < 0) {
				o.pos.x += width;
			}
			if(o.pos.y > height) {
				o.pos.y -= height;
			} else if(o.pos.y < 0) {
				o.pos.y += height;
			}
			o.speed = o.speed.scale(0.98f);
		}
		Iterator<Reactive> reactItr = reactive.iterator();
		while(reactItr.hasNext()) {
			if(reactItr.next().ticks++ > 60) {
				reactItr.remove();
			}
		}
		for(Car car : cars) {
			Iterator<Food> foodItr = foods.iterator();
			while(foodItr.hasNext()) {
				Food f = foodItr.next();
				if(car.pos.sub(f.pos).len() <= car.radius()) {
					car.size += f.size;
					foodItr.remove();
				}
			}
		}
		if(foods.size() < 100) {
			foods.add(new Food(rndPos()));
		}
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
		return min + rnd(999)/1000f * (max - min);//todo optimize
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
		Angle pi = new Angle(2 * Math.PI);
//	if(Math.abs(pi.radians) > 0.0001f) {
//		throw new RuntimeException("test fail");
//	}
//	Angle minusPi = new Angle(-2.00001 * Math.PI);
//	if(Math.abs(minusPi.radians) > 0.01f) {
//		throw new RuntimeException("test fail");
//	}
//	Angle angle1 = new Angle(2 * Math.PI + 0.5f);
//	if(angle1.radians < 0 || angle1.radians > 2 * Math.PI) {
//		throw new RuntimeException("test fail");
//	}
//	Angle angle2 = new Angle(-2 * Math.PI - 0.5f);
//	if(angle2.radians < 0 || angle2.radians > 2 * Math.PI) {
//		throw new RuntimeException("test fail");
//	}
	}
	private float radians;
	public Angle() {
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
			int a = 1 + 1;//todo
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
		throw new RuntimeException("bad");
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
public static class XY{
	public float x;
	public float y;
	public XY() {
		x = 0;
		y = 0;
	}
	public XY(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}
	public XY(XY pos) {//todo Вынести копирование в context
		this(pos.x, pos.y);
	}
	public XY add(XY a) {
		XY result = new XY();
		result.x = this.x + a.x;
		result.y = this.y + a.y;
		return result;
	}
	public XY sub(XY a) {
		return add(a.scale(-1));
	}
	public XY scale(float scl) {
		XY result = new XY();
		result.x = this.x * scl;
		result.y = this.y * scl;
		return result;
	}
	public double dst(XY xy) {
		return Math.sqrt((xy.x - x) * (xy.x - x) + (xy.y - y) * (xy.y - y));
	}
	public double len() {
		return dst(new XY(0,0));
	}
	public XY rotate(Logic.Angle angleA) {
		Logic.Angle angle = calcAngle().add(angleA);
		return new XY(len()*angle.cos(), len() * angle.sin());
	}
	public Logic.Angle calcAngle() {
		if(true) {
			return new Angle(Math.atan2(y, x));
		} else {
			try {
				Logic.Angle result = new Logic.Angle(Math.atan(y / x));
				if(x < 0) {
					result = result.add(new Logic.DegreesAngle(180));
				}
				return result;
			} catch(Throwable t) {
				return new Logic.DegreesAngle(LibAllGwt.Fun.sign(y) * 90);
			}
		}
	}
}
}
