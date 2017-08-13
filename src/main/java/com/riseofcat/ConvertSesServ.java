package com.riseofcat;
import com.n8cats.lib.TypeMap;
import com.n8cats.lib_gwt.IConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConvertSesServ<CSay, SSay, CCode, SCode> extends AbstSesServ<CCode, SCode> {
private final IConverter<CCode, CSay> cConv;
private final IConverter<SSay, SCode> sConv;
private final Map<AbstSesServ.Ses, AbstSesServ<CSay, SSay>.Ses> map = new ConcurrentHashMap<>();
private final AbstSesServ<CSay, SSay> server;
public ConvertSesServ(AbstSesServ<CSay, SSay> server, IConverter<CCode, CSay> cConv, IConverter<SSay, SCode> sConv) {
	this.cConv = cConv;
	this.sConv = sConv;
	this.server = server;
}
public void start(Ses session) {
	AbstSesServ<CSay, SSay>.Ses s = server.new Ses() {
		public int getId() {
			return session.getId();
		}
		public void stop() {
			session.stop();
		}
		public void send(SSay data) {
			session.send(sConv.convert(data));
		}
		protected TypeMap getTypeMap() {
			return session.getTypeMap();
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
