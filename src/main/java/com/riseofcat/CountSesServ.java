package com.riseofcat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CountSesServ<C, S, E> extends AbstSesServ<C, S, E> {
private final AbstSesServ<C, S, ExtraCount<E>> server;
private Map<Ses, CountSes> map = new ConcurrentHashMap<>();
private int sessionsCount = 0;
public CountSesServ(AbstSesServ<C, S, ExtraCount<E>> server) {
	this.server = server;
}
public void start(Ses session) {
	CountSes s = new CountSes(session);
	map.put(session, s);
	server.start(s);
	sessionsCount++;
}
final public void close(Ses session) {
	server.close(map.get(session));
	map.remove(session);
	sessionsCount--;
}
final public void message(Ses session, C code) {
	CountSes s = map.get(session);
	server.message(s, code);
	s.incomeCalls++;
}
public final int getSessionsCount() {
	return sessionsCount;
}

public class CountSes extends AbstSesServ<C, S, ExtraCount<E>>.Ses {
	private int incomeCalls;
	private int outCalls;
	private final Ses sess;
	private final ExtraCount<E> extra;
	private final long startTimeMs;
	public CountSes(Ses session) {
		this.sess = session;
		startTimeMs = System.currentTimeMillis();
		this.extra = new ExtraCountImpl(this);
	}
	public int getId() {
		return sess.getId();
	}
	public void stop() {
		sess.stop();
	}
	public void send(S message) {
		sess.send(message);
		outCalls++;
	}
	public ExtraCount<E> getExtra() {
		return extra;
	}
}

private class ExtraCountImpl extends ExtraCount<E> {
	private final CountSes countSes;
	public ExtraCountImpl(CountSes countSes) {
		this.countSes = countSes;
	}
	public int getIncomeCalls() {
		return countSes.incomeCalls;
	}
	public int getOutCalls() {
		return countSes.outCalls;
	}
	public E getExtra() {
		return countSes.sess.getExtra();
	}
	public long getStartTime() {
		return countSes.startTimeMs;
	}
}

public abstract static class ExtraCount<Extra> {
	abstract public int getIncomeCalls();
	abstract public int getOutCalls();
	abstract public Extra getExtra();
	abstract public long getStartTime();
}

}
