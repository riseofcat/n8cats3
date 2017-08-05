package com.riseofcat.session;

abstract public class SesServ {
private int sessionsCount = 0;
final public void start(StrSesServ.Ses session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(StrSesServ.Ses session) {
	abstractClose(session);
	sessionsCount--;
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(StrSesServ.Ses session);
abstract protected void abstractClose(StrSesServ.Ses session);

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
