package com.n8cats.share;

import java.io.Reader;
public interface IStringSerializer<T> {
T fromStr(String str);
T fromStr(Reader reader);
String toStr(T t);
}
