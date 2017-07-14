package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class EchoWebSocket {
//http://sparkjava.com/tutorials/websocket-chat
//http://sparkjava.com/documentation#embedded-web-server

// Store sessions if you want to, for example, broadcast a message to all users
private static final Map<Session, Params> sessions = new ConcurrentHashMap<>();
private static int lastId = 0;

@OnWebSocketConnect
public void connected(Session session) {
	InetSocketAddress localAddress = session.getLocalAddress();//server
	InetSocketAddress remoteAddress = session.getRemoteAddress();//client
	System.out.println("connected");
	BatchMode batchMode = session.getRemote().getBatchMode();//AUTO by default
	long currentTime = System.currentTimeMillis();
	Params params = new Params(currentTime);
	params.id = ++lastId;
	sessions.put(session, params);
	ServerSay json = new ServerSay();
//	json.latency = LibAllGwt.getRand(50,100);
	json.message = "message from server";
	json.ping = true;
	json.id = params.id;
	try {
		session.getRemote().sendString(new Json().toJson(json));
		params.lastPingTime = currentTime;
	} catch(IOException e) {
		e.printStackTrace();
	}
}

@OnWebSocketClose
public void closed(Session session, int statusCode, String reason) {
	sessions.remove(session);
	System.out.println("closed");
}

@OnWebSocketMessage
public void message(Session session, String message) throws IOException {
//	System.out.println("Got: " + message);   // Print message
	if(false) {
		session.getRemote().sendString(message); // and send it back
	}
	if(!session.isOpen()) {
		App.log("session not open");
		throw new RuntimeException("handle session not open");
	}
	ClientSay clientSay = new Json().fromJson(ClientSay.class, message);
	Params params = sessions.get(session);
	if(clientSay.pingDelay != null) {
		long l = System.currentTimeMillis() - params.lastPingTime - clientSay.pingDelay;
		l = l / 2;
		params.latency = (int) l;
		if(false) {
			params.lastPingTime = null;
		}
	}
	params.calls++;
	ServerSay json = new ServerSay();
//	json.latency = LibAllGwt.getRand(50,100);
	json.message = "message from server";
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
