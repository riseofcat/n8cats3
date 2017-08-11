package com.riseofcat.session;

import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ConvertSesServ<TClientSay, TServerSay, TClientCodeReader, TServCodeString, Extra> extends AbstSesServ<TClientCodeReader, TServCodeString, Extra> {
private final IConverter<TClientCodeReader, TClientSay> cConv;
private final IConverter<TServerSay, TServCodeString> sConv;
private final Map<AbstSesServ.Ses, AbstSesServ<TClientSay, TServerSay, Extra>.Ses> map = new ConcurrentHashMap<>();
private final AbstSesServ<TClientSay, TServerSay, Extra> server;
public ConvertSesServ(AbstSesServ<TClientSay, TServerSay, Extra> child, IConverter<TClientCodeReader, TClientSay> cConv, IConverter<TServerSay, TServCodeString> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.server = child;
}
@Override
public void start(Ses session) {
	AbstSesServ<TClientSay, TServerSay, Extra>.Ses s = server.new Ses() {
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
	map.put(session, s);
	server.start(s);
}
@Override
public void close(Ses sess) {
	server.close(map.get(sess));
	map.remove(sess);
}
@Override
public void message(Ses ses, TClientCodeReader code) {
	server.message(map.get(ses), cConv.convert(code));
}

}
