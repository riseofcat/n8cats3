package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.Tick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TickGame {
public static final int DELAY_TICKS = 10;//количество тиков для хранения действий //bigger delayed
public static final int REMOVE_TICKS = 20;//bigger removed

private int previousActionsVersion = 0;
private int tick = 0;
private Logic.State state = new Logic.State();
DefaultValueMap<Tick, ArrayList<ServerPayload.PlayerAction>> actions = new DefaultValueMap<>(new ConcurrentHashMap<>(), ArrayList::new);
private Map<Logic.Player.Id, Integer> mapPlayerActionVersion = new ConcurrentHashMap<>();

public TickGame(ConcreteRoomsServer.Room room, Logic logic) {
	room.onPlayerAdded.add(player -> {
		synchronized(TickGame.this) {
			Logic.Car car = new Logic.Car();
			car.playerId = player.getId();
			car.x = (float) (Math.random() * Logic.width);
			car.y = (float) (Math.random() * Logic.height);
			state.cars.add(car);
			ServerPayload payload = createStablePayload();
			payload.welcome = new ServerPayload.Welcome();
			payload.welcome.id = player.getId();
			payload.actions = new ArrayList<>();
			for(Tick k : actions.map.keySet()) {//todo duplicate
				ServerPayload.TickActions ta = new ServerPayload.TickActions();
				ta.tick = k.tick;
				ta.list = actions.map.get(k);
				payload.actions.add(ta);
			}
			player.session.send(payload);
			mapPlayerActionVersion.put(player.getId(), previousActionsVersion);
			//Говорим другим, что пришёл новый игрок
			for(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p : room.getPlayers()) {
				if(!p.equals(player)) {
					p.session.send(createStablePayload());
				}
			}
		}
	});
	room.onMessage.add(message -> {
		synchronized(TickGame.this) {
			if(message.payload.actions != null) {
				for(ClientPayload.ClientAction a : message.payload.actions) {
					ServerPayload payload = new ServerPayload();
					payload.tick = tick;
					int delay = 0;
					if(message.payload.tick + a.wait < getStableTick().tick) {
						if(message.payload.tick + a.wait < getRemoveBeforeTick()) {
							payload.canceled = new HashSet<>();
							payload.canceled.add(a.aid);
							message.player.session.send(payload);
							continue;
						} else {
							delay = getStableTick().tick - (message.payload.tick + a.wait);//todo сложная логика
						}
					}
					payload.apply = new ArrayList<>();
					ServerPayload.ApplyedActions ap = new ServerPayload.ApplyedActions();
					ap.aid = a.aid;
					a.wait += delay;
					ap.delay = delay;
					payload.apply.add(ap);
					message.player.session.send(payload);
					ServerPayload.PlayerAction pa = new ServerPayload.PlayerAction();
					pa.action = a.action;
					pa.id  = message.player.getId();
					pa.actionVersion = ++previousActionsVersion;
					actions.getExistsOrPutDefault(new Tick(message.payload.tick + a.wait)).add(pa);
				}
				for(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p : room.getPlayers()) {
					if(p.getId().equals(message.player.getId())) {
						continue;
					}
					ServerPayload payload2 = new ServerPayload();
					payload2.tick = tick;
					payload2.actions = new ArrayList<>();
					for(Tick k : actions.map.keySet()) {//todo duplicate
						ServerPayload.TickActions ta = null;
						for(ServerPayload.PlayerAction pa : actions.map.get(k)) {
							if(pa.actionVersion <= mapPlayerActionVersion.get(message.player.getId())) {
								continue;
							}
							if(ta == null) {
								ta = new ServerPayload.TickActions();
								ta.tick = k.tick;
								ta.list = new ArrayList<>();
							}
							ta.list.add(pa);
						}
						if(ta != null) {
							payload2.actions.add(ta);
						}
					}
					mapPlayerActionVersion.put(message.player.getId(), previousActionsVersion);
					p.session.send(payload2);
				}
			}
		}
	});
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			synchronized(TickGame.this) {
				tick++;
				logic.update(state, actions.map.get(getStableTick()));
				actions.map.remove(getStableTick());
				if(tick % 100 == 0) { //Разослать state всем игрокам
					for(ConcreteRoomsServer.Room.Player player : room.getPlayers()) {
						player.session.send(createStablePayload());
					}
				}
			}
		}
	}, 0, Logic.UPDATE_MS);
}

ServerPayload createStablePayload() {
	ServerPayload result = new ServerPayload();
	result.tick = tick;
	result.stable = new ServerPayload.Stable();
	result.stable.tick = getStableTick().tick;
	result.stable.state = state;
	return result;
}

public Tick getStableTick() {
	int result = tick - DELAY_TICKS + 1;
	if(result < 0) {
		return new Tick(0);
	}
	return new Tick(result);
}

public int getRemoveBeforeTick() {
	return tick - REMOVE_TICKS + 1;
}

public static class Reconciliation {

}

private static class ConcreteRoomsServer extends RoomsDecorator<ClientPayload, ServerPayload> {

}

private void todo() {
	ConcreteRoomsServer.Room.Player player = null;
	long startTime = player.session.get(UsageMonitorDecorator.Extra.class).getStartTime();
	Integer latency = player.session.get(PingDecorator.Extra.class).getLatency();
}

}
