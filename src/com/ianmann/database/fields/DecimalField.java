package com.ianmann.database.fields;

import java.lang.reflect.Constructor;
import java.sql.Types;
import java.util.HashMap;

import com.ianmann.database.config.Settings;
import com.ianmann.database.utils.exceptions.LanguageNotSupportedError;

import iansLibrary.data.databases.MetaTableColumn;
import iansLibrary.utilities.JSONMappable;

public class DecimalField extends SavableField<Float> implements JSONMappable {
	
	/**
	 * Represents number of significant figures that can be stored in
	 * the values of this field.
	 */
	public Integer precision;
	
	/**
	 * Represents number of digits that can be stored after the decimal point
	 * in the values of this field.
	 */
	public Integer scale;

	/**
	 * A field that stores fixed point data for decimal numbers. This field can be saved to the database and will allow 10
	 * significant figures and 30 digits after the decimal point.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 */
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Float _defaultValue) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = 10;
		this.scale = 30;
		
		this.PSQL_TYPE = "numeric";
		this.MYSQL_TYPE = "DECIMAL";
	}

	/**
	 * A field that stores fixed point data for decimal numbers. This field can be saved to the database and will allow
	 * as many significant figures as is indicated in _precision and 30 digits after the decimal point.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 * @param _precision - Number of significant digits allowed in the value
	 */
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Integer _precision, Float _defaultValue) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = _precision;
		this.scale = 30;
	}

	/**
	 * A field that stores fixed point data for decimal numbers. This field can be saved to the database and will allow 
	 * 10 significant figures and as many digits after the decimal point as stored in
	 * _scale.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 * @param _scale - Number of digits allowed after the decimal point
	 */
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Float _defaultValue, Integer _scale) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = 10;
		this.scale = _scale;
	}

	/**
	 * A field that stores fixed point data for decimal numbers. This field can be saved to the database and will allow 
	 * as many significant figures as is indicated in _precision and as many digits after the decimal point as stored in
	 * _scale.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 * @param _precision - Number of significant digits allowed in the value
	 * @param _scale - Number of digits allowed after the decimal point
	 */
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Integer _precision, Integer _scale, Float _defaultValue) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = _precision;
		this.scale = _scale;
	}
	
	public DecimalField() {
		super("", true, false, null);
		this.precision = 10;
		this.scale = 30;
	}
	
	@Override
	public Constructor getJsonConstructor(){
		Class[] paramTypes = new Class[]{
				String.class,
				Boolean.class,
				Boolean.class,
				Integer.class,
				Integer.class,
				Float.class
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

	@Override
	public String getMySqlDefinition() {
		String def = this.MYSQL_TYPE;
		
		def = def + "(" + this.precision + "," + this.scale + ")";
		
		if(!this.isNull){
			def = def + " NOT NULL";
		}
		if (this.defaultValue != null) {
			def = def + " DEFAULT " + this.defaultValue;
		}
		
		return def;
	}

	@Override
	public String getPsqlDefinition() {
		String def = this.PSQL_TYPE;
		
		def = def + "(" + this.precision + "," + this.scale + ")";
		
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

	@Override
	public int getReturnedSqlDefinition() {
		// TODO Auto-generated method stub
		if (Settings.database.language.equals(Settings.database.MYSQL)) {
			return Types.DECIMAL;
		} else if (Settings.database.language.equals(Settings.database.POSTRESQL)) {
			return Types.NUMERIC;
		} else {
			throw new LanguageNotSupportedError(Settings.database.language);
		}
	}
	
	@Override
	public String getTypeString(int _type) {
		switch (_type) {
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.NUMERIC:
			return "numeric";
		default:
			return null;
		}
	}

	@Override
	public boolean equals(MetaTableColumn _column) {
		if (!this.isSameColumn(_column)) {
			return false;
		}
		if (this.getDifferenceNullable(_column) != null) {
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
		if (this.label.equals(_column.getColumnName()) && this.getReturnedSqlDefinition() == _column.getDataType()) {
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
		}
		try {
			this.getDefaultValueDifference(_column);
			diffs.put("default", this.getDefaultValueDifference(_column));
		} catch (NoSuchFieldException e) {//if it throws the exception, that means they are the same.
		}
		
		return diffs;
	}
	

	
	public Float getDefaultValueDifference(MetaTableColumn _column) throws NoSuchFieldException {
		if (this.defaultValue == null) {
			if (_column.getDefaultValue() != null) {
				return Float.valueOf((String) _column.getDefaultValue());
			} else {
				throw new NoSuchFieldException("The two default values are the same.");
			}
		} else {
			if (_column.getDefaultValue() == null) {
				return this.defaultValue;
			} else {
				if (Float.valueOf((String) _column.getDefaultValue()).equals(this.defaultValue)) {
					throw new NoSuchFieldException("The two default values are the same.");
				} else {
					return Float.valueOf((String) _column.getDefaultValue());
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
