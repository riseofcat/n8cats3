package com.n8cats.share;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Logic {
public static final int UPDATE_MS = 20;
public static final float UPDATE_S = UPDATE_MS * 0.001f;
public static float width = 1000;
public static float height = 1000;
public void update(State state, @Nullable List<ServerPayload.PlayerAction> actions) {
	class Cache {
		public Car getCar(Logic.Player.Id id) {
			for(Car car : state.cars) {
				if(id.equals(car.playerId)) {
					return car;
				}
			}
			return null;
		}
	}
	Cache cache = new Cache();
	if(actions != null) {
		for(ServerPayload.PlayerAction p : actions) {
			Car car = cache.getCar(p.id);
			if(car == null) {
				continue;
			}
			float secondsToMove = 2.0f;
			car.speedX = (p.action.touchX - car.x) / secondsToMove;
			car.speedY = (p.action.touchY - car.y) / secondsToMove;
		}
	}
	for(Car car : state.cars) {
		if(car.destroyedBy == null) {
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
			if(false) {
				car.destroyedBy = car.playerId;
			}
		}
	}
}
abstract public static class Player {
	abstract public Id getId();

	public static class Id {
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
			return o != null && ( o == this || /*o instanceof Id &&*/ o.getClass() == Id.class && ((Id) o).id == id );
		}
		public String toString() {
			return String.valueOf(id);
		}
	}
}

public static class Car {
	public Player.Id playerId;
	public float x = 0;
	public float y = 0;
	public float speedX = 0;
	public float speedY = 0;
	public Player.Id destroyedBy;
	public Car copy() {//todo simplify
		Car result = new Car();
		result.playerId = this.playerId;
		result.x = this.x;
		result.y = this.y;
		result.speedX = this.speedX;
		result.speedY = this.speedY;
		result.destroyedBy = this.destroyedBy;
		return result;
	}
}

public static class Action {
	public Action() {
	}
	public Action(float touchX, float touchY) {
		this.touchX = touchX;
		this.touchY = touchY;
	}
	public float touchX;
	public float touchY;
}

public static class State {
	public ArrayList<Car> cars = new ArrayList<>();
	public State copy() {//todo simplify
		State result = new State();
		result.cars = new ArrayList<>();
		for(Car c : this.cars) {
			result.cars.add(c.copy());
		}
		return result;
	}
}

public static class Tick {
	//do not use in JSON
	//todo move out from here
	public final int tick;
	public Tick(int tick) {
		this.tick = tick;
	}
	public Tick add(int t) {
		return new Tick(tick + t);
	}
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		Tick tick1 = (Tick) o;
		return tick == tick1.tick;
	}
	public int hashCode() {
		return tick;
	}
}

}
