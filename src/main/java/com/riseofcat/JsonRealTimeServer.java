package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class JsonRealTimeServer<C, S> extends AbstractRealTimeServer {
private final Class<ClientSay<C>> typeC;
private final AbstractPayloadServer<C,S> server;
private final int pingIntervalMs;
public JsonRealTimeServer(Class<ClientSay<C>> typeC, int pingIntervalMs, AbstractPayloadServer<C,S> server) {
	this.typeC = typeC;
	this.server = server;
	this.pingIntervalMs = pingIntervalMs;
}
private final Map<AbstractRealTimeServer.Session, Session> sessions = new ConcurrentHashMap<>();
@Override
public void abstractStart(AbstractRealTimeServer.Session sess) {
	Session s = new Session(sess);
	sessions.put(sess, s);
	server.starts(s);
}
@Override
public void abstractMessage(AbstractRealTimeServer.Session sess, Reader reader) {
	message(sess, new Json().fromJson(typeC, reader));
}
@Override
public void abstractMessage(AbstractRealTimeServer.Session sess, String message) {
	message(sess, new Json().fromJson(typeC, message));
}
@Override
public void abstractClose(AbstractRealTimeServer.Session sess) {
	server.closed(sessions.get(sess));
	sessions.remove(sess);
}
private void message(AbstractRealTimeServer.Session sess, ClientSay<C> say) {
	Session s = sessions.get(sess);
	if(say.pong) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1)/2;
		s.latency = (int)l;
	}
	if(say.payload != null) {
		server.payloadMessage(s, say.payload);
	}
}
private class Session extends AbstractPayloadServer.Session<C,S>{
	private final AbstractRealTimeServer.Session sess;
	private Long lastPingTime;
	private Integer latency;
	private Session(AbstractRealTimeServer.Session sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void send(S payload) {
		ServerSay<S> say = new ServerSay<>();
		say.id = sess.id;
		say.latency = latency;
		if(lastPingTime == null || System.currentTimeMillis() > lastPingTime + pingIntervalMs) {
			say.ping = true;
			lastPingTime = System.currentTimeMillis();
		}
		say.payload = payload;
		String message = new Json().toJson(say);
		sess.send(message);
	}
	@Override
	public void stop() {
		sess.stop();
	}
	@Override
	public Integer getLatency() {
		return latency;
	}
}
}
