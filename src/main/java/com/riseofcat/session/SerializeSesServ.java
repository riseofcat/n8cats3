package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class SerializeSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString, Extra> extends AbstSesServ<TClientCodeReader, TServCodeString, Extra> {
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final IConverter<TServerSay, TServCodeString> sConv;
private final Map<AbstSesServ.Ses, AbstSesServ<TClientSay, TServerSay, Extra>.Ses> sessions = new ConcurrentHashMap<>();
private final AbstSesServ<TClientSay, TServerSay, Extra> child;
public SerializeSesServ(AbstSesServ<TClientSay, TServerSay, Extra> child, IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.child = child;
}
@Override
protected void abstractStart(Ses session) {
	AbstSesServ<TClientSay, TServerSay, Extra>.Ses s = child.new Ses(session.id) {
		@Override
		public void stop() {
			session.stop();
		}
		@Override
		protected void abstractSend(TServerSay data) {
			session.send(sConv.convert(data));
		}
		@Override
		public Extra getExtra() {
			return session.getExtra();
		}
	};
	sessions.put(session, s);
	child.start(s);
}
@Override
public void abstractClose(Ses sess) {
	child.close(sessions.get(sess));
	sessions.remove(sess);
}
@Override
protected void abstractMessage(Ses ses, TClientCodeReader code) {
	child.message(sessions.get(ses), cConv.convert(code));
}

}
