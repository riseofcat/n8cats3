package com.riseofcat;
import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConvertSesServ<CSay, SSay, CCode, SCode, E> extends AbstSesServ<CCode, SCode, E> {
private final IConverter<CCode, CSay> cConv;
private final IConverter<SSay, SCode> sConv;
private final Map<AbstSesServ.Ses, AbstSesServ<CSay, SSay, E>.Ses> map = new ConcurrentHashMap<>();
private final AbstSesServ<CSay, SSay, E> server;
public ConvertSesServ(AbstSesServ<CSay, SSay, E> server, IConverter<CCode, CSay> cConv, IConverter<SSay, SCode> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.server = server;
}
public void start(Ses session) {
	AbstSesServ<CSay, SSay, E>.Ses s = server.new Ses() {
		public int getId() {
			return session.getId();
		}
		public void stop() {
			session.stop();
		}
		public void send(SSay data) {
			session.send(sConv.convert(data));
		}
		public E getExtra() {
			return session.getExtra();
		}
	};
	map.put(session, s);
	server.start(s);
}
public void close(Ses sess) {
	server.close(map.get(sess));
	map.remove(sess);
}
public void message(Ses ses, CCode code) {
	server.message(map.get(ses), cConv.convert(code));
}

}
