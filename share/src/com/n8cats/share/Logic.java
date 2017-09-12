package com.n8cats.share;

import java.util.ArrayList;
import java.util.Iterator;

public class Logic {
public static final int UPDATE_MS = 20;
public static final float UPDATE_S = UPDATE_MS * 0.001f;
public static float width = 1000;
public static float height = 1000;
public static final int MIN_SIZE = 10;
public static final int FOOD_SIZE = 5;
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
	public float x = 0;
	public float y = 0;
}

public static abstract class SpeedObject extends PosObject {
	public float speedX = 0;
	public float speedY = 0;
}

public static abstract class EatMe extends SpeedObject {
	public int size;
	public float radius() {
		return (float) (Math.sqrt(size) * 1f) + MIN_RADIUS;
	}
}

public static class Food extends EatMe {
	public Food() {
		size = FOOD_SIZE;
	}
}

public static class Reactive extends EatMe {
	public Reactive() {
	}
	public Reactive(Car car) {
		size = car.size / 10 + 1;
		car.size -= size;
	}
}

public static class Car extends EatMe {
	public Car() {
		size = MIN_SIZE;
	}
	public Player.Id playerId;
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
	public State act(Iterator<? extends PlayerAction> iterator) {
		class Cache {
			public Car getCar(Logic.Player.Id id) {
				for(Car car : cars) {
					if(id.equals(car.playerId)) {
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
			float koeff1 = 100f;
			car.speedY += p.action.direction.sin() * koeff1;
			car.speedX += p.action.direction.cos() * koeff1;
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
		for(Car car : cars) {
			car.x += car.speedX * UPDATE_S;
			car.y += car.speedY * UPDATE_S;
			if(car.x > width) {
				car.x -= width;
			} else if(car.x < 0) {
				car.x += width;
			}
			if(car.y > height) {
				car.y -= height;
			} else if(car.y < 0) {
				car.y += height;
			}
		}
		return this;
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
}
