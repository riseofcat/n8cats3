package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
import com.riseofcat.session.SesServ;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongServ<C, S> extends SesServ<ClientSay<C>, ServerSay<S>> {
private final int pingIntervalMs;
private final SesServ<C, S> server;
private final Map<Ses<ServerSay<S>>, PingSes> sessions = new ConcurrentHashMap<>();
public PingPongServ(SesServ<C, S> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
@Override
public void abstractStart(Ses<ServerSay<S>> session) {
	PingSes s = new PingSes(session);
	sessions.put(session, s);
	server.start(s);
}
@Override
public void abstractClose(Ses<ServerSay<S>> session) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@Override
public void abstractMessage(Ses<ServerSay<S>> session, ClientSay<C> say) {
	PingSes s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}

private class PingSes extends SesServ.Ses<S> {
	private final Ses<ServerSay<S>> sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private PingSes(Ses<ServerSay<S>> sess) {
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
//	@Override
	public Integer getLatency() {
		return latency;
	}
}

}
