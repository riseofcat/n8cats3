package com.riseofcat;
import static spark.Spark.*;//http://sparkjava.com/documentation

public class MainJava {
    public static void main(String[] args) {
        String port = System.getenv("PORT");
        if(port != null && port.length() > 0) {
            port(Integer.parseInt(port));
        }
        staticFileLocation("/public");
        get("/", (req, res) -> "Hello from Java");

        if(false) {
            webSocket("socket", EchoWebSocket.class);//todo damn, it doesn't work!
            init();
        }
        if(false) {
            stop();
        }
    }
}
