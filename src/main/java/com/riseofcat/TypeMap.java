package com.riseofcat;

import java.util.HashMap;
import java.util.Map;

public class TypeMap {
private Map<Class<?>, Object> favorites = new HashMap<>();

public <T> void put(T value) {
	favorites.put(value.getClass(), value);
}

public <T> T get(Class<T> clazz) {
	return clazz.cast(favorites.get(clazz));
}

}