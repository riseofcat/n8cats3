package com.n8cats.lib_gwt;

import java.io.Reader;
public interface IStrSerialize<T> {
T fromStr(String str);
T fromStr(Reader reader);
String toStr(T t);
}
