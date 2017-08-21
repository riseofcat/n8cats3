package com.riseofcat;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.redundant.ServerSayS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Model {
final static boolean LOCAL = true;
private final PingClient<ServerPayload, ClientPayload> client;
private final Logic logic;
private Logic.Player.Id playerId;
private float clientTick;//Плавно меняется, подстраиваясь под сервер
private float serverTick;//Задаётся моментально с сервера
private Map<Integer, List<ServerPayload.PlayerAction>> actions = new ConcurrentHashMap<>();//todo Tick key
private Map<Integer, List<ClientPayload.ClientAction>> clientActions = new HashMap<>();//todo redundant field "wait"
private Logic.State state;
private int stateTick;
private int stableTick;
private int previousActionId=0;
public static final int DEFAULT_LATENCY_MS = 50;

public Model() {
	if(LOCAL) {
		client = new PingClient("localhost", 5000, "socket", ServerSayS.class);
	} else {
		client = new PingClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
	}
	logic = new Logic();
	client.incoming.add(s -> {
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
		if(s.actions != null) {
			for(ServerPayload.TickActions t : s.actions) {
				List<ServerPayload.PlayerAction> tckActs = actions.get(t.tick);
				if(tckActs == null) {//todo duplicate
					tckActs = new ArrayList<>();
					actions.put(t.tick, tckActs);
				}
				tckActs.addAll(t.list);
			}
		}

		for(Integer t : clientActions.keySet()) {
			Iterator<ClientPayload.ClientAction> iterator = clientActions.get(t).iterator();
			whl: while(iterator.hasNext()) {
				ClientPayload.ClientAction next = iterator.next();
				if(s.canceled != null) {
					if(s.canceled.contains(next.aid)) {
						iterator.remove();
						continue;
					}
				}
				if(s.apply != null) {
					for(ServerPayload.ApplyedActions apply : s.apply) {
						if(apply.aid == next.aid) {
							ServerPayload.PlayerAction pa = new ServerPayload.PlayerAction();
							pa.action = next.action;
							pa.id = playerId;
							List<ServerPayload.PlayerAction> tckActs = actions.get(t + apply.delay);
							if(tckActs == null) {//todo duplicate
								tckActs = new ArrayList<>();
								actions.put(t + apply.delay, tckActs);
							}
							tckActs.add(pa);
							iterator.remove();
							continue whl;
						}
					}
				}
			}
		}
		serverTick = s.tick + getLatencySeconds()/Logic.UPDATE_S;
		clientTick = serverTick;//todo плавно
	});
}
public float getLatencySeconds() {
	return (client.latency == null ? DEFAULT_LATENCY_MS : client.latency)/1000f;
}
public void touch(float x, float y) {
	if(playerId == null) {
		return;
	}
	int w = (int) (getLatencySeconds() / Logic.UPDATE_S);
	List<ClientPayload.ClientAction> ca = this.clientActions.get((int) clientTick + w);
	if(ca == null) {//todo duplicate
		ca = new ArrayList<>();
		this.clientActions.put((int) clientTick + w, ca);
	}
	ClientPayload.ClientAction a = new ClientPayload.ClientAction();
	a.aid = ++previousActionId;
	a.wait = w;
	a.action = new Logic.Action();
	a.action.touchX = x;
	a.action.touchY = y;
	ca.add(a);
	client.say(new ClientPayload());
}
public void update(float deltaTime) {
	serverTick+=deltaTime/Logic.UPDATE_MS;
	clientTick +=deltaTime/Logic.UPDATE_MS;
}
public Logic.State getDisplayState() {
	return getState((int)clientTick);//todo плавно
}
private Logic.State getState(int tick) {
	if(tick == stateTick) {
		return state.copy();
	}
	List<ServerPayload.PlayerAction> a = actions.get(tick - 1);
	if(a == null) {
		a = new ArrayList<>();
	}
	for(ClientPayload.ClientAction ca : this.clientActions.get(tick - 1)) {
		ServerPayload.PlayerAction pa = new ServerPayload.PlayerAction();
		pa.id = playerId;
		pa.action = ca.action;
		a.add(pa);
	}
	Logic.State s = getState(tick - 1);
	logic.update(s, a);
	return s;
}
public void dispose() {
	client.close();
}
}
