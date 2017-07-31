package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongServ<C, S> extends AbstractTypedServ<ClientSay<C>, ServerSay<S>> {
private final int pingIntervalMs;
private final AbstractPayloadServ<C, S> server;
private final Map<AbstractTypedServ.Session<ServerSay<S>>, Session> sessions = new ConcurrentHashMap<>();
public PingPongServ(AbstractPayloadServ<C, S> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
@Override
public void start(AbstractTypedServ.Session<ServerSay<S>> session) {
	Session s = new Session(session);
	sessions.put(session, s);
	server.start(s);
}
@Override
public void close(AbstractTypedServ.Session<ServerSay<S>> session) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@Override
public void message(AbstractTypedServ.Session<ServerSay<S>> session, ClientSay<C> say) {
	Session s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.payloadMessage(s, say.payload);
	}
}

private class Session extends AbstractPayloadServ.Session<S> {
	private final AbstractTypedServ.Session<ServerSay<S>> sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private Session(AbstractTypedServ.Session<ServerSay<S>> sess) {
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
		sess.send(say);
	}
	@Override
	public void stop() {
		sess.stop();
	}
	@Nullable
	@Override
	public Integer getLatency() {
		return latency;
	}
}

}
