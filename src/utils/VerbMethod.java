package src.utils;

import java.util.HashMap;
import java.lang.reflect.Method;
public class VerbMethod extends HashMap<String, Method>{
    @Override
	public Method put(String key, Method value) throws IllegalArgumentException {
		expectError(key, value);
		return super.put(key, value); 
	}

	public boolean isValableKey(String key) {
		switch (key) {
			case "GET": 
			case "POST":
				return true;
			default:
				return false;
		}
	}

	public Class<?> getDeclaringClass(String key){
		return get(key).getDeclaringClass();
	}

	public void expectError(String key, Method value) throws IllegalArgumentException {
		if (!isValableKey(key)) {
			throw new IllegalArgumentException("Verb '" + key + "'' not allowed . only 'GET','POST' for now");
		} else if (containsKey(key)) {
			throw new IllegalArgumentException("Verb '" + key + " already exists");
		}
	}
	
}
