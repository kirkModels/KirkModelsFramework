package com.ianmann.database.orm.queries;

public class InsertValue {

	public String fieldName;
	public Object value;
	
	public InsertValue(String _fieldName, Object _value){
		this.fieldName = _fieldName;
		this.value = _value;
	}
	
	public static String sqlStr(Object val){
		if (val.getClass().equals(String.class)) {
			return "'" + val + "'";
		} else {
			return val.toString();
		}
	}
	
	public String toString(){
		return "( " + this.fieldName + " ) VALUE ( " + this.value + " )";
	}
}
