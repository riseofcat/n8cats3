package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Queue;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketState;
import com.github.czyzby.websocket.net.ExtendedNet;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import org.jetbrains.annotations.Nullable;

public class RealTimeClient<S, C> {
public final Signal<S> incoming = new Signal<>();
private WebSocket socket;
@Nullable public Integer latency;
public Integer id;
private Queue<ClientSay<C>> queue = new Queue<>();
public static final Json json = new Json();
public RealTimeClient(String host, int port, String path, Class<ServerSay<S>> typeS) {
	if(true) {
		socket = ExtendedNet.getNet().newWebSocket(host, port, path);
	} else {
		socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(host, port, path));
	}
	socket.addListener(new WebSocketAdapter() {
		public boolean onOpen(final WebSocket webSocket) {
			while(queue.first() != null) {
				sayNow(queue.removeFirst());
			}
			return FULLY_HANDLED;
		}
		public boolean onClose(final WebSocket webSocket, final WebSocketCloseCode code, final String reason) {
			return FULLY_HANDLED;
		}
		public boolean onMessage(final WebSocket webSocket, final String packet) {
			ServerSay<S> serverSay = json.fromJson(typeS, packet);
			if(serverSay.latency != null) {
				latency = serverSay.latency;
			}
			if(serverSay.id != null) {
				RealTimeClient.this.id = serverSay.id;
			}
			if(serverSay.ping) {
				ClientSay<C> answer = new ClientSay<>();
				answer.pong = true;
				say(answer);
			}
			if(serverSay.payload != null) {
				incoming.dispatch(serverSay.payload);
			}
			return FULLY_HANDLED;
		}
		public boolean onMessage(WebSocket webSocket, byte[] packet) {
			return super.onMessage(webSocket, packet);
		}
		public boolean onError(WebSocket webSocket, Throwable error) {
			return super.onError(webSocket, error);//todo
		}
	});
	socket.connect();//socket.close();
}

public void say(C payload) {
	ClientSay<C> answer = new ClientSay<>();
	answer.payload = payload;
	say(answer);
}

private void say(ClientSay<C> say) {
	if(socket.getState() == WebSocketState.OPEN) {
		sayNow(say);
	} else {
		queue.addLast(say);
	}
}

private void sayNow(ClientSay<C> say) {
	socket.send(json.toJson(say));
}

public WebSocketState getState() {
	return socket.getState();
}

public void close() {
	WebSockets.closeGracefully(socket); // Null-safe closing method that catches and logs any exceptions.
}

}
