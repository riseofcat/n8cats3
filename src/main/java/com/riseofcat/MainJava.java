package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n8cats.lib.LibAll;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;//import static spark.Spark.*;//http://sparkjava.com/documentation

public class MainJava {
public static void main(String[] args) {
	String port = java.lang.System.getenv("PORT");
	if(port != null) {
		Spark.port(Integer.parseInt(port));
	} else {
		Spark.port(5000);
	}
	if(false) {//todo
		Spark.threadPool(10, 2, 30 * 1000);
	}
	Spark.staticFiles.location("/public");
	Spark.staticFiles.expireTime(600);
	//https://github.com/tipsy/spark-websocket
	Spark.webSocket("/socket", EchoWebSocket.class);
	Spark.init();//Spark.stop();
	Spark.get("/", new Route() {
		@Override
		public Object handle(Request request, Response response) {
			return LibAll.JSON.toPrettyStr(App.info);
//			return new StringBuilder().append("maxMemoty = ")
//					.append(App.info.getMaxMemory())
//					.append("\n<br/>usedMemory = ")
//					.append(App.info.getUsedMemoty());
		}
	});
}
}
