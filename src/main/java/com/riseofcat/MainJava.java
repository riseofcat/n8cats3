package com.riseofcat;
import com.n8cats.lib_gwt.Const;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.share.Share;

import static spark.Spark.*;//http://sparkjava.com/documentation

public class MainJava {
    public static void main(String[] args) {
        String port = System.getenv("PORT");
        if(port != null && port.length() > 0) {
            port(Integer.parseInt(port));
        } else {
            port(5000);
        }
        if(false) {//todo
            threadPool(10, 2, 30*1000);
        }
        staticFiles.location("/public");
        //staticFileLocation("/public");
        staticFiles.expireTime(600);
        //https://github.com/tipsy/spark-websocket
        webSocket("/socket", EchoWebSocket.class);
        init();
        get("/", (req, res) -> "Hello from Java" + Share.testShare() + Const.ACTION);
        if(false) {
            stop();
        }
    }
}
