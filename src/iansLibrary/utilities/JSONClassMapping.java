package iansLibrary.utilities;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.queries.Query;

public final class JSONClassMapping {
		
		public static Object jsonFieldToObject(String key, Object jsonVal) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Object toReturn = null;
			
			String type = key.split("#")[1];
			Class typeClass = Class.forName(type);
			if (type.substring(0, 2).equals("[L")) {
				toReturn = Array.newInstance(typeClass, ((JSONArray) jsonVal).size());
				
				for (int i = 0; i < ((JSONArray) jsonVal).size(); i ++) {
					Object javaVal = jsonAnyToObject(i);
					Array.set(toReturn, i, javaVal);
				}
			}
			
			return toReturn;
		}
		
		public static Object jsonAnyToObject(Object jsonVal) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Object toReturn = null;
			jsonVal = verifyType(jsonVal);
			
			if (jsonVal == null) {
				toReturn = null;
			} else if (jsonVal.getClass().isPrimitive() || jsonVal instanceof String || isWrapper(jsonVal)) {
				toReturn = jsonVal;
			} else if (jsonVal instanceof JSONArray) {
				toReturn = jsonArrayToArray((JSONArray) jsonVal);
			} else if (jsonVal instanceof JSONObject) {
				toReturn = jsonObjectToObject((JSONObject) jsonVal);
			}
			return toReturn;
		}
		
		public static Object jsonObjectToObject(JSONObject jsonVal) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Object toReturn = null;
			
			//figure out how to see if a class is a subclass of Query
			if (Query.class.isAssignableFrom(Class.forName((String) jsonVal.get("type")))) {
				jsonVal.put("1#java.lang.String#_dbName", Settings.database.dbName);
			}
			
			String className = (String) jsonVal.get("type");
			Object[] paramValues = new Object[jsonVal.keySet().size()-1];
			Class[] paramTypes = new Class[jsonVal.keySet().size()-1];
			int i = 0;
			for (Object key : jsonVal.keySet()) {
				if (!((String) key).equals("type")) {
					//set value at key to attribute of toReturn.
					i = Integer.valueOf(((String) key).split("#")[0]) - 1;
					Class type = Class.forName(((String) key).split("#")[1]);
					Object value = null;
					
					if (type.equals(Class.class)) {
						value = Class.forName((String) jsonVal.get(key));
					} else {
						value = jsonAnyToObject(jsonVal.get(key));
					}
					
					paramTypes[i] = type;
					paramValues[i] = value;
				}
			}
			
			Constructor c = Class.forName(className).getConstructor(paramTypes);
			try {
				toReturn = c.newInstance(paramValues);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				for (Object object : paramValues) {
				}
				e.printStackTrace();
			}
			
			return toReturn;
		}
		
		public static Object jsonArrayToArray(JSONArray jsonVal) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Object toReturn = null;
			
			String dataType = jsonVal.get(0).toString();
			Class typeClass = Class.forName(dataType);
			toReturn = Array.newInstance(typeClass, jsonVal.size() - 1);
			
			for (int i = 1; i < jsonVal.size(); i ++) {
				Object javaVal = jsonAnyToObject(jsonVal.get(i));
				try {
					Array.set(toReturn, i-1, javaVal);
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
			
			return toReturn;
		}
		
		private static Set<Class<?>> getWrapperTypes()
	    {
	        Set<Class<?>> ret = new HashSet<Class<?>>();
	        ret.add(Boolean.class);
	        ret.add(Character.class);
	        ret.add(Byte.class);
	        ret.add(Short.class);
	        ret.add(Integer.class);
	        ret.add(Long.class);
	        ret.add(Float.class);
	        ret.add(Double.class);
	        ret.add(Void.class);
	        return ret;
	    }
		
		public static boolean isWrapper(Object o){
			if (getWrapperTypes().contains(o.getClass())) {
				return true;
			}
			else {
				return false;
			}
		}
		
		public static Object verifyType(Object o) {
			Object retVal = null;
			
			if (o.getClass().equals(Long.class) && (Long) o < Integer.MAX_VALUE) {
				retVal = (Integer) ((Long) o).intValue();
			} else {
				retVal = o;
			}
			
			return retVal;
		}

}
