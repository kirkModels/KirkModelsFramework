package com.ianmann.database.orm.schema.queries;

import java.lang.reflect.Constructor;

import com.ianmann.database.orm.scema.fields.ManyToManyField;
import com.ianmann.database.orm.scema.fields.SavableField;

import iansLibrary.utilities.JSONMappable;

public class DropField extends ColumnOperation implements JSONMappable {
	
	public static final String CASCADE = " CASCADE";
	public static final String RESTRICT = " RESTRICT";
	/**
	 * returns "".
	 */
	public static final String DEFAULT = "";
	
	public String option = "";

	public DropField(SavableField _field, String _option) {
		super(_field.label);

		this.option = _option;
	}
	
	public DropField(String _fieldName, String _option) {
		super(_fieldName);

		this.option = _option;
	}
	
	public DropField(ManyToManyField _field) {
		super(_field.tableName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMySqlString() {
		// TODO Auto-generated method stub
		return "DROP COLUMN " + this.fieldName;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		return "DROP COLUMN " + this.fieldName + this.option;
	}

	@Override
	public Constructor getJsonConstructor() {
		// TODO Auto-generated method stub
		Class[] paramTypes = new Class[]{
				String.class,
				String.class,
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
				"fieldName",
				"option",
		};
	}

}
