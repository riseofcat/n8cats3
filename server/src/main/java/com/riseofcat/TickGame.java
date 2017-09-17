package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.Params;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.Tick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TickGame {
private final long startTime = System.currentTimeMillis();
private int previousActionsVersion = 0;
volatile private int tick = 0;//todo volatile redundant? //todo float
private Logic.State state = new Logic.State();
private DefaultValueMap<Tick, List<Action>> actions = new DefaultValueMap<>(new ConcurrentHashMap<>(), ArrayList::new);
private Map<Logic.Player.Id, Integer> mapPlayerVersion = new ConcurrentHashMap<>();
public TickGame(ConcreteRoomsServer.Room room) {
	room.onPlayerAdded.add(player -> {
		synchronized(TickGame.this) {
			int d = 1;
			actions.getExistsOrPutDefault(new Tick(tick + d)).add(new Action(++previousActionsVersion, new Logic.NewCarAction(new Logic.XY(Math.random() * Logic.width, Math.random() * Logic.height), player.getId()).toBig()));
			ServerPayload payload = createStablePayload();
			payload.welcome = new ServerPayload.Welcome();
			payload.welcome.id = player.getId();
			payload.actions = new ArrayList<>();
			for(Map.Entry<Tick, List<Action>> entry : actions.map.entrySet()) {
				ArrayList<Logic.BigAction> temp = new ArrayList<>();
				for(Action a : entry.getValue()) temp.add(a.pa);
				payload.actions.add(new ServerPayload.TickActions(entry.getKey().tick, temp));
			}
			player.session.send(payload);
			mapPlayerVersion.put(player.getId(), previousActionsVersion);
		}
		for(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p : room.getPlayers()) if(!p.equals(player)) updatePlayer(p);//Говорим другим, что пришёл новый игрок
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
							message.player.session.send(payload);//todo move out of for
							continue;
						} else delay = getStableTick().tick - a.tick;
					} else if(a.tick > getFutureTick()) {
						payload.canceled = new HashSet<>();
						payload.canceled.add(a.aid);
						message.player.session.send(payload);//todo move out of for
						continue;
					}
					payload.apply = new ArrayList<>();
					payload.apply.add(new ServerPayload.AppliedActions(a.aid, delay));
					message.player.session.send(payload);//todo move out of for
					actions.getExistsOrPutDefault(new Tick(a.tick + delay)).add(new Action(++previousActionsVersion, new Logic.PlayerAction(message.player.getId(), a.action).toBig()));
				}
			}
		}
		for(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p : room.getPlayers()) if(!p.equals(message.player)) updatePlayer(p);
	});
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			class Adapter implements Iterator<Logic.InStateAction> {
				private Iterator<Action> iterator;
				public Adapter(List<Action> arr) {
					if(arr != null) iterator = arr.iterator();
				}
				public boolean hasNext() {
					return iterator != null && iterator.hasNext();
				}
				public Logic.InStateAction next() {
					return iterator.next().pa;
				}
			}
			while(System.currentTimeMillis() - startTime > tick * Logic.UPDATE_MS) {
				synchronized(TickGame.this) {
					state.act(new Adapter(actions.map.get(getStableTick()))).tick();
					TickGame.this.actions.map.remove(getStableTick());
					if(++tick % 200 == 0) /*todo %*/ for(ConcreteRoomsServer.Room.Player player : room.getPlayers()) player.session.send(createStablePayload());
				}
			}
		}
	}, 0, Logic.UPDATE_MS / 2);
}
private void updatePlayer(RoomsDecorator<ClientPayload, ServerPayload>.Room.Player p) {
	ServerPayload payload = new ServerPayload();
	payload.actions = new ArrayList<>();
	synchronized(this) {
		payload.tick = tick;
		for(Map.Entry<Tick, List<Action>> entry : actions.map.entrySet()) {
			ArrayList<Logic.BigAction> temp = new ArrayList<>();
			for(Action a : entry.getValue()) if(a.actionVersion > mapPlayerVersion.get(p.getId())) temp.add(a.pa);
			if(temp.size() > 0) payload.actions.add(new ServerPayload.TickActions(entry.getKey().tick, temp));
		}
		mapPlayerVersion.put(p.getId(), previousActionsVersion);
		p.session.send(payload);
	}
}
ServerPayload createStablePayload() {
	ServerPayload result = new ServerPayload();
	result.tick = tick;
	result.stable = new ServerPayload.Stable();
	result.stable.tick = getStableTick().tick;
	result.stable.state = state;
	return result;
}
private Tick getStableTick() {
	int result = tick - Params.DELAY_TICKS + 1;
	if(result < 0) return new Tick(0);
	return new Tick(result);
}
private int getRemoveBeforeTick() {
	return tick - Params.REMOVE_TICKS + 1;
}
private int getFutureTick() {
	return tick + Params.FUTURE_TICKS;
}
private static class ConcreteRoomsServer extends RoomsDecorator<ClientPayload, ServerPayload> {

}
private class Action {
	public int actionVersion;
	public Logic.BigAction pa;
	public Action(int actionVersion, Logic.BigAction pa) {
		this.actionVersion = actionVersion;
		this.pa = pa;
	}
}
private void todo() {
	ConcreteRoomsServer.Room.Player player = null;
	long startTime = player.session.get(UsageMonitorDecorator.Extra.class).getStartTime();
	Integer latency = player.session.get(PingDecorator.Extra.class).getLatency();
}
}
