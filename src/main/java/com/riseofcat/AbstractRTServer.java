package com.riseofcat;

abstract public class AbstractRTServer {
private int sessionsCount = 0;
final public void start(AbstractStringRTServer.Session session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(AbstractStringRTServer.Session session) {
	abstractClose(session);
	sessionsCount--;
}
public final int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractStart(AbstractStringRTServer.Session session);
abstract protected void abstractClose(AbstractStringRTServer.Session session);

public static abstract class Session {
	public final long startTimeMs;
	public final int id;
	public Session(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public abstract void stop();
	public abstract int getIncomeCalls();
	public abstract int getOutCalls();
}

}
