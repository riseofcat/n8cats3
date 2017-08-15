package com.n8cats.share;

abstract public class IntHashStrEquals {
abstract protected int getInt();
public int hashCode() {
	return toString().hashCode();
}
public boolean equals(Object o) {
	return o == this || o != null && ( /*((IntHashStrEquals) o).getInt() == getInt() ||*/ toString().equals(o.toString()));
}
public String toString() {
	return String.valueOf(getInt());
}
}
