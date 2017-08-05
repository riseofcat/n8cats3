package com.riseofcat.session;
import java.io.Reader;
abstract public class StrSesServ extends SesServ {
final public void message(Ses ses, Reader reader) {
	ses.incomeCalls++;
	abstractMessage(ses, reader);
}
final public void message(Ses ses, String message) {
	ses.incomeCalls++;
	abstractMessage(ses, message);
}
abstract protected void abstractMessage(Ses ses, Reader reader);
abstract protected void abstractMessage(Ses ses, String message);

public static abstract class Ses extends SesServ.Ses {
	private int incomeCalls;
	private int outCalls;
	public Ses(int id) {
		super(id);
	}
	public void send(String message) {
		outCalls++;
	}
	@Override
	public int getIncomeCalls() {
		return incomeCalls;
	}
	@Override
	public int getOutCalls() {
		return outCalls;
	}
}

}
