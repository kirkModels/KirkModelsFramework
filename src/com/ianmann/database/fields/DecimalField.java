package com.ianmann.database.fields;

import java.lang.reflect.Constructor;
import java.sql.Types;
import java.util.HashMap;

import com.ianmann.database.config.Settings;
import com.ianmann.database.utils.exceptions.LanguageNotSupportedError;

import iansLibrary.data.databases.MetaTableColumn;
import iansLibrary.utilities.JSONMappable;

public class DecimalField extends SavableField<Double> implements JSONMappable {
	
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
	 * significant figures and 3 digits after the decimal point.
	 * @param _label - The name given to this field
	 * @param _isNull - whether this field can be set as <b>null</b>
	 * @param _defaultValue - default value of this field if left null
	 * @param _unique - whether this field contains a unique constraint
	 */
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Double _defaultValue) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = 10;
		this.scale = 3;
		
		this.PSQL_TYPE = "numeric" + "(" + this.precision + "," + this.scale + ")";
		this.MYSQL_TYPE = "DECIMAL" + "(" + this.precision + "," + this.scale + ")";
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
	public DecimalField(String _label, Boolean _isNull, Boolean _unique, Integer _precision, Integer _scale, Double _defaultValue) {
		super(_label, _isNull, _unique, _defaultValue);
		// TODO Auto-generated constructor stub
		this.precision = _precision;
		this.scale = _scale;
		
		this.PSQL_TYPE = "numeric" + "(" + this.precision + "," + this.scale + ")";
		this.MYSQL_TYPE = "DECIMAL" + "(" + this.precision + "," + this.scale + ")";
	}
	
	public DecimalField() {
		super("", true, false, null);
	}
	
	@Override
	public Constructor getJsonConstructor(){
		Class[] paramTypes = new Class[]{
				String.class,
				Boolean.class,
				Boolean.class,
				Integer.class,
				Integer.class,
				Double.class
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
				"unique",
				"precision",
				"scale",
				"defaultValue"
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
		} else if (this.getDifferenceNullable(_column) != null) {
			return false;
		} else if (this.getDefaultValueDifference(_column) != null) {
			return false;
		} else if (this.getDifferencePrecision(_column) != null) {
			return false;
		} else if (this.getDifferenceScale(_column) != null) {
			return false;
		} else {
			return true;
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
		if (this.getDefaultValueDifference(_column) != null) {
			diffs.put("default", this.getDefaultValueDifference(_column));
		}
		if (this.getDifferencePrecision(_column) != null) {
			diffs.put("precision/scale", this.getDifferencePrecision(_column));
		}
		if (this.getDifferenceScale(_column) != null) {
			diffs.put("precision/scale", this.getDifferenceScale(_column));
		}
		
		return diffs;
	}
	

	
	public Double getDefaultValueDifference(MetaTableColumn _column) {
		if (this.defaultValue == null) {
			if (_column.getDefaultValue() != null) {
				return Double.valueOf((String) _column.getDefaultValue());
			} else {
				return null;
			}
		} else {
			if (_column.getDefaultValue() == null) {
				return this.defaultValue;
			} else {
				if (Double.valueOf((String) _column.getDefaultValue()).equals(this.defaultValue)) {
					return null;
				} else {
					return Double.valueOf((String) _column.getDefaultValue());
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
	
	public Integer getDifferencePrecision(MetaTableColumn _column) {
		if (_column.getColumnSize() == this.precision) {
			return null;
		} else {
			return this.precision;
		}
	}
	
	public Integer getDifferenceScale(MetaTableColumn _column) {
		if (_column.getDecimalPlaces() == this.scale) {
			return null;
		} else {
			return this.scale;
		}
	}

}
