package com.n8cats.share;

import com.n8cats.lib_gwt.LibAllGwt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Logic {
public static final int UPDATE_MS = 20;
public static final float UPDATE_S = UPDATE_MS * 0.001f;
public static float width = 1000;
public static float height = 1000;
abstract public static class Player {
	abstract public Id getId();
	public static class Id implements Serializable {
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
public static class Car implements LibAllGwt.Cloneable<Car>{
	public Player.Id playerId;
	public float x = 0;
	public float y = 0;
	public float speedX = 0;
	public float speedY = 0;
	public Car clone() {
		try {
			return (Car) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
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
public static class State implements Serializable, LibAllGwt.Cloneable<State>{
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
	public State clone() {
		State result = new State();
		result.cars = new ArrayList<>();
		for(Car car : cars) {
			result.cars.add(LibAllGwt.clone(car));
		}
		return result;
	}
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
