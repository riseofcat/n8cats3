package com.riseofcat;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TickGame {
private final RoomsServer.Room room;
private final Logic logic;
public final int reconcilationTicks = 10;
public final int oldTicks = 20;
private int tick = 0;
private Logic.State state = new Logic.State();
private ConcurrentHashMap<Logic.Player.Id, Logic.Action> actions = new ConcurrentHashMap<>();

public TickGame(ConcreteRoomsServer.Room room, Logic logic) {
	this.room = room;
	this.logic = logic;
	room.onPlayerAdded.add(player -> {
		Logic.Car car = new Logic.Car();
		car.playerId = player.getId();
		car.x = (float) (Math.random() * Logic.width);
		car.y = (float) (Math.random() * Logic.height);
		state.cars.add(car);
	});
	room.onMessage.add(message -> {
		if(message.payload.action != null) {
			actions.put(message.player.getId(), message.payload.action);
		}
	});
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			tick++;
			logic.update(state, actions);
			actions = new ConcurrentHashMap<>();
			if(tick % 5 == 0) {
				for(ConcreteRoomsServer.Room.Player player : room.getPlayers()) {
					ServerPayload payload = new ServerPayload();
					payload.state = new ServerPayload.State();
					payload.state.cars = state.cars.toArray(new Logic.Car[]{});
					player.session.send(payload);
					player.session.getExtra().getLatency();
					player.session.getExtra().getExtra().getIncomeCalls();
					long startTime = player.session.get(CountServ.Extra2.class).getStartTime();
					Integer latency = player.session.get(PingServ.Extra2.class).getLatency();
					App.breakpoint();
				}
			}
		}
	}, 0, Logic.UPDATE_MS);
}

public static class Reconciliation {

}
}
