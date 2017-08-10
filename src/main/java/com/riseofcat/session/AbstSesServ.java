package com.riseofcat.session;

abstract public class AbstSesServ<C, S, CE/*, SE*/> {
private int sessionsCount = 0;
final public void start(Ses session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(Ses session) {
	abstractClose(session);
	sessionsCount--;
}
final public void message(Ses ses, C code) {
	ses.incomeCalls++;
	abstractMessage(ses, code);
}
public final int getSessionsCount() {//todo redundant
	return sessionsCount;
}
abstract protected void abstractStart(Ses session);
abstract protected void abstractClose(Ses session);
abstract protected void abstractMessage(Ses ses, C code);

public abstract class Ses {
	public final long startTimeMs;
	public final int id;
	private int incomeCalls;
	private int outCalls;

	public Ses(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public abstract void stop();
	final public void send(S message) {
		outCalls++;
		abstractSend(message);
	}
	protected abstract void abstractSend(S message);

	public int getIncomeCalls() {//todo redundant
		return incomeCalls;
	}
	public int getOutCalls() {
		return outCalls;
	}
	abstract public CE getExtra();
}

}
