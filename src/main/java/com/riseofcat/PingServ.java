package com.riseofcat;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PingServ<C, S, E> extends AbstSesServ<ClientSay<C>, ServerSay<S>, E> {
private final int pingIntervalMs;
private final AbstSesServ<C, S, Extra<E>> server;
private final Map<Ses, PingSes> map = new ConcurrentHashMap<>();
public PingServ(AbstSesServ<C, S, Extra<E>> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
public void start(Ses session) {
	PingSes s = new PingSes(session);
	map.put(session, s);
	server.start(s);
}
public void close(Ses session) {
	server.close(map.get(session));
	map.remove(session);
}
public void message(Ses session, ClientSay<C> say) {
	PingSes s = map.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}
public abstract static class Extra<Extra> {
	@Nullable abstract public Integer getLatency();
	abstract public Extra getExtra();
}

private class PingSes extends AbstSesServ<C, S, Extra<E>>.Ses {
	private final Ses sess;
	@Nullable private Long lastPingTime;
	@Nullable private Integer latency;
	private Extra<E> extra;
	private PingSes(Ses sess) {
		this.sess = sess;
		this.extra = new ExtraLatencyImpl(this);
		put(Extra2.class, new Extra2(this));
	}
	public int getId() {
		return sess.getId();
	}
	public void stop() {
		sess.stop();
	}
	public void send(S payload) {
		ServerSay<S> say = new ServerSay<>();
		say.latency = latency;
		if(lastPingTime == null || System.currentTimeMillis() > lastPingTime + pingIntervalMs) {
			say.ping = true;
			lastPingTime = System.currentTimeMillis();
		}
		say.payload = payload;
		sess.send(say);
	}
	public Extra<E> getExtra() {
		return extra;
	}
	protected TypeMap getTypeMap() {
		return sess.getTypeMap();
	}
}

private class ExtraLatencyImpl extends Extra<E> {
	private final PingSes pingSes;
	public ExtraLatencyImpl(PingSes pingSes) {
		this.pingSes = pingSes;
	}
	@Nullable public Integer getLatency() {
		return pingSes.latency;
	}
	public E getExtra() {
		return pingSes.sess.getExtra();
	}
}

public static class Extra2 {

	private final PingServ.PingSes pingSes;
	public Extra2(PingServ.PingSes pingSes) {
		this.pingSes = pingSes;
	}
	@Nullable public Integer getLatency() {
		return pingSes.latency;
	}
}

}
