package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public abstract class SerializeSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString, Extra> extends AbstSesServ<TClientCodeReader, TServCodeString, Extra> {
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final IConverter<TServerSay, TServCodeString> sConv;
private final Map<AbstSesServ.Ses, SerializeSes> sessions = new ConcurrentHashMap<>();
public SerializeSesServ(IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
}
@Override
protected void abstractStart(AbstSesServ.Ses<TServCodeString, Extra> session) {
	SerializeSes s = new SerializeSes(session);
	sessions.put(session, s);
	abstractStart2(s);
}
abstract protected void abstractStart2(AbstSesServ.Ses<TServerSay, Extra> session);
@Override
public void abstractClose(AbstSesServ.Ses sess) {
	abstractClose2(sessions.get(sess));
	sessions.remove(sess);
}
abstract protected void abstractClose2(AbstSesServ.Ses<TServerSay, Extra> serializeSes);
@Override
protected void abstractMessage(AbstSesServ.Ses<TServCodeString, Extra> ses, TClientCodeReader code) {
	handleMessage(ses, cConv.convert(code));
}
protected final void handleMessage(AbstSesServ.Ses<TServCodeString, Extra> sess, TClientSay say) {
	SerializeSes s = sessions.get(sess);
	abstractMessage2(s, say);
}
protected abstract void abstractMessage2(AbstSesServ.Ses<TServerSay, Extra> s, TClientSay say);

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
