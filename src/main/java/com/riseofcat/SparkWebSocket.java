package com.riseofcat;

import com.riseofcat.session.AbstSesServ;

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
public class SparkWebSocket {
//https://github.com/tipsy/spark-websocket
//http://sparkjava.com/tutorials/websocket-chat
//http://sparkjava.com/documentation#embedded-web-server
private final Map<Session, AbstSesServ<Reader, String, Void>.Ses> sessions = new ConcurrentHashMap<>();
private int lastId = 0;
private final AbstSesServ<Reader, String, Void> server;
public SparkWebSocket(AbstSesServ<Reader, String, Void> server) {
	this.server = server;
}
@OnWebSocketConnect
public void connected(Session session) {
	AbstSesServ<Reader, String, Void>.Ses s = server.new Ses(++lastId) {
		@Override
		public void stop() {
			session.close();
		}
		@Override
		public void send(String message) {
			if(!session.isOpen()) {
				App.log.error("SparkWebSocket !session.isOpen()");
				return;
			}
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
		public Void getExtra() {
			return null;
		}
	};
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
	server.message(sessions.get(session), reader);
}
@OnWebSocketError
public void error(Session session, Throwable error) {
	App.log.error("OnWebSocketError " + error);
	error.printStackTrace();
}

private void todo(Session session) {//todo
	session.suspend().resume();
	session.getRemoteAddress();//client
	session.getRemote().getBatchMode();//AUTO by default
}
}
