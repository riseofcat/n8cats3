package com.riseofcat.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class CountSesServ<C, S, E> extends AbstSesServ<C, S, E> {
private final AbstSesServ<C, S, ExtraCount<E>> server;
private Map<Ses, CountSes> sessions = new ConcurrentHashMap<>();
private int sessionsCount = 0;
public CountSesServ(AbstSesServ<C, S, ExtraCount<E>> server) {
	this.server = server;
}
@Override
public void start(Ses session) {
	CountSes s = new CountSes(session);
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
public final int getSessionsCount() {//todo
	return sessionsCount;
}

public class CountSes extends AbstSesServ<C, S, ExtraCount<E>>.Ses {
	private int incomeCalls;
	private int outCalls;
	private Ses sess;
	private final ExtraCount<E> extra;
	public CountSes(Ses session) {
		super(session.id);
		this.sess = session;
		this.extra = new ExtraCountImpl(this);
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
		return 0;
	}
	@Override
	public int getOutCalls() {
		return 0;
	}
	@Override
	public E getExtra() {
		return countSes.sess.getExtra();
	}
}
public abstract static class ExtraCount<Extra> {
	abstract public int getIncomeCalls();
	abstract public int getOutCalls();
	abstract public Extra getExtra();
}

}
