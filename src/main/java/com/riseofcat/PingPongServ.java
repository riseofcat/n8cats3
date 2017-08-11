package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
import com.riseofcat.session.AbstSesServ;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongServ<TClientPayload, TServerPayload> extends AbstSesServ<ClientSay<TClientPayload>, ServerSay<TServerPayload>, Void> {
private final int pingIntervalMs;
private final AbstSesServ<TClientPayload, TServerPayload, ExtraLatency> server;
private final Map<Ses, PingSes> sessions = new ConcurrentHashMap<>();
public PingPongServ(AbstSesServ<TClientPayload, TServerPayload, ExtraLatency> server, int pingIntervalMs) {
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
public void message(Ses session, ClientSay<TClientPayload> say) {
	PingSes s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}

private class PingSes extends AbstSesServ<TClientPayload, TServerPayload, ExtraLatency>.Ses {
	private final Ses sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private ExtraLatency extra;
	private PingSes(Ses sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void send(TServerPayload payload) {
		ServerSay<TServerPayload> say = new ServerSay<>();
		say.id = sess.id;
		say.latency = latency;
		if(lastPingTime == null || System.currentTimeMillis() > lastPingTime + pingIntervalMs) {
			say.ping = true;
			lastPingTime = System.currentTimeMillis();
		}
		say.payload = payload;
		sess.send(say);
		this.extra = new ExtraLatencyImpl(this);
	}
	@Override
	public void stop() {
		sess.stop();
	}
	@Override
	public ExtraLatency getExtra() {
		return extra;
	}
}
private class ExtraLatencyImpl extends ExtraLatency {
	private final PingSes pingSes;
	public ExtraLatencyImpl(PingSes pingSes) {
		super();
		this.pingSes = pingSes;
	}
	@Nullable
	@Override
	public Integer getLatency() {
		return pingSes.latency;
	}
}
public abstract static class ExtraLatency {
	@Nullable
	abstract public Integer getLatency();
}

}
