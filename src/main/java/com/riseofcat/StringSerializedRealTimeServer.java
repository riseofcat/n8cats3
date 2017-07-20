package com.riseofcat;

import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;
//import com.sun.istack.internal.Nullable;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class StringSerializedRealTimeServer<C, S> extends AbstractStringRealTimeServer {
private final AbstractPayloadServer<C, S> server;
private final int pingIntervalMs;
private final IStringSerializer<C, S> serializer;
private final Map<AbstractStringRealTimeServer.Session, Session> sessions = new ConcurrentHashMap<>();
public StringSerializedRealTimeServer(AbstractPayloadServer<C, S> server, int pingIntervalMs, IStringSerializer<C, S> serializer) {
	this.server = server;
	this.pingIntervalMs = pingIntervalMs;
	this.serializer = serializer;
}
@Override
public void abstractStart(AbstractStringRealTimeServer.Session sess) {
	Session s = new Session(sess);
	sessions.put(sess, s);
	server.start(s);
}
@Override
public void abstractMessage(AbstractStringRealTimeServer.Session sess, Reader reader) {
	message(sess, serializer.fromStringC(reader));
}
@Override
public void abstractMessage(AbstractStringRealTimeServer.Session sess, String message) {
	message(sess, serializer.fromStringC(message));
}
@Override
public void abstractClose(AbstractStringRealTimeServer.Session sess) {
	server.close(sessions.get(sess));
	sessions.remove(sess);
}
private void message(AbstractStringRealTimeServer.Session sess, ClientSay<C> say) {
	Session s = sessions.get(sess);
	if(say.pong) {
		long l = (System.currentTimeMillis() - s.lastPingTime + 1) / 2;
		s.latency = (int) l;
	}
	if(say.payload != null) {
		server.payloadMessage(s, say.payload);
	}
}
private class Session extends AbstractPayloadServer.Session<C, S> {
	private final AbstractStringRealTimeServer.Session sess;
//	@Nullable
	private Long lastPingTime;
//	@Nullable
	private Integer latency;
	private Session(AbstractStringRealTimeServer.Session sess) {
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
		String message = serializer.toStringS(say);
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
public interface IStringSerializer<C, S> {//todo change to StringSerializer from share
	ClientSay<C> fromStringC(String str);
	ServerSay<S> fromStringS(String str);
	ClientSay<C> fromStringC(Reader reader);
	ServerSay<S> fromStringS(Reader reader);
	String toStringC(ClientSay<C> c);
	String toStringS(ServerSay<S> s);
}
}
