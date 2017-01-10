package com.ianmann.database.fields;

import java.lang.reflect.Constructor;
import java.sql.Types;
import java.util.HashMap;

import com.ianmann.database.config.Settings;

import iansLibrary.data.databases.MetaTableColumn;
import iansLibrary.utilities.JSONMappable;

public class IntegerField extends SavableField<Integer> implements JSONMappable {

	public Integer maxVal;
	
	/**
	 * A field that, when called, will return an {@link Integer}. This field can be saved to a database and, depending on the maxValue parameter, will be saved as a TINYINT, SMALLINT, or a MEDIUMINT in SQL.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 * @param autoIncrement - whether this field will automatically increment
	 * @param _maxVal - the maximum value that this field is allowed to be
	 */
	public IntegerField(String _label, Boolean _isNull, Integer _defaultValue, Boolean _unique, Integer _maxVal) {
		super(_label, _isNull, _unique, _defaultValue);
		
		if (_defaultValue == null || _defaultValue == Integer.MIN_VALUE) {
			this.defaultValue = null;
			this.value = null;
		} else {
			this.value = _defaultValue;
		}
		if (_maxVal == null) {
			this.maxVal = 100000;
		} else {
			this.maxVal = _maxVal;
		}
		this.JAVA_TYPE = Integer.class;
		
		this.MYSQL_TYPE = this.getMySqlIntType(_maxVal);
		this.PSQL_TYPE = this.getPsqlIntType(_maxVal);
	}
	
	public IntegerField() {
		super("", true, false, null);
	}
	
