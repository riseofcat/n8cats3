package com.n8cats.lib_gwt;

public interface IConverter<From,To> {
To convert(From obj);
}
