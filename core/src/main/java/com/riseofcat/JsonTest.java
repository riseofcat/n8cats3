package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.lib_gwt.ILog;
import com.n8cats.share.ForJsonTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class JsonTest {
public static final Json json = new Json();
public static void main(String[] args) {
	test(null);
}
public static void test(ILog log) {
	ForJsonTest test = new ForJsonTest();
	test.testStr = "testStrValue";
	test.mapStrStr = new HashMap<>();
	test.mapStrStr.put("key1", "value1");
	test.mapIntStr = new HashMap<>();
	test.mapIntStr.put(1, "One");
	test.mapIntStr.put(2, "Two");
	test.treeMapIntInt = new TreeMap<>();
	test.treeMapIntInt.put(1, 10);
	test.treeMapIntInt.put(3, 20);
	test.treeMapIntInt.put(2, 30);
	test.list = new ArrayList<>();
	test.list.add("One");
	test.list.add("Two");

	String s = json.toJson(test);
	ForJsonTest result = json.fromJson(ForJsonTest.class, s);
	if(!json.toJson(result).equals(s)) {
		App.breakpoint();
	}
	if(log != null) {
		log.info(json.toJson(result));
	}
}

}
