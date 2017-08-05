package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;
import com.riseofcat.AbstractTypedServ;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class CodeSerializeSesServ<C, S, CCoded, SCoded> extends SesServ<CCoded, SCoded> {
private final AbstractTypedServ<C, S> server;
private final IConverter<CCoded, C> cConv;
private final Map<SesServ.Ses, Ses> sessions = new ConcurrentHashMap<>();
private final IConverter<S, SCoded> sConv;
public CodeSerializeSesServ(AbstractTypedServ<C, S> server, IConverter<CCoded, C> cConv, IConverter<S, SCoded> sConv) {
	this.server = server;
	this.cConv = cConv;
	this.sConv = sConv;
}
@Override
protected void abstractStart(SesServ.Ses<SCoded> session) {
		Ses s = new Ses(session);
		sessions.put(session, s);
		server.start(s);
}
@Override
protected void abstractMessage(SesServ.Ses ses, CCoded code) {
	handleMessage(ses, cConv.convert(code));
}
@Override
public void abstractClose(SesServ.Ses sess) {
	server.close(sessions.get(sess));
	sessions.remove(sess);
}
protected final void handleMessage(SesServ.Ses sess, C say) {
	Ses s = sessions.get(sess);
	server.message(s, say);
}
private class Ses extends AbstractTypedServ.Ses<S> {
	private final SesServ.Ses<SCoded> sess;
	private Ses(SesServ.Ses<SCoded> sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void send(S data) {
		sess.send(sConv.convert(data));
	}
	@Override
	public void stop() {
		sess.stop();
	}
}
}
