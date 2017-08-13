package com.riseofcat;

import java.util.HashMap;
import java.util.Map;

public class TypeMap {
private Map<Class<?>, Object> favorites = new HashMap<>();

public <T> void put(Class<T> clazz, T value) {
	favorites.put(clazz, value);
}

public <T> T get(Class<T> clazz) {
	return clazz.cast(favorites.get(clazz));
}

}