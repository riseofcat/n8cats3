package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.lib_gwt.IStrSerialize;
import com.n8cats.share.ServerSay;

import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class PingPongRTServer<C, S> extends AbstractStringRTServer {
private final AbstractPayloadRTServer<C, S> server;
private final int pingIntervalMs;
private final IStrSerialize<ClientSay<C>> cSerializer;
private final Map<AbstractStringRTServer.Session, Session> sessions = new ConcurrentHashMap<>();
private final IStrSerialize<ServerSay<S>> sSerializer;
public PingPongRTServer(AbstractPayloadRTServer<C, S> server, int pingIntervalMs, IStrSerialize<ClientSay<C>> cSerializer, IStrSerialize<ServerSay<S>> sSerializer) {
	this.server = server;
	this.pingIntervalMs = pingIntervalMs;
	this.cSerializer = cSerializer;
	this.sSerializer = sSerializer;
}
@Override
public void abstractStart(AbstractStringRTServer.Session sess) {
	Session s = new Session(sess);
	sessions.put(sess, s);
	server.start(s);
}
@Override
public void abstractMessage(AbstractStringRTServer.Session sess, Reader reader) {
	message(sess, cSerializer.fromStr(reader));
}
@Override
public void abstractMessage(AbstractStringRTServer.Session sess, String message) {
	message(sess, cSerializer.fromStr(message));
}
@Override
public void abstractClose(AbstractStringRTServer.Session sess) {
	server.close(sessions.get(sess));
	sessions.remove(sess);
}
private void message(AbstractStringRTServer.Session sess, ClientSay<C> say) {
	Session s = sessions.get(sess);
	if(say.pong && s.lastPingTime != null) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.payloadMessage(s, say.payload);
	}
}
private class Session extends AbstractPayloadRTServer.Session<S> {
	private final AbstractStringRTServer.Session sess;
	@Nullable
	private Long lastPingTime;
	@Nullable
	private Integer latency;
	private Session(AbstractStringRTServer.Session sess) {
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
		String message = sSerializer.toStr(say);
		sess.send(message);
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
