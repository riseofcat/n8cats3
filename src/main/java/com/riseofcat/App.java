package com.riseofcat;

import com.n8cats.lib_gwt.ILog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
private static Logger logger = LoggerFactory.getLogger("main");
public static ILog log = new ILog() {
	@Override
	public String info(String s) {
		logger.info(s);
		System.out.println(s);
		return s;
	}
	@Override
	public void error(String s) {
		logger.error(s);
		System.out.println("error " + s);
	}
	@Override
	public void warning(String s) {
		logger.warn(s);
	}
};

}
