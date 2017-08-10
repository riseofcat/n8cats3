package com.riseofcat.session;

abstract public class AbstSesServ<C, S, E> {
private int sessionsCount = 0;
final public void start(Ses<S, E> session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(Ses<S, E> session) {
	abstractClose(session);
	sessionsCount--;
}
final public void message(Ses<S, E> ses, C code) {
	ses.incomeCalls++;
	abstractMessage(ses, code);
}
public final int getSessionsCount() {//todo redundant
	return sessionsCount;
}
abstract protected void abstractStart(Ses<S, E> session);
abstract protected void abstractClose(Ses<S, E> session);
abstract protected void abstractMessage(Ses<S, E> ses, C code);

public static abstract class Ses<SCoded, Extra> {
	public final long startTimeMs;
	public final int id;
	private int incomeCalls;
	private int outCalls;

	public Ses(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public abstract void stop();
	final public void send(SCoded message) {
		outCalls++;
		abstractSend(message);
	}
	protected abstract void abstractSend(SCoded message);

	public int getIncomeCalls() {//todo redundant
		return incomeCalls;
	}
	public int getOutCalls() {
		return outCalls;
	}
	abstract public Extra getExtra();
}

}
