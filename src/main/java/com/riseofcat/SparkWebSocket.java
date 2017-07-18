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
private static final Map<Session, SparkSession> sessions = new ConcurrentHashMap<>();
private static int lastId = 0;
private final AbstractRealTimeServer server;
public SparkWebSocket(AbstractRealTimeServer server) {
	this.server = server;
}
@OnWebSocketConnect
public void connected(Session session) {
	SparkSession s = new SparkSession(session, ++lastId);
	sessions.put(session, s);
	server.start(s);
}
@OnWebSocketClose
public void closed(Session session, int statusCode, String reason) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@OnWebSocketMessage
//public void byteMessage(Session session, byte buf[], int offset, int length)
//public void message(Session session, String message) {
public void message(Session session, Reader reader) {//Reader have low ram usage
	if(!session.isOpen()) {
		App.log.error("SparkWebSocket session not open");
		return;
	}
	SparkSession s = sessions.get(session);
	server.message(s, reader);
}
@OnWebSocketError
public void error(Session session, Throwable error) {
	App.log.error("OnWebSocketError " + error);
}
private void todo(Session session) {//todo
	session.suspend().resume();
	session.getRemoteAddress();//client
	session.getRemote().getBatchMode();//AUTO by default
}

private static class SparkSession extends AbstractRealTimeServer.Session {
	public final Session session;
	public SparkSession(Session session, int id) {
		super(id);
		this.session = session;
	}
	@Override
	public void send(String message) {
		session.getRemote().sendString(message, new WriteCallback() {
			@Override
			public void writeFailed(Throwable x) {
				App.log.error("SparkSession.send.writeFailed " + x);
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
