package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketState;
import com.github.czyzby.websocket.net.ExtendedNet;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientSay;
import com.n8cats.share.Params;
import com.n8cats.share.ServerSay;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class PingClient<S, C> {
private final Signal<S> incoming = new Signal<>();
private final WebSocket socket;
private final Queue<ClientSay<C>> queue = new LinkedList<>();//todo test
private static final Json json = new Json();
public float smartLatencyS = Params.DEFAULT_LATENCY_S;
public float latencyS = Params.DEFAULT_LATENCY_S;
private Queue<LatencyTime> latencies = new ArrayDeque<>();
public PingClient(String host, int port, String path, final Class<ServerSay<S>> typeS) {
	latencies.add(new LatencyTime(Params.DEFAULT_LATENCY_MS, App.timeMs()));
	socket = LibAllGwt.TRUE() ? ExtendedNet.getNet().newWebSocket(host, port, path) : WebSockets.newSocket(WebSockets.toWebSocketUrl(host, port, path));
	socket.addListener(new WebSocketAdapter() {
		public boolean onOpen(final WebSocket webSocket) {
			while(queue.peek() != null) sayNow(queue.poll());
			return FULLY_HANDLED;
		}
		public boolean onClose(final WebSocket webSocket, final WebSocketCloseCode code, final String reason) {
			return FULLY_HANDLED;
		}
		public boolean onMessage(final WebSocket webSocket, final String packet) {
			ServerSay<S> serverSay = json.fromJson(typeS, packet);
			if(serverSay.latency != null) {
				latencyS = serverSay.latency / LibAllGwt.MILLIS_IN_SECCOND;
				latencies.offer(new LatencyTime(serverSay.latency, App.timeMs()));
				while(latencies.size() > 100) latencies.poll();
				float sum = 0;
				float weights = 0;
				final long time = App.timeMs();
				for(LatencyTime l : latencies) {
					double w = 1 - LibAllGwt.Fun.arg0toInf(time - l.time, 10_000);
					w *= 1 - LibAllGwt.Fun.arg0toInf(l.latency, Params.DEFAULT_LATENCY_MS);
					sum += w * l.latency;
					weights += w;
				}
				if(weights > Float.MIN_VALUE * 1E10) smartLatencyS = sum / weights / LibAllGwt.MILLIS_IN_SECCOND;
			}
			if(serverSay.ping) {
				ClientSay<C> answer = new ClientSay<>();
				answer.pong = true;
				say(answer);
			}
			if(serverSay.payload != null) incoming.dispatch(serverSay.payload);
			return FULLY_HANDLED;
		}
		public boolean onMessage(WebSocket webSocket, byte[] packet) {
			return super.onMessage(webSocket, packet);
		}
		public boolean onError(WebSocket webSocket, Throwable error) {
			return super.onError(webSocket, error);//todo
		}
	});
}
public void connect(Signal.Listener<S> incomeListener) {
	incoming.add(incomeListener);
	try {
		socket.connect();
	} catch(Exception e) {//todo
		e.printStackTrace();

	}
}
public void close() {
	WebSockets.closeGracefully(socket); // Null-safe closing method that catches and logs any exceptions.
	if(false) socket.close();
}
public void say(C payload) {
	ClientSay<C> answer = new ClientSay<>();
	answer.payload = payload;
	say(answer);
}
private void say(ClientSay<C> say) {
	if(socket.getState() == WebSocketState.OPEN) sayNow(say);
	else queue.offer(say);
}
private void sayNow(ClientSay<C> say) {
	int attempt = 0;
	while(attempt++ < 3) {//todo Костыль JSON сериализации
		try {
			socket.send(json.toJson(say));
			return;
		} catch(Throwable t) {}
	}
	App.log.error("sayNow 3 attempts fail");
}
public WebSocketState getState() {
	return socket.getState();
}

private static class LatencyTime {
	public final int latency;
	public final long time;
	public LatencyTime(int latency, long time) {
		this.latency = latency;
		this.time = time;
	}
}
}
