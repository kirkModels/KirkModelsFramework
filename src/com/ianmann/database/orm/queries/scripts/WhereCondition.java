package com.ianmann.database.orm.queries.scripts;

import java.util.ArrayList;
import java.util.HashMap;

import com.ianmann.database.config.Settings;

public class WhereCondition {

	public static final int EQUALS = 1;
	public static final int CONTAINED_IN = 2;
	public static final int NOT_CONTAINED_IN = 3;
	public static final int GREATER_THAN = 4;
	public static final int LESS_THAN = 5;
	public static final int GREATER_THAN_OR_EQUAL = 6;
	public static final int LESS_THAN_OR_EQUAL = 7;
	public static final int NOT_EQUAL_TO = 8;
	
	public static HashMap<Integer, String> types = new HashMap<Integer, String>(){{
		put(EQUALS, "=");
		put(CONTAINED_IN, " IN ");
		put(NOT_CONTAINED_IN, " NOT IN ");
		put(GREATER_THAN, ">");
		put(LESS_THAN, "<");
		put(GREATER_THAN_OR_EQUAL, ">=");
		put(LESS_THAN_OR_EQUAL, "<=");
		put(NOT_EQUAL_TO, "!=");
	}};
	
	public String fieldName;
	public int type;
	public Object value;
	
	public WhereCondition(String _fieldName, int _type, Object _value){
		this.fieldName = _fieldName;
		this.type = _type;
		this.value = _value;
	}
	
	public String getValueString(){
		if(this.value == null) {
			return "NULL";
		}
		if (this.value.getClass().equals(String.class)) {
			return "'" + this.value + "'";
		} else if (ArrayList.class.isAssignableFrom(this.value.getClass())) {
			String valueString = listToString((ArrayList) this.value);
			
			return valueString;
		} else {
			return this.value.toString();
		}
	}
	
	public static String listToString(ArrayList list){
		String valueString = "( ";
		
		for (Object val : list) {
			valueString = valueString + sqlStr(val);
			
			if (list.indexOf(val) < list.size() - 1) {
				valueString = valueString + ", ";
			}
		}
		
		valueString = valueString + " )";
		
		return valueString;
	}
	
	public static String sqlStr(Object val){
		if (val.getClass().equals(String.class)) {
			return "'" + val + "'";
		} else {
			return val.toString();
		}
	}
	
	public String getMySqlString(){
		String sql = this.fieldName + WhereCondition.types.get(this.type) + this.getValueString();
		return sql;
	}
	
	public String getPsqlString(){
		String sql = this.fieldName + WhereCondition.types.get(this.type) + this.getValueString();
		return sql;
	}
	
	public String toString(){
		String language = Settings.database.language;
		
		String sql = "";
		
		switch (language) {
		case "MySQL":
			
			sql = this.getMySqlString();
			break;
			
		case "postgreSQL":
			
			sql = this.getPsqlString();
			break;

		default:
			
			sql = "No default language.";
			break;
		}
		
		return sql;
	}
}
