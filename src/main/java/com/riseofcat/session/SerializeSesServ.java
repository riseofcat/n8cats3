package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public abstract class SerializeSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString, Extra> extends AbstSesServ<TClientCodeReader, TServCodeString, Extra> {
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final IConverter<TServerSay, TServCodeString> sConv;
private final Map<AbstSesServ.Ses, SerializeSes> sessions = new ConcurrentHashMap<>();
private final AbstSesServ<TClientSay, TServerSay, Extra> child;
public SerializeSesServ(AbstSesServ<TClientSay, TServerSay, Extra> child, IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.child = child;
}
@Override
protected void abstractStart(AbstSesServ.Ses<TServCodeString, Extra> session) {
	SerializeSes s = new SerializeSes(session);
	sessions.put(session, s);
	child.start(s);
}
@Override
public void abstractClose(AbstSesServ.Ses<TServCodeString, Extra> sess) {
	child.close(sessions.get(sess));
	sessions.remove(sess);
}
@Override
protected void abstractMessage(AbstSesServ.Ses<TServCodeString, Extra> ses, TClientCodeReader code) {
	SerializeSes s = sessions.get(ses);
	child.message(s, cConv.convert(code));
}
private class SerializeSes extends AbstSesServ.Ses<TServerSay, Extra> {
	private final AbstSesServ.Ses<TServCodeString, Extra> sess;
	private SerializeSes(AbstSesServ.Ses<TServCodeString, Extra> sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void abstractSend(TServerSay data) {
		sess.send(sConv.convert(data));
	}
	@Override
	public void stop() {
		sess.stop();
	}
	@Override
	public Extra getExtra() {
		return sess.getExtra();
	}
}

}
