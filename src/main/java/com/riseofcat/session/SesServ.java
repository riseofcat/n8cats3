package com.riseofcat.session;

abstract public class SesServ {
private int sessionsCount = 0;
final public void start(StrSessServ.Session session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(StrSessServ.Session session) {
	abstractClose(session);
	sessionsCount--;
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(StrSessServ.Session session);
abstract protected void abstractClose(StrSessServ.Session session);

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
