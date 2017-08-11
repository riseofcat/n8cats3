package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ConvertSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString, Extra> extends AbstSesServ<TClientCodeReader, TServCodeString, Extra> {
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final IConverter<TServerSay, TServCodeString> sConv;
private final Map<AbstSesServ.Ses, AbstSesServ<TClientSay, TServerSay, Extra>.Ses> sessions = new ConcurrentHashMap<>();
private final AbstSesServ<TClientSay, TServerSay, Extra> child;
public ConvertSesServ(AbstSesServ<TClientSay, TServerSay, Extra> child, IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.child = child;
}
@Override
public void start(Ses session) {
	AbstSesServ<TClientSay, TServerSay, Extra>.Ses s = child.new Ses() {
		@Override
		public int getId() {
			return session.getId();
		}
		@Override
		public void stop() {
			session.stop();
		}
		@Override
		public void send(TServerSay data) {
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
public void close(Ses sess) {
	child.close(sessions.get(sess));
	sessions.remove(sess);
}
@Override
public void message(Ses ses, TClientCodeReader code) {
	child.message(sessions.get(ses), cConv.convert(code));
}

}
