package com.riseofcat;

import com.n8cats.lib.LibAll;
import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientSayC;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerSayS;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
//import static spark.Spark.*;
//http://sparkjava.com/documentation

public class MainJava {
public static void main(String[] args) {
	String port = java.lang.System.getenv("PORT");
	Spark.port(port != null ? Integer.parseInt(port) : 5000);
	if(false) {
		Spark.threadPool(30, 2, 30_000);
		Spark.webSocketIdleTimeoutMillis(30_000);
	}
	Spark.staticFiles.location("/public");
	Spark.staticFiles.expireTime(600);
	JsonSerializer serializer = new JsonSerializer(ClientSayC.class, ServerSayS.class);
	RoomsServer roomsServer = new RoomsServer();
	roomsServer.onRoomCreated.add(new SignalListener<RoomsServer.Room>() {
		@Override
		public void onSignal(RoomsServer.Room room) {
			App.log.info("room created");
			new ServerTickGameInRoom(room, new Logic());
		}
	});
	StringSerializedRealTimeServer stringSerialized = new StringSerializedRealTimeServer(roomsServer, 1000, serializer);
	Spark.webSocket("/socket", new SparkWebSocket(stringSerialized));
	Spark.get("/", new Route() {
		@Override
		public Object handle(Request request, Response response) {
			return LibAll.JSON.toPrettyStr(App.info);
		}
	});
	Spark.init();//Spark.stop();
}
}
