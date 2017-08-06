package com.riseofcat.session;

abstract public class AbstSesServ<TClientData, TServerData> {
private int sessionsCount = 0;
final public void start(Ses<TServerData> session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(Ses<TServerData> session) {
	abstractClose(session);
	sessionsCount--;
}
final public void message(Ses<TServerData> ses, TClientData code) {
	ses.incomeCalls++;
	abstractMessage(ses, code);
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(Ses<TServerData> session);
abstract protected void abstractClose(Ses<TServerData> session);
abstract protected void abstractMessage(Ses<TServerData> ses, TClientData code);

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
