package iansLibrary.utilities;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.ianmann.database.orm.queries.Query;

public final class ObjectParser {
	
	public static <T extends JSONMappable> JSONObject objectToJSON(T object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		JSONObject json = new JSONObject();
		
		json.put("type", object.getClass().getName());
		
		int i = 0;
		if (object instanceof Query) {
			i = 1;
		}
		while (i < object.getConstructorFieldOrder().length) {
			Field f = null;
			try{
				f = object.getClass().getField(object.getConstructorFieldOrder()[i]);
			} catch (NoSuchFieldException e) {
				throw e;
			}
			Class type = object.getJsonConstructor().getParameterTypes()[i];
			String key = String.valueOf(i+1) + "#" + type.getName() + "#_" + f.getName();
			Object value = f.get(object);
			
			Object jsonValue = anyObjectToJSON(value);
			
			if (jsonValue.getClass().equals(HashMap.class)) {
				Object tmp = ((HashMap) jsonValue).get(type.getName());
				jsonValue = tmp;
			}
			
			json.put(key, jsonValue);
			i ++;
		}
		
		return json;
	}
	
	public static <T extends JSONMappable> JSONArray arrayToJSON(Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		JSONArray json = new JSONArray();
		
		json.add(object.getClass().getName().substring(2).replace(";", ""));
		
		for (int i = 0; i < Array.getLength(object); i++) {
			Object jsonElement = anyObjectToJSON(Array.get(object, i));
			
			if (jsonElement.getClass().equals(HashMap.class)) {
				jsonElement = ((HashMap) jsonElement).get(Class.forName(object.getClass().getName().substring(2).replace(";", "")));
			}
			
			json.add(jsonElement);
		}
		
		return json;
	}
	
	public static Object primitiveToJSON(Object object) {
		Object json = null;
		
		if (object instanceof Class) {
			json = ((Class) object).getName();
		} else {
			json = object;
		}
		
		return json;
	}
	
	public static Object anyObjectToJSON(Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		Object json = null;
		
		if (object == null) {
			json = new HashMap<String, Object>();
			((HashMap) json).put(String.class.getName(), "null-value");
			((HashMap) json).put(Integer.class.getName(), Integer.MIN_VALUE);
		} else if (object instanceof JSONMappable) {
			json = objectToJSON((JSONMappable) object);
		} else if (object.getClass().isArray()) {
			json = arrayToJSON(object);
		} else {
			json = primitiveToJSON(object);
		}
		
		return json;
	}

}
