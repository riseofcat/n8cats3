package com.riseofcat;

import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.ServerPayload;

public class ConcreteRoomsServer extends RoomsServer<ClientPayload, ServerPayload, PingPongServ.ExtraLatency<CountSesServ.ExtraCount<Void>>> {
public ConcreteRoomsServer(SignalListener<Room> onRoomAdded) {
	super(onRoomAdded);
}
public ConcreteRoomsServer() {
}
}
