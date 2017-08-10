package com.riseofcat.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class CountSesServ<C, S, CE> extends AbstSesServ<C, S, CE> {
private final AbstSesServ<C, S, CE> server;
private Map<Ses, CountSes> sessions = new ConcurrentHashMap<>();
private int sessionsCount = 0;
public CountSesServ(AbstSesServ<C, S, CE> server) {
	this.server = server;
}
@Override
public void start(Ses session) {
	CountSes s = new CountSes(session.id, session);
	sessions.put(session, s);
	server.start(s);
	sessionsCount++;
}
final public void close(Ses session) {
	server.close(sessions.get(session));
	sessions.remove(session);
	sessionsCount--;
}
final public void message(Ses session, C code) {
	CountSes s = sessions.get(session);
	server.message(s, code);
	s.incomeCalls++;
}
public final int getSessionsCount() {
	return sessionsCount;
}

public class CountSes extends Ses {
	private int incomeCalls;
	private int outCalls;
	private Ses session;
	public CountSes(int id, Ses session) {
		super(id);
		this.session = session;
	}
	@Override
	public void stop() {
		session.stop();
	}
	@Override
	public void send(S message) {
		session.send(message);
		outCalls++;
	}
	public int getIncomeCalls() {
		return incomeCalls;
	}
	public int getOutCalls() {
		return outCalls;
	}
	@Override
	public CE getExtra() {
		return session.getExtra();
	}
}

}
