package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.net.ExtendedNet;
import com.n8cats.lib.LibAll;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

public class Small extends ApplicationAdapter {
public static final String SERVER = "n8cats3.herokuapp.com";
private SpriteBatch batch;
private BitmapFont font;
private WebSocket socket;
private ServerSay say;

@Override
public void create() {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	batch = new SpriteBatch();
	font = new BitmapFont();
		socket = ExtendedNet.getNet().newWebSocket("localhost", 5000, "socket");
//	socket = ExtendedNet.getNet().newWebSocket(SERVER, 80, "socket");
	if(false) {
		WebSockets.newSocket(WebSockets.toWebSocketUrl(SERVER, 80, "socket"));
	}
	socket.addListener(new WebSocketAdapter() {
		@Override
		public boolean onOpen(final WebSocket webSocket) {
			Gdx.app.log("WS", "Connected!");
			return FULLY_HANDLED;
		}
		@Override
		public boolean onClose(final WebSocket webSocket, final WebSocketCloseCode code, final String reason) {
			Gdx.app.log("WS", "Disconnected - status: " + code + ", reason: " + reason);
			return FULLY_HANDLED;
		}
		@Override
		public boolean onMessage(final WebSocket webSocket, final String packet) {
			ServerSay serverSay = new Json().fromJson(ServerSay.class, packet);
			say = serverSay;
			ClientSay answer = new ClientSay();
			if(serverSay.ping) {
				answer.pingDelay = 50;
			}
			LibAll.sleep(50);
			answer.message = "message from client";
			webSocket.send(new Json().toJson(answer));
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
	socket.connect();
}

@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	font.draw(batch, socket.getState().name(), 10f + Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
	font.draw(batch, say.id + ": " + say.latency + " " + say.message, 10f, 20);
	batch.end();
}

@Override
public void dispose() {
	WebSockets.closeGracefully(socket); // Null-safe closing method that catches and logs any exceptions.
	batch.dispose();
	font.dispose();
}
}
