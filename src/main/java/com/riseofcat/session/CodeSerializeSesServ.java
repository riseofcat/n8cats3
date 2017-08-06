package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class CodeSerializeSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString> extends AbstSesServ<TClientCodeReader, TServCodeString> {
private final AbstSesServ<TClientSay, TServerSay> server;
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final Map<AbstSesServ.Ses, Ses> sessions = new ConcurrentHashMap<>();
private final IConverter<TServerSay, TServCodeString> sConv;
public CodeSerializeSesServ(AbstSesServ<TClientSay, TServerSay> server, IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.server = server;
	this.cConv = cConv;
	this.sConv = sConv;
}
@Override
protected void abstractStart(AbstSesServ.Ses<TServCodeString> session) {
		Ses s = new Ses(session);
		sessions.put(session, s);
		server.abstractStart(s);
}
@Override
public void abstractClose(AbstSesServ.Ses sess) {
	server.abstractClose(sessions.get(sess));
	sessions.remove(sess);
}
@Override
protected void abstractMessage(AbstSesServ.Ses<TServCodeString> ses, TClientCodeReader code) {
	handleMessage(ses, cConv.convert(code));
}
protected final void handleMessage(AbstSesServ.Ses<TServCodeString> sess, TClientSay say) {
	Ses s = sessions.get(sess);
	server.abstractMessage(s, say);
}
private class Ses extends AbstSesServ.Ses<TServerSay> {
	private final AbstSesServ.Ses<TServCodeString> sess;
	private Ses(AbstSesServ.Ses<TServCodeString> sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void send(TServerSay data) {
		sess.send(sConv.convert(data));
	}
	@Override
	public void stop() {
		sess.stop();
	}
}
}
