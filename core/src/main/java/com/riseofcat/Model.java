package com.riseofcat;
import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.redundant.ServerSayS;

public class Model {
final static boolean LOCAL = true;
private final PingClient<ServerPayload, ClientPayload> client;
public Model() {
	if(LOCAL) {
		client = new PingClient("localhost", 5000, "socket", ServerSayS.class);
	} else {
		client = new PingClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
	}
	client.incoming.add(new SignalListener<ServerPayload>() {
		public void onSignal(ServerPayload arg) {
		}
	});
	client.say(new ClientPayload());
	Integer latency = client.latency;
}
public void touch(float x, float y) {

}
public void update(float deltaTime) {

}
public Logic.State getCurrentState() {

}
public void dispose() {
	client.close();
}
}
