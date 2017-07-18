package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class JsonRealtimeServer<C, S> extends AbstractRealTimeServer {
private final Class<ClientSay<C>> typeC;
private final int pingInerval;
private final AbstractGameRealtimeServer<C,S> server;
public JsonRealtimeServer(Class<ClientSay<C>> typeC, int pingIntervalMs, AbstractGameRealtimeServer<C,S> server) {
	this.typeC = typeC;
	this.pingInerval = pingIntervalMs;
	this.server = server;
}
private final Map<Session, Sess2> sessions = new ConcurrentHashMap<>();
@Override
public void abstractStart(Session sess) {
	Sess2 sess2 = new Sess2(sess);
	sessions.put(sess, sess2);
	this.server.starts(sess2);
}
@Override
public void abstractMessage(Session sess, Reader reader) {
	abstractMessage(sess, new Json().fromJson(typeC, reader));
}
@Override
public void abstractMessage(Session sess, String message) {
	abstractMessage(sess, new Json().fromJson(typeC, message));
}
private void abstractMessage(Session sess, ClientSay<C> say) {
	Sess2 sess2 = sessions.get(sess);
	if(say.pong) {
		long l = (System.currentTimeMillis() - sess2.lastPingTime + 1)/2;
		sess2.latency = (int)l;
	}
	if(say.payload != null) {
		server.payloadMessage(sess2, say.payload);
	}
}
@Override
public void abstractClose(Session sess) {
	server.closed(sess);
}
public class Sess2 {
	public final Session sess;
	public Long lastPingTime;
	public Integer latency;
	private Sess2(Session sess) {
		this.sess = sess;
	}
	public void send(S payload) {
		ServerSay<S> say = new ServerSay<>();
		say.id = sess.id;
		say.latency = latency;
		if(lastPingTime == null || System.currentTimeMillis() > lastPingTime + pingInerval) {
			say.ping = true;
			lastPingTime = System.currentTimeMillis();
		}
		say.payload = payload;
		String message = new Json().toJson(say);
		sess.send(message);
	}
}
}
