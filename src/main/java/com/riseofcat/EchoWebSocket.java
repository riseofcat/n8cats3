package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.ServerSay;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class EchoWebSocket {
//http://sparkjava.com/tutorials/websocket-chat
//http://sparkjava.com/documentation#embedded-web-server

private static final Map<Session, Params> sessions = new ConcurrentHashMap<>();
private static int lastId = 0;

@OnWebSocketConnect
public void connected(Session session) {
	Params params = new Params(System.currentTimeMillis());
	params.id = ++lastId;
	sessions.put(session, params);
	ServerSay<ServerPayload> json = new ServerSay<>();
	json.payload = new ServerPayload();
	json.payload.message = "message from server";
	json.ping = true;
	json.id = params.id;
	try {
		session.getRemote().sendString(new Json().toJson(json));
		params.lastPingTime = System.currentTimeMillis();
		App.log.info("send string " + new Json().toJson(json));
	} catch(IOException e) {
		e.printStackTrace();
	}
}

@OnWebSocketClose
public void closed(Session session, int statusCode, String reason) {
	sessions.remove(session);
}

@OnWebSocketMessage
//public void byteMessage(Session session, byte buf[], int offset, int length)
//public void message(Session session, String message) {//todo test ram usage
public void message(Session session, Reader reader) {
	if(!session.isOpen()) {
		App.log.error("session not open");
		return;
	}
	ClientSayC clientSay = new Json().fromJson(ClientSayC.class, reader);
	Params params = sessions.get(session);
	if(clientSay.pong) {
		long l = (System.currentTimeMillis() - params.lastPingTime + 1)/2;
		params.latency = (int)l;
		if(false) {
			params.lastPingTime = null;
		}
	}
	params.calls++;
	ServerSay<ServerPayload> json = new ServerSay<>();
	json.payload = new ServerPayload();
	json.payload.message = "message from server";
	json.latency = params.latency;
	json.ping = true;
	json.id = params.id;
	try {
		session.getRemote().sendString(new Json().toJson(json));
		params.lastPingTime = System.currentTimeMillis();
	} catch(IOException e) {
		e.printStackTrace();
	}
}

@OnWebSocketError
public void error(Session session, Throwable error) {
	App.log.error("OnWebSocketError " + error);
}

public void todo(Session session) {
	session.suspend().resume();
	session.getRemoteAddress();//client
	session.getRemote().getBatchMode();//AUTO by default
}

private static class Params {
	public final long startTime;
	public Long lastPingTime;
	public Integer latency;
	public int calls;
	public int id;
	public Params(long time) {
		this.startTime = time;
	}
}
}
