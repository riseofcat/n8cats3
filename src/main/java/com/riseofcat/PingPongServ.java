package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
import com.riseofcat.session.AbstSesServ;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongServ<TClientPayload, TServerPayload> extends AbstSesServ<ClientSay<TClientPayload>, ServerSay<TServerPayload>> {
private final int pingIntervalMs;
private final AbstSesServ<TClientPayload, TServerPayload> server;
private final Map<Ses<ServerSay<TServerPayload>>, PingSes> sessions = new ConcurrentHashMap<>();
public PingPongServ(AbstSesServ<TClientPayload, TServerPayload> server, int pingIntervalMs) {
	this.pingIntervalMs = pingIntervalMs;
	this.server = server;
}
@Override
public void abstractStart(Ses<ServerSay<TServerPayload>> session) {
	PingSes s = new PingSes(session);
	sessions.put(session, s);
	server.start(s);
}
@Override
public void abstractClose(Ses<ServerSay<TServerPayload>> session) {
	server.close(sessions.get(session));
	sessions.remove(session);
}
@Override
public void abstractMessage(Ses<ServerSay<TServerPayload>> session, ClientSay<TClientPayload> say) {
	PingSes s = sessions.get(session);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.message(s, say.payload);
	}
}

private class PingSes extends AbstSesServ.Ses<TServerPayload> {
	private final Ses<ServerSay<TServerPayload>> sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private PingSes(Ses<ServerSay<TServerPayload>> sess) {
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
