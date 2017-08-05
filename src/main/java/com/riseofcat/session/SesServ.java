package com.riseofcat.session;

abstract public class SesServ<CCoded, SCoded> {
private int sessionsCount = 0;
final public void start(CodeSesServ.Ses<SCoded> session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(CodeSesServ.Ses<SCoded> session) {
	abstractClose(session);
	sessionsCount--;
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(CodeSesServ.Ses<SCoded> session);//todo CodeSesServ.Ses to Ses
abstract protected void abstractClose(CodeSesServ.Ses<SCoded> session);

public static abstract class Ses {
	public final long startTimeMs;
	public final int id;
	public Ses(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public abstract void stop();
	public abstract int getIncomeCalls();
	public abstract int getOutCalls();
}

}
