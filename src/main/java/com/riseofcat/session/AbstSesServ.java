package com.riseofcat.session;

abstract public class AbstSesServ<CCoded, SCoded> {
private int sessionsCount = 0;
final public void start(Ses<SCoded> session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(Ses<SCoded> session) {
	abstractClose(session);
	sessionsCount--;
}
final public void message(Ses<SCoded> ses, CCoded code) {
	ses.incomeCalls++;
	abstractMessage(ses, code);
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(Ses<SCoded> session);
abstract protected void abstractClose(Ses<SCoded> session);
abstract protected void abstractMessage(Ses<SCoded> ses, CCoded code);

public static abstract class Ses<SCoded> {
	public final long startTimeMs;
	public final int id;
	private int incomeCalls;
	private int outCalls;

	public Ses(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public abstract void stop();
	public void send(SCoded message) {
		outCalls++;
	}
	public int getIncomeCalls() {
		return incomeCalls;
	}
	public int getOutCalls() {
		return outCalls;
	}
}

}
