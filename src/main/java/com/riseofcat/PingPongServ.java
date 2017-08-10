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
private final Map<Ses<ServerSay<TServerPayload>, Void>, PingSes> sessions = new ConcurrentHashMap<>();
public PingPongServ(AbstSesServ<TClientPayload, TServerPayload, ExtraLatency> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
@Override
protected void abstractClose(Ses<ServerSay<TServerPayload>, Void> session) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@Override
protected void abstractStart(Ses<ServerSay<TServerPayload>, Void> session) {
	PingSes s = new PingSes(session);
	sessions.put(session, s);
	server.start(s);
}
@Override
protected void abstractMessage(Ses<ServerSay<TServerPayload>, Void> session, ClientSay<TClientPayload> say) {
	PingSes s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}

public class PingSes extends AbstSesServ.Ses<TServerPayload, ExtraLatency> {
	private final Ses<ServerSay<TServerPayload>, Void> sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private ExtraLatency extra;
	private PingSes(Ses<ServerSay<TServerPayload>, Void> sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void abstractSend(TServerPayload payload) {
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
