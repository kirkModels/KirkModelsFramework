package com.ianmann.database.orm.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.scema.fields.SavableField;

public abstract class Utilities {

	public static JSONObject json(String jsonString) throws ParseException{
		JSONObject jsonObject = null;
		JSONParser parser = new JSONParser();
		jsonObject = ((JSONObject) parser.parse(jsonString));
		return jsonObject;
	}
	
	public static JSONObject json(File jsonFile) throws FileNotFoundException, ParseException{
		Scanner jsonReader = new Scanner(jsonFile).useDelimiter("\\A");
		return json(jsonReader.next());
	}
	
	public static Model instantiateDbObject(HashMap<String, Object> kwargs){
		Class<?> table = (Class<?>) kwargs.get("table_label");
		Model instance = null;
		try {
			instance = (Model) table.newInstance();
			for(String key : kwargs.keySet()){
				if(!key.equals("table_label")){
					((SavableField) instance.getClass().getField(key).get(instance)).set(kwargs.get(key));
				}
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return instance;
	}
}
