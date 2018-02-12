package com.ianmann.database.orm.schema.queries;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.scema.fields.ForeignKey;
import com.ianmann.database.orm.scema.fields.ManyToManyField;
import com.ianmann.database.orm.scema.fields.SavableField;

import iansLibrary.utilities.JSONMappable;

public class AddColumn extends ColumnOperation implements JSONMappable {
	
	public SavableField field;

	public AddColumn(SavableField _field) {
		super(_field.label);
		this.field = _field;
	}

	public String getMySqlString() {
		// TODO Auto-generated method stub
		String sql = "ADD COLUMN ";
		sql = sql + this.field.MySqlString().split("::")[0];
		return sql;
	}

	public String getPsqlString() {
		// TODO Auto-generated method stub
		String sql = "ADD COLUMN ";
		sql = sql + this.field.PSqlString().split("::")[0];
		return sql;
	}

	@Override
	public Constructor getJsonConstructor() {
		// TODO Auto-generated method stub
		Class[] paramTypes = new Class[]{
				SavableField.class,
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
				"field",
		};
	}

}
