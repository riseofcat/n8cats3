package com.n8cats.share;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Logic {
public static final int UPDATE_MS = 20;
public static float width = 1000;
public static float height = 1000;
public void update(State state, Map<Player.Id, Action> actions) {
	for(Car car : state.cars) {
		Action action = actions.get(car.playerId);
		if(action != null) {
			float secondsToMove = 2.0f;
			car.speedX = (action.touchX - car.x) / secondsToMove;
			car.speedY = (action.touchY - car.y) / secondsToMove;
		}
	}
	for(Car car : state.cars) {
		if(car.destroyedBy == null) {
			car.x += car.speedX * UPDATE_MS / 1000;
			car.y += car.speedY * UPDATE_MS / 1000;
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

	public static class Id extends IntHashStrEquals {
		public Id() {
		}
		public Id(int id) {
			this.id = id;
		}
		public int id;
		protected int getInt() {
			return id;
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
}

public static class Action {
	public float touchX;
	public float touchY;
}

public static class State {
	public Set<Car> cars = new HashSet<>();
}

}
