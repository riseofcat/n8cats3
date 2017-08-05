package com.riseofcat.session;

import com.n8cats.lib_gwt.IStrSerialize;
import com.riseofcat.AbstractTypedServ;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class TextSerializedSesServ<C, S, Text> extends StrSesServ {
private final AbstractTypedServ<C, S> server;
private final IStrSerialize<C> cSerializer;
private final IStrSerialize<S> sSerializer;
private final Map<Ses, Session> sessions = new ConcurrentHashMap<>();
public TextSerializedSesServ(AbstractTypedServ<C, S> server, IStrSerialize<C> cSerializer, IStrSerialize<S> sSerializer) {
	this.server = server;
	this.cSerializer = cSerializer;
	this.sSerializer = sSerializer;
}
@Override
public void abstractStart(Ses sess) {
	Session s = new Session(sess);
	sessions.put(sess, s);
	server.start(s);
}
@Override
public void abstractMessage(Ses sess, Reader reader) {
	handleMessage(sess, cSerializer.fromStr(reader));
}
@Override
public void abstractMessage(Ses sess, String message) {
	handleMessage(sess, cSerializer.fromStr(message));
}
@Override
public void abstractClose(Ses sess) {
	server.close(sessions.get(sess));
	sessions.remove(sess);
}
protected final void handleMessage(Ses sess, C say) {
	Session s = sessions.get(sess);
	server.message(s, say);
}
private class Session extends AbstractTypedServ.Ses<S> {
	private final Ses sess;
	private Session(Ses sess) {
		super(sess.id);
		this.sess = sess;
	}
	@Override
	public void send(S data) {
		String message = sSerializer.toStr(data);
		sess.send(message);
	}
	@Override
	public void stop() {
		sess.stop();
	}
}
}
