package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.Tick;
import com.n8cats.share.redundant.ServerSayS;
import com.riseofcat.lib.XY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Model {
private final PingClient<ServerPayload, ClientPayload> client;
private Logic.Player.Id playerId;
private float clientTick;//Плавно меняется, подстраиваясь под сервер
private float serverTick;//Задаётся моментально с сервера
//todo bad not concurrent hash maps and array lists. Look exception in todo.
private final DefaultValueMap<Tick, List<Logic.PlayerAction>> actions = new DefaultValueMap<>(new HashMap<Tick, List<Logic.PlayerAction>>(), new DefaultValueMap.ICreateNew<List<Logic.PlayerAction>>() {
	public List<Logic.PlayerAction> createNew() {
		return new ArrayList<>();
	}
});
private final DefaultValueMap<Tick, List<Action>> myActions = new DefaultValueMap<>(new HashMap<Tick, List<Action>>(), new DefaultValueMap.ICreateNew<List<Action>>() {
	public List<Action> createNew() {
		return new ArrayList<>();
	}
});
private Logic.State state;
private int stateTick;
private int stableTick;
private int previousActionId = 0;
public static final int DEFAULT_LATENCY_MS = 50;
public static final boolean LOCAL = LibAllGwt.FALSE();
public Model() {
	client = LOCAL ? new PingClient("localhost", 5000, "socket", ServerSayS.class) : new PingClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
	client.connect(new Signal.Listener<ServerPayload>() {
		public void onSignal(ServerPayload s) {
			if(s.welcome != null) {
				playerId = s.welcome.id;
			}
			if(s.stable != null) {
				stableTick = s.stable.tick;
				if(s.stable.state != null) {
					state = s.stable.state;
					stateTick = s.stable.tick;
				}
			}
			if(s.actions != null && s.actions.size() > 0) {
				for(ServerPayload.TickActions t : s.actions) {
					actions.getExistsOrPutDefault(new Tick(t.tick)).addAll(t.list);
				}
			}
			for(Tick t : myActions.map.keySet()) {
				Iterator<Action> iterator = myActions.map.get(t).iterator();
				whl:
				while(iterator.hasNext()) {
					Action next = iterator.next();
					if(s.canceled != null) {
						if(s.canceled.contains(next.aid)) {
							iterator.remove();
							continue;
						}
					}
					if(s.apply != null) {
						for(ServerPayload.AppliedActions apply : s.apply) {
							if(apply.aid == next.aid) {
								actions.getExistsOrPutDefault(t.add(apply.delay)).add(new Logic.PlayerAction(playerId, next.action));
								iterator.remove();
								continue whl;
							}
						}
					}
				}
			}
			serverTick = s.tick + getLatencySeconds() / Logic.UPDATE_S;
			clientTick = serverTick;//todo плавно

		}
	});
}
public boolean ready() {
	return playerId != null;
}
public float getLatencySeconds() {
	return (client.latency == null ? DEFAULT_LATENCY_MS : client.latency) / 1000f;
}
public void touch(XY pos) {
	if(!ready()) return;
	int w = (int) (getLatencySeconds() / Logic.UPDATE_S) + 1;//todo Учитывать среднюю задержку
	ClientPayload.ClientAction a = new ClientPayload.ClientAction();
	a.aid = ++previousActionId;
	a.wait = w;
	a.tick = (int) clientTick + w;
	a.action = new Logic.Action(pos.x, pos.y);
	myActions.getExistsOrPutDefault(new Tick((int) clientTick + w)).add(new Action(a.aid, a.action));
	ClientPayload payload = new ClientPayload();
	payload.tick = (int) clientTick;
	payload.actions = new ArrayList<>();
	payload.actions.add(a);
	client.say(payload);
}
public void update(float deltaTime) {
	serverTick += deltaTime / Logic.UPDATE_S;
	clientTick += deltaTime / Logic.UPDATE_S;
}
public Logic.State getDisplayState() {
	if(!ready()) return new Logic.State();
	return getState((int) clientTick);//todo плавно
}
private Logic.State getState(int tick) {
	class Adapter implements Iterator<Logic.PlayerAction> {
		private Iterator<Action> iterator;
		public Adapter(List<Action> arr) {
			if(arr != null) {
				iterator = arr.iterator();
			}
		}
		public boolean hasNext() {
			return iterator != null && iterator.hasNext();
		}
		public Logic.PlayerAction next() {
			return new Logic.PlayerAction(playerId, iterator.next().action);//todo new???
		}
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}
	if(tick == stateTick) return state.copy();
	return getState(tick - 1)
			.act(actions.getOrNew(new Tick(tick - 1), new DefaultValueMap.ICreateNew<List<Logic.PlayerAction>>() {
				public List<Logic.PlayerAction> createNew() {
					return new ArrayList<>();
				}
			}).iterator())
			.act(new Adapter(myActions.map.get(new Tick(tick - 1))))
			.tick();
}
public void dispose() {
	client.close();
}
private class Action {
	public final int aid;
	public final Logic.Action action;
	public Action(int aid, Logic.Action action) {
		this.aid = aid;
		this.action = action;
	}
}
}
