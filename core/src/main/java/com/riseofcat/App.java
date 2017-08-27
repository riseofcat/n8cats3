package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.n8cats.lib_gwt.ILog;

public class App {
static {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);

}

public static final ILog log = new ILog() {
	private static final String TAG = "n8cats";
	public String info(String s) {
		Gdx.app.log(TAG, s);
		return s;
	}
	public void error(String s) {
		Gdx.app.error(TAG, s);
	}
	public void warning(String s) {
		Gdx.app.error(TAG, s);
	}
	public void debug(String s) {
		info("debug " + s);//todo
		Gdx.app.debug(TAG, s);
	}
};

public static void breakpoint() {
	int a = 1+1;
}
}