	@Override
	public Constructor getJsonConstructor(){
		Class[] paramTypes = new Class[]{
				String.class,
				Boolean.class,
				Integer.class,
				Boolean.class,
				Integer.class
		};
		try {
			return this.getClass().getConstructor(paramTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String[] getConstructorFieldOrder() {
		return new String[]{
				"label",
				"isNull",
				"defaultValue",
				"unique",
				"maxVal"
		};
	}

	@Override
	public String MySqlString() {
		String sql = this.label + " " + this.getMySqlDefinition();
		return sql;
	}
	
	@Override
	public String PSqlString() {
		String sql = this.label + " " + this.getPsqlDefinition();
		return sql;
	}
	
	public String getMySqlIntType(Integer maxVal) {
		if(maxVal != null){
			if(maxVal <= 127){
				return "TINYINT";
			}
			else if(maxVal <= 32767){
				return "SMALLINT";
			}
			else if (maxVal <= 8388607) {
				return "MEDIUMINT";
			}
			else{  // if (maxVal <= 2147483647)
				return "INT";
			}
		}
		else {
			return "INT";
		}
	}
	
	public String getMySqlDefinition() {
		String def = this.MYSQL_TYPE;
		if(!this.isNull){
			def = def + " NOT NULL";
		}
		if(this.label.equals("id")){
			def = def + " PRIMARY KEY";
		}
		if (this.defaultValue != null) {
			def = def + " DEFAULT " + this.defaultValue;
		}
		
		return def;
	}
	
	public String getPsqlDefinition() {
		String def = this.PSQL_TYPE;
		if(!this.isNull){
			def = def + " NOT NULL";
		}
		if (this.defaultValue != null) {
			def = def + " DEFAULT " + this.defaultValue;
		}
		if(this.label.equals("id")){
			def = def + " PRIMARY KEY";
		}
		
		return def;
	}
	
	/**
	 * @Override
	 * When querying the database for int type, they come back
	 * as "int4", "int2" and other type like that. int2 is
	 * a smallint while int4 is an integer and so on.
	 * @return
	 */
	@Override
	public int getReturnedSqlDefinition() {
		String type = "";
		
		if (Settings.database.language.equals(Settings.database.MYSQL)) {
			type = this.MYSQL_TYPE;
		} else if (Settings.database.language.equals(Settings.database.POSTRESQL)) {
			type = this.PSQL_TYPE;
		}
		
		switch (type) {
		case "tinyint":
			return Types.TINYINT;
		case "smallint":
			return Types.SMALLINT;
		case "integer":
			return Types.INTEGER;
		case "int":
			return Types.INTEGER;
		case "bigint":
			return Types.BIGINT;
		default:
			return -100;
		}
	}
	
	public String getPsqlIntType(Integer maxVal) {
		if(maxVal != null){
			if(maxVal <= 32767){
				return "smallint";
			}
			else if(maxVal <= 2147483647){
				return "integer";
			}
			else {
				return "bigint";
			}
		}
		else {
			return "integer";
		}
	}

	@Override
	public boolean equals(MetaTableColumn _column) {
		if (this.getDifferenceNullable(_column) != null) {
			return false;
		} else if (this.getTypeDifference(_column) != null) {
			return false;
		}
		try {
			this.getDefaultValueDifference(_column);
			return false;
		} catch (NoSuchFieldException e) {
			return true; //if it throws the exception, that means they are the same.
		}
	}

	@Override
	public boolean isSameColumn(MetaTableColumn _column) {
		// TODO Auto-generated method stub
		if (!this.label.equals(_column.getColumnName())) {
			return false;
		}
		
		/*
		 * If the column is any type of int, we will say it's still the same type.
		 * We'll handle size in the equals method above.
		 */
		if (_column.getDataType() == Types.TINYINT
				||_column.getDataType() == Types.SMALLINT
				|| _column.getDataType() == Types.INTEGER
				|| _column.getDataType() == Types.BIGINT) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public HashMap<String, Object> getDifferences(MetaTableColumn _column) {
		HashMap<String, Object> diffs = new HashMap<String, Object>();
		
		if (this.getDifferenceNullable(_column) != null) {
			diffs.put("nullable", this.getDifferenceNullable(_column));
		} else if (this.getTypeDifference(_column) != null) {
			diffs.put("type", this.getTypeDifference(_column));
		}
		try {
			this.getDefaultValueDifference(_column);
			diffs.put("default", this.getDefaultValueDifference(_column));
		} catch (NoSuchFieldException e) {//if it throws the exception, that means they are the same.
		}
		
		return diffs;
	}
	
	@Override
	public String getTypeString(int _type) {
		switch(_type) {
		case Types.TINYINT:
			return "tinyint";
		case Types.SMALLINT:
			return "smallint";
		case Types.INTEGER:
			if (Settings.database.language.equals(Settings.database.MYSQL)) {
				return "int";
			} else if (Settings.database.language.equals(Settings.database.POSTRESQL)) {
				return "integer";
			} else {
				return "Unknown Language";
			}
		case Types.BIGINT:
			return "bigint";
		default:
			return "Invalid Identifyer";
		}
	}
	
	public String getTypeDifference(MetaTableColumn _column) {
		if (this.getReturnedSqlDefinition() == _column.getDataType()) {
			return null;
		} else {
			return this.getTypeString(_column.getDataType());
		}
	}
	
	public Integer getDefaultValueDifference(MetaTableColumn _column) throws NoSuchFieldException {
		if (this.defaultValue == null) {
			if (_column.getDefaultValue() != null) {
				return Integer.valueOf((String) _column.getDefaultValue());
			} else {
				throw new NoSuchFieldException("The two default values are the same.");
			}
		} else {
			if (_column.getDefaultValue() == null) {
				return this.defaultValue;
			} else {
				if (Integer.valueOf((String) _column.getDefaultValue()).equals(this.defaultValue)) {
					throw new NoSuchFieldException("The two default values are the same.");
				} else {
					return Integer.valueOf((String) _column.getDefaultValue());
				}
			}
		}
	}
	
	public Boolean getDifferenceNullable(MetaTableColumn _column) {
		if ((this.isNull.booleanValue() ? 1 : 0) != _column.getNullable()) {
			if (this.isNull) {
				return false;
			} else {
				return true;
			}
		} else {
			return null;
		}
	}

}
