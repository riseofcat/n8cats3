package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
import com.riseofcat.session.AbstSesServ;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongServ<C, S, E> extends AbstSesServ<ClientSay<C>, ServerSay<S>, E> {
private final int pingIntervalMs;
private final AbstSesServ<C, S, ExtraLatency<E>> server;
private final Map<Ses, PingSes> sessions = new ConcurrentHashMap<>();
public PingPongServ(AbstSesServ<C, S, ExtraLatency<E>> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
@Override
public void start(Ses session) {
	PingSes s = new PingSes(session);
	sessions.put(session, s);
	server.start(s);
}
@Override
public void close(Ses session) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@Override
public void message(Ses session, ClientSay<C> say) {
	PingSes s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}

private class PingSes extends AbstSesServ<C, S, ExtraLatency<E>>.Ses {
	private final Ses sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private ExtraLatency<E> extra;
	private PingSes(Ses sess) {
		super(sess.id);
		this.sess = sess;
		this.extra = new ExtraLatencyImpl(this);
	}
	@Override
	public void stop() {
		sess.stop();
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
	public ExtraLatency<E> getExtra() {
		return extra;
	}
}
private class ExtraLatencyImpl extends ExtraLatency<E> {
	private final PingSes pingSes;
	public ExtraLatencyImpl(PingSes pingSes) {
		this.pingSes = pingSes;
	}
	@Nullable
	@Override
	public Integer getLatency() {
		return pingSes.latency;
	}
	@Override
	public E getExtra() {
		return pingSes.sess.getExtra();
	}
}
public abstract static class ExtraLatency<Extra> {
	@Nullable
	abstract public Integer getLatency();
	abstract public Extra getExtra();
}

}
