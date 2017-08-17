package com.riseofcat;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TickGame {
private final ConcreteRoomsServer.Room room;
private final Logic logic;
private int tick = 0;
private Logic.State state = new Logic.State();
//private ConcurrentHashMap<Logic.Player.Id, Logic.Action> actions = new ConcurrentHashMap<>();

public TickGame(ConcreteRoomsServer.Room room, Logic logic) {
	this.room = room;
	this.logic = logic;
	room.onPlayerAdded.add(player -> {
		Logic.Car car = new Logic.Car();
		car.playerId = player.getId();
		car.x = (float) (Math.random() * Logic.width);
		car.y = (float) (Math.random() * Logic.height);
		state.cars.add(car);
		ServerPayload data = new ServerPayload();
		data.welcome = new ServerPayload.Welcome();
		data.welcome.id = player.getId();
		data.state = new ServerPayload.State();
		data.state.tick = tick;
		data.state.state;// = state;
		data.actions;//Все actions
		player.session.send(data);
		//todo Разослать state другим игрокам
	});
	room.onMessage.add(message -> {
		if(message.payload.actions != null) {
			actions.put(message.player.getId(), message.payload.action);
			//todo разослать actions другим игрокам
		}
	});
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			tick++;
			logic.update(state, actions);
			if(tick % 20 == 0) { //Разослать state всем игрокам
				for(ConcreteRoomsServer.Room.Player player : room.getPlayers()) {
					ServerPayload payload = new ServerPayload();
					payload.state = new ServerPayload.State();
					payload.state.tick;
					payload.state.state;//.cars = state.cars.toArray(new Logic.Car[]{});
					player.session.send(payload);
					long startTime = player.session.get(UsageMonitorDecorator.Extra.class).getStartTime();
					Integer latency = player.session.get(PingDecorator.Extra.class).getLatency();
				}
			}
		}
	}, 0, Logic.UPDATE_MS);
}

public static class Reconciliation {

}

private static class ConcreteRoomsServer extends RoomsDecorator<ClientPayload, ServerPayload> {

}

}
