package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.Params;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.ShareTodo;
import com.n8cats.share.Tick;
import com.n8cats.share.redundant.ServerSayS;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Model {
public final PingClient<ServerPayload, ClientPayload> client;
public Logic.Player.Id playerId;
private final DefaultValueMap<Tick, List<Logic.BigAction>> actions = new DefaultValueMap<>(new HashMap<Tick, List<Logic.BigAction>>(), new DefaultValueMap.ICreateNew<List<Logic.BigAction>>() {
	public List<Logic.BigAction> createNew() {return App.context.createConcurrentList();}
});
private final DefaultValueMap<Tick, List<Action>> myActions = new DefaultValueMap<>(new HashMap<Tick, List<Action>>(), new DefaultValueMap.ICreateNew<List<Action>>() {
	public List<Action> createNew() {return new ArrayList<>();}
});
private StateWrapper stable;
private Sync sync;

private static class Sync {
	final float serverTick;
	final float clientTick;
	final long time;
	public Sync(float serverTick, @Nullable Sync oldSync) {
		time = App.timeMs();
		this.serverTick = serverTick;
		if(oldSync == null) this.clientTick = serverTick;
		else this.clientTick = oldSync.calcClientTick();
	}
	private float calcServerTick(long t) {
		return serverTick + (t - time) / (float)Logic.UPDATE_MS;
	}
	public float calcServerTick() {
		return calcServerTick(App.timeMs());
	}
	public float calcClientTick() {
		long t = App.timeMs();
		return calcServerTick(t) + (clientTick - serverTick) * (1f - LibAllGwt.Fun.arg0toInf(t - time, 600));
	}
}
public Model() {
	final boolean LOCAL =
//			LibAllGwt.TRUE();
		  LibAllGwt.FALSE();
	String host = "n8cats3.herokuapp.com";
	int port = 80;
	if(LOCAL) {//todo параметры при компиляции
//		host = "192.168.0.82";
		host = "localhost";//"127.0.0.1"
		port = 5000;
	}
	client = new PingClient(host, port, "socket", ServerSayS.class);
	client.connect(new Signal.Listener<ServerPayload>() {
		public void onSignal(ServerPayload s) {
			synchronized(this) {
				sync = new Sync(s.tick + client.smartLatencyS / Logic.UPDATE_S, sync);
				if(s.welcome != null) playerId = s.welcome.id;
				if(s.stable != null) {
					if(s.stable.state != null) {
						stable = new StateWrapper();
						stable.state = s.stable.state;
						stable.tick = s.stable.tick;
					} else stable.tick(s.stable.tick);
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
									if(!ShareTodo.SIMPLIFY) actions.getExistsOrPutDefault(t.add(apply.delay)).add(new Logic.PlayerAction(playerId, next.action).toBig());
									iterator.remove();
									clearCache(t.tick + 1);
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
public String getPlayerName() {
	if(playerId == null) return "Wait connection...";
	return "Player " + playerId.toString();
}
public boolean ready() {
	return playerId != null;
}
private int previousActionId = 0;
public void action(Logic.Action action) {
	synchronized(this) {
		final int clientTick = (int) sync.calcClientTick();
		if(!ready()) return;
		if(false) {
			if(sync.calcServerTick() - sync.calcClientTick() > Params.DELAY_TICKS * 1.5) return;
			if(sync.calcClientTick() - sync.calcServerTick() > Params.FUTURE_TICKS * 1.5) return;
		}
		int w = (int) (client.smartLatencyS / Logic.UPDATE_S + 1);//todo delta serverTick-clientTick
		ClientPayload.ClientAction a = new ClientPayload.ClientAction();
		a.aid = ++previousActionId;
		a.wait = w;
		a.tick = clientTick + w;//todo serverTick?
		a.action = action;
		List<Action> my = myActions.getExistsOrPutDefault(new Tick(clientTick + w));
		synchronized(my) {
			my.add(new Action(a.aid, a.action));
		}
		ClientPayload payload = new ClientPayload();
		payload.tick = clientTick;
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
//	if(serverTickPreviousTime == null) return;
//	float time = App.timeSinceCreate();
//	serverTick += (time - serverTickPreviousTime) / Logic.UPDATE_S;
//	serverTickPreviousTime = time;
//	clientTick += graphicDelta / Logic.UPDATE_S;
//	clientTick += (serverTick - clientTick) * LibAllGwt.Fun.arg0toInf(Math.abs((serverTick - clientTick) * graphicDelta), 6f);
}
public @Nullable Logic.State getDisplayState() {
	if(sync == null) return null;
	return getState((int) sync.calcClientTick());
}
private StateWrapper cache;
private void clearCache(int tick) {
	if(cache != null && tick < cache.tick) cache = null;
}
private StateWrapper getNearestCache(int tick) {
	if(cache != null && cache.tick <= tick) return cache;
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
		result.state = UtilsCore.copy(state);//todo тяжёлая операция
		result.tick = tick;
		return result;
	}
	public void tick(int targetTick) {
		while(tick < targetTick) {
			List<Logic.BigAction> other = actions.map.get(new Tick(tick));
			if(other != null) state.act(other.iterator());
			List<Action> my = myActions.map.get(new Tick(tick));
			if(my != null) {
				synchronized(my) {
					state.act(my.iterator());
				}
			}
			state.tick();
			tick++;
		}
	}
}
}
