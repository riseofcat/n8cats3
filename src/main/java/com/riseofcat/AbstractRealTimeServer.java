package com.riseofcat;
import java.io.Reader;
abstract public class AbstractRealTimeServer {
private int sessionsCount = 0;
final public void message(Session ses, Reader reader) {
	ses.incomeCalls++;
	abstractMessage(ses, reader);
}
final public void message(Session ses, String message) {
	ses.incomeCalls++;
	abstractMessage(ses, message);
}
final public void start(Session session) {
	abstractStart(session);
	sessionsCount++;
}
final public void close(Session session) {
	abstractClose(session);
	sessionsCount--;
}
public int getSessionsCount() {
	return sessionsCount;
}
abstract protected void abstractMessage(Session ses, Reader reader);
abstract protected void abstractMessage(Session ses, String message);
abstract protected void abstractStart(Session session);
abstract protected void abstractClose(Session session);

public static abstract class Session {
	public final long startTimeMs;
	public final int id;
	public int incomeCalls;
	public int outCalls;
	public Session(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public void send(String message) {
		outCalls++;
	}
	public abstract void stop();
}
}
