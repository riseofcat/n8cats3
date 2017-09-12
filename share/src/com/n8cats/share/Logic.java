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
		car.size-=size;
	}
}
public static class Car extends EatMe{
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
	public Action(float touchX, float touchY) {
		this.touchX = touchX;
		this.touchY = touchY;
	}
	public float touchX;
	public float touchY;
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
public static class State /*implements Serializable, LibAllGwt.Cloneable<State>*/{
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
			float secondsToMove = 2.0f;
			car.speedX = (p.action.touchX - car.x) / secondsToMove;
			car.speedY = (p.action.touchY - car.y) / secondsToMove;
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

}
