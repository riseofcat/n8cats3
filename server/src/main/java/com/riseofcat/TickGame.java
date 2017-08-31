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
private DefaultValueMap<Tick, ArrayList<Action>> actions = new DefaultValueMap<>(new ConcurrentHashMap<>(), ArrayList::new);
private Map<Logic.Player.Id, Integer> mapPlayerVersion = new ConcurrentHashMap<>();
public TickGame(ConcreteRoomsServer.Room room) {
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
				ArrayList<Logic.PlayerAction> temp = new ArrayList<>();
				for(Action a : actions.map.get(k)) {
					temp.add(a.pa);
				}
				payload.actions.add(new ServerPayload.TickActions(k.tick, temp));
			}
			player.session.send(payload);
			mapPlayerVersion.put(player.getId(), previousActionsVersion);
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
					if(a.tick < getStableTick().tick) {
						if(a.tick < getRemoveBeforeTick()) {
							payload.canceled = new HashSet<>();
							payload.canceled.add(a.aid);
							message.player.session.send(payload);
							continue;
						} else {
							delay = getStableTick().tick - a.tick;
						}
					}
					payload.apply = new ArrayList<>();
					payload.apply.add(new ServerPayload.AppliedActions(a.aid, delay));
					message.player.session.send(payload);
					actions.getExistsOrPutDefault(new Tick(a.tick + delay)).add(new Action(++previousActionsVersion, new Logic.PlayerAction(message.player.getId(), a.action)));
				}
				for(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p : room.getPlayers()) {
					if(p.getId().equals(message.player.getId())) {
						continue;
					}
					ServerPayload payload2 = new ServerPayload();
					payload2.tick = tick;
					payload2.actions = new ArrayList<>();
					for(Tick k : actions.map.keySet()) {//todo duplicate
						ArrayList<Logic.PlayerAction> temp = new ArrayList<>();
						for(Action pa : actions.map.get(k)) {
							if(pa.actionVersion > mapPlayerVersion.get(message.player.getId())) {
								temp.add(pa.pa);
							}
						}
						if(temp.size() > 0) {
							payload2.actions.add(new ServerPayload.TickActions(k.tick, temp));
						}
					}
					mapPlayerVersion.put(message.player.getId(), previousActionsVersion);
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
				ArrayList<Logic.PlayerAction> list = new ArrayList<>();
				for(Action a : actions.getOrNew(getStableTick(), ArrayList::new)) {
					list.add(a.pa);
				}
				state.update(list);
				TickGame.this.actions.map.remove(getStableTick());
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

private class Action {
	public int actionVersion;
	public Logic.PlayerAction pa;
	public Action(int actionVersion, Logic.PlayerAction pa) {
		this.actionVersion = actionVersion;
		this.pa = pa;
	}
}
}
