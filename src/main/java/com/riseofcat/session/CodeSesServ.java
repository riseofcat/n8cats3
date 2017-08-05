package com.riseofcat.session;
abstract public class CodeSesServ<CCoded, SCoded> extends SesServ<CCoded, SCoded> {
final public void message(Ses ses, CCoded code) {
	ses.incomeCalls++;
	abstractMessage(ses, code);
}
abstract protected void abstractMessage(Ses ses, CCoded code);

public static abstract class Ses<SCoded> extends SesServ.Ses {
	private int incomeCalls;
	private int outCalls;
	public Ses(int id) {
		super(id);
	}
	public void send(SCoded message) {
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
