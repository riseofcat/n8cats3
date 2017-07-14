package com.riseofcat;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@WebSocket
public class EchoWebSocket {
//http://sparkjava.com/tutorials/websocket-chat
//http://sparkjava.com/documentation#embedded-web-server

// Store sessions if you want to, for example, broadcast a message to all users
private static final Map<Session, Params> sessions = new ConcurrentHashMap<>();

@OnWebSocketConnect
public void connected(Session session) {
	InetSocketAddress localAddress = session.getLocalAddress();//server
	InetSocketAddress remoteAddress = session.getRemoteAddress();//client
	System.out.println("connected");
	BatchMode batchMode = session.getRemote().getBatchMode();//AUTO by default
	int a = 1;
	sessions.put(session, new Params(System.currentTimeMillis()));
}

@OnWebSocketClose
public void closed(Session session, int statusCode, String reason) {
	sessions.remove(session);
	System.out.println("closed");
}

@OnWebSocketMessage
public void message(Session session, String message) throws IOException {
	System.out.println("Got: " + message);   // Print message
	session.getRemote().sendString(message); // and send it back
	session.isOpen();

}

private static class Params {
	private final long startTime;

	public Params(long time) {
		this.startTime = time;
	}
}
}
