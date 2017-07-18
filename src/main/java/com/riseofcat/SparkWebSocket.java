package com.riseofcat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class SparkWebSocket{
//https://github.com/tipsy/spark-websocket
//http://sparkjava.com/tutorials/websocket-chat
//http://sparkjava.com/documentation#embedded-web-server

private static final Map<Session, SparkSess> sessions = new ConcurrentHashMap<>();
private static int lastId = 0;
private final IRealTimeServer server;
public SparkWebSocket(IRealTimeServer server) {
	this.server = server;
}
@OnWebSocketConnect
public void connected(Session session) {
	SparkSess params = new SparkSess(session, ++lastId);
	sessions.put(session, params);
	server.starts(params);
}

@OnWebSocketClose
public void closed(Session session, int statusCode, String reason) {
	server.closed(sessions.get(session));
	sessions.remove(session);
}

@OnWebSocketMessage
//public void byteMessage(Session session, byte buf[], int offset, int length)
//public void message(Session session, String message) {
public void message(Session session, Reader reader) {//Reader have low ram usage
	if(!session.isOpen()) {
		App.log.error("session not open");
		return;
	}
	SparkSess sparkSess = sessions.get(session);
	server.message(sparkSess, reader);
}

@OnWebSocketError
public void error(Session session, Throwable error) {
	App.log.error("OnWebSocketError " + error);
	error.printStackTrace();
}

public void todo(Session session) {//todo
	session.suspend().resume();
	session.getRemoteAddress();//client
	session.getRemote().getBatchMode();//AUTO by default
}

private static class SparkSess extends IRealTimeServer.Sess {
	public final Session session;
	public SparkSess(Session session, int id) {
		super(id);
		this.session = session;
	}
	@Override
	public void send(String message) {
		session.getRemote().sendString(message, new WriteCallback() {
			@Override
			public void writeFailed(Throwable x) {
				App.log.error("SparkSess.send.writeFailed " + x);
			}
			@Override
			public void writeSuccess() {

			}
		});
	}
	@Override
	public void stop() {
		session.close();
	}

}
}
