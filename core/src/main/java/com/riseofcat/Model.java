package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.Params;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.Tick;
import com.n8cats.share.redundant.ServerSayS;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Model {
private final PingClient<ServerPayload, ClientPayload> client;
private Logic.Player.Id playerId;
private float serverTick;//Задаётся моментально с сервера
private float clientTick;//Плавно меняется, подстраиваясь под сервер
private final DefaultValueMap<Tick, List<Logic.PlayerAction>> actions = new DefaultValueMap<>(new HashMap<Tick, List<Logic.PlayerAction>>(), new DefaultValueMap.ICreateNew<List<Logic.PlayerAction>>() {
	public List<Logic.PlayerAction> createNew() {return new ArrayList<>();}
});
private final DefaultValueMap<Tick, List<Action>> myActions = new DefaultValueMap<>(new HashMap<Tick, List<Action>>(), new DefaultValueMap.ICreateNew<List<Action>>() {
	public List<Action> createNew() {return new ArrayList<>();}
});
private StateWrapper stable;
private Float serverTickPreviousTime;
public Model() {
	final boolean LOCAL =
			LibAllGwt.TRUE();
//		  LibAllGwt.FALSE();
	client = LOCAL ? new PingClient("192.168.0.82", 5000, "socket", ServerSayS.class) : new PingClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
	client.connect(new Signal.Listener<ServerPayload>() {
		public void onSignal(ServerPayload s) {
			synchronized(this) {
				serverTick = s.tick + getLatencySeconds() / Logic.UPDATE_S;
				serverTickPreviousTime = App.timeSinceCreate();
				if(s.welcome != null) {
					playerId = s.welcome.id;
					clientTick = s.tick;
				}
				if(s.stable != null) {
					if(s.stable.state != null) {
						stable = new StateWrapper();
						stable.state = s.stable.state;
						stable.tick = s.stable.tick;
					} else {
						stable.tick(s.stable.tick);
					}
					clearCache(s.stable.tick);
				}
				if(s.actions != null && s.actions.size() > 0) {
					for(ServerPayload.TickActions t : s.actions) {
						actions.getExistsOrPutDefault(new Tick(t.tick)).addAll(t.list);
						clearCache(t.tick + 1);
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
								clearCache(t.tick + 1);
								continue;
							}
						}
						if(s.apply != null) {
							for(ServerPayload.AppliedActions apply : s.apply) {
								if(apply.aid == next.aid) {
									if(apply.delay > 0) {
										actions.getExistsOrPutDefault(t.add(apply.delay)).add(new Logic.PlayerAction(playerId, next.action));
										iterator.remove();
										clearCache(t.tick + 1);
									}
									continue whl;
								}
							}
						}
					}
				}
			}
		}
	});
}
public int getLatency() {
	if(client.latency == null) return Params.DEFAULT_LATENCY_MS;
	return client.latency;
}
public String getPlayerName() {
	if(playerId == null) {
		return "Wait connection...";
	}
	return "Player " + playerId.toString();
}
public boolean ready() {
	return playerId != null;
}
public float getLatencySeconds() {
	return (client.latency == null ? Params.DEFAULT_LATENCY_MS : client.latency) / 1000f;
}
private int previousActionId = 0;
public void action(Logic.Action action) {
	synchronized(this) {
		if(!ready()) return;
		if(serverTick - clientTick > Params.DELAY_TICKS) return;
		if(clientTick - serverTick > Params.FUTURE_TICKS) return;
		int w = (int) (getLatencySeconds() / Logic.UPDATE_S) + 1;//todo Учитывать среднюю задержку
		ClientPayload.ClientAction a = new ClientPayload.ClientAction();
		a.aid = ++previousActionId;
		a.wait = w;
		a.tick = (int) clientTick + w;
		a.action = action;
		myActions.getExistsOrPutDefault(new Tick((int) clientTick + w)).add(new Action(a.aid, a.action));
		ClientPayload payload = new ClientPayload();
		payload.tick = (int) clientTick;
		payload.actions = new ArrayList<>();
		payload.actions.add(a);
		client.say(payload);
	}
}
public void touch(Logic.XY pos) {//todo move out?
	Logic.State displayState = getDisplayState();
	if(displayState == null) return;
	for(Logic.Car car : displayState.cars) {
		if(playerId.equals(car.owner)) {
			Logic.Angle direction = pos.sub(car.pos).calcAngle().add(new Logic.DegreesAngle(0 * 180));
			action(new Logic.Action(direction));
			break;
		}
	}
}
public void update(float graphicDelta) {
	if(serverTickPreviousTime == null) return;
	float time = App.timeSinceCreate();
	serverTick += (time - serverTickPreviousTime) / Logic.UPDATE_S;
	serverTickPreviousTime = time;
	clientTick += graphicDelta / Logic.UPDATE_S;
	clientTick += (serverTick - clientTick) * LibAllGwt.Fun.arg0toInf(Math.abs((serverTick - clientTick) * graphicDelta), 6f);
}
public @Nullable Logic.State getDisplayState() {
	return getState((int) clientTick);
}
private StateWrapper cache;
private void clearCache(int tick) {
	if(cache != null && tick < cache.tick) {
		cache = null;
	}
}
private StateWrapper getNearestCache(int tick) {
	if(cache != null && cache.tick <= tick) {
		return cache;
	}
	return null;
}
private void saveCache(StateWrapper value) {
	cache = value;
}
private @Nullable Logic.State getState(int tick) {
	StateWrapper result = getNearestCache(tick);
	if(result == null) {
		if(stable == null) return null;
		synchronized(this) {
			result = stable.copy();
			saveCache(result);
		}
	}
	result.tick(tick);
	return result.state;
}
public void dispose() {
	client.close();
}
private class Action extends Logic.PlayerAction {
	public final int aid;
	public Action(int aid, Logic.Action action) {
		this.id = playerId;
		this.action = action;
		this.aid = aid;
	}
}

private class StateWrapper {
	public Logic.State state;
	public int tick;
	public StateWrapper copy() {
		StateWrapper result = new StateWrapper();
		result.state = UtilsCore.copy(state);//todo 50% процессорного времени
		result.tick = tick;
		return result;
	}
	public void tick(int targetTick) {
		while(tick < targetTick) {
			List<Logic.PlayerAction> other = actions.map.get(new Tick(tick));
			if(other != null) state.act(other.iterator());
			List<Action> my = myActions.map.get(new Tick(tick));
			if(my != null) state.act(my.iterator());
			state.tick();
			tick++;
		}
	}
}
}
