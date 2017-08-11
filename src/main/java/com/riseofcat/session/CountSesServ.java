package com.riseofcat.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class CountSesServ<C, S, E> extends AbstSesServ<C, S, E> {
private final AbstSesServ<C, S, ExtraCount<E>> server;
private Map<Ses, CountSes> map = new ConcurrentHashMap<>();
private int sessionsCount = 0;
public CountSesServ(AbstSesServ<C, S, ExtraCount<E>> server) {
	this.server = server;
}
@Override
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
public final int getSessionsCount() {//todo
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
	@Override
	public int getId() {
		return sess.getId();
	}
	@Override
	public void stop() {
		sess.stop();
	}
	@Override
	public void send(S message) {
		sess.send(message);
		outCalls++;
	}
	@Override
	public ExtraCount<E> getExtra() {
		return extra;
	}
}

private class ExtraCountImpl extends ExtraCount<E> {
	private final CountSes countSes;
	public ExtraCountImpl(CountSes countSes) {
		this.countSes = countSes;
	}
	@Override
	public int getIncomeCalls() {
		return countSes.incomeCalls;
	}
	@Override
	public int getOutCalls() {
		return countSes.outCalls;
	}
	@Override
	public E getExtra() {
		return countSes.sess.getExtra();
	}
	@Override
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
