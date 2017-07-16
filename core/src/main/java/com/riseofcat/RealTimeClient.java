package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketState;
import com.github.czyzby.websocket.net.ExtendedNet;
import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
public class RealTimeClient<S, C> {
public final Signal<S> incoming = new Signal<>();
private WebSocket socket;
public Integer latency;
public RealTimeClient(String host, int port, String path, Class<ServerSay<S>> typeS) {
	if(true) {
		socket = ExtendedNet.getNet().newWebSocket(host, port, path);
	} else {
		socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(host, port, path));
	}
	socket.addListener(new WebSocketAdapter() {
		@Override
		public boolean onOpen(final WebSocket webSocket) {
			return FULLY_HANDLED;
		}
		@Override
		public boolean onClose(final WebSocket webSocket, final WebSocketCloseCode code, final String reason) {
			return FULLY_HANDLED;
		}
		@Override
		public boolean onMessage(final WebSocket webSocket, final String packet) {
			ServerSay<S> serverSay = new Json().fromJson(typeS, packet);
			if(serverSay.latency != null) {
				latency = serverSay.latency;
			}
			if(serverSay.ping) {
				ClientSayC answer = new ClientSayC();
				answer.pong = true;
				say(answer);
			}
			if(serverSay.payload != null) {
				incoming.dispatch(serverSay.payload);
			}
			return FULLY_HANDLED;
		}

		@Override
		public boolean onMessage(WebSocket webSocket, byte[] packet) {
			return super.onMessage(webSocket, packet);
		}

		@Override
		public boolean onError(WebSocket webSocket, Throwable error) {
			return super.onError(webSocket, error);
		}
	});
	socket.connect();//socket.close();
}

public void say(C payload) {
	ClientSayC answer = new ClientSayC();
	answer.payload = payload;
	say(answer);
}

private void say(ClientSayC say) {
	if(socket.getState() != WebSocketState.OPEN) {
		//todo queue
	}
	socket.send(new Json().toJson(say));
}

public WebSocketState getState() {
	return socket.getState();
}

public void close() {
	WebSockets.closeGracefully(socket); // Null-safe closing method that catches and logs any exceptions.
}

private class ClientSayC extends ClientSay<C> {

}

}
