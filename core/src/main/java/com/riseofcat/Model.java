package com.riseofcat;
import com.n8cats.lib_gwt.DefaultValueMap;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.Tick;
import com.n8cats.share.redundant.ServerSayS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Model {
private final PingClient<ServerPayload, ClientPayload> client;
private final Logic logic = new Logic();
private Logic.Player.Id playerId;
private float clientTick;//Плавно меняется, подстраиваясь под сервер
private float serverTick;//Задаётся моментально с сервера
//todo test not concurrent hash maps:
private final DefaultValueMap<Tick, List<ServerPayload.PlayerAction>> actions =
		new DefaultValueMap<>(new HashMap<>(), ArrayList::new);
private final DefaultValueMap<Tick, List<ClientPayload.ClientAction>> clientActions =
		new DefaultValueMap<>(new HashMap<>(), ArrayList::new);//todo redundant field "wait"
private Logic.State state;
private int stateTick;
private int stableTick;
private int previousActionId = 0;
public static final int DEFAULT_LATENCY_MS = 50;
public static final boolean LOCAL = LibAllGwt.TRUE();

public Model() {
	client = LOCAL ? new PingClient("localhost", 5000, "socket", ServerSayS.class) : new PingClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
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
		if(s.actions != null && s.actions.size() > 0) {
			for(ServerPayload.TickActions t : s.actions) {
				actions.getExistsOrPutDefault(new Tick(t.tick)).addAll(t.list);
			}
		}
		for(Tick t : clientActions.map.keySet()) {
			Iterator<ClientPayload.ClientAction> iterator = clientActions.map.get(t).iterator();
			whl:
			while(iterator.hasNext()) {
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
							actions.getExistsOrPutDefault(t.add(apply.delay)).add(pa);
							iterator.remove();
							continue whl;
						}
					}
				}
			}
		}
		serverTick = s.tick + getLatencySeconds() / Logic.UPDATE_S;
		clientTick = serverTick;//todo плавно
	});
}
public boolean ready() {
	return playerId != null;
}
public float getLatencySeconds() {
	return (client.latency == null ? DEFAULT_LATENCY_MS : client.latency) / 1000f;
}
public void touch(float x, float y) {
	if(!ready()) return;
	int w = (int) (getLatencySeconds() / Logic.UPDATE_S) + 1;//todo Учитывать среднюю задержку
	ClientPayload.ClientAction a = new ClientPayload.ClientAction();
	a.aid = ++previousActionId;
	a.wait = w;
	a.action = new Logic.Action(x, y);
	clientActions.getExistsOrPutDefault(new Tick((int) clientTick + w)).add(a);
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
	if(tick == stateTick) return state.copy();
	List<ServerPayload.PlayerAction> as = new ArrayList<>();
	List<ServerPayload.PlayerAction> others = actions.map.get(new Tick(tick - 1));
	if(others != null) as.addAll(others);
	List<ClientPayload.ClientAction> clientTickActions = clientActions.map.get(new Tick(tick - 1));
	if(clientTickActions != null) {
		for(ClientPayload.ClientAction my : clientTickActions) {
			ServerPayload.PlayerAction pa = new ServerPayload.PlayerAction();
			pa.id = playerId;
			pa.action = my.action;
			as.add(pa);
		}
	}
	Logic.State s = getState(tick - 1);
	logic.update(s, as);
	return s;
}
public void dispose() {
	client.close();
}
}
