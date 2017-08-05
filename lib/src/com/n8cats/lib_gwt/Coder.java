package com.n8cats.lib_gwt;

public interface Coder<TCoded, TDecoded> {
TCoded encode(TDecoded data);
TDecoded decode(TCoded code);
}
