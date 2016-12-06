package com.ianmann.database.orm.backend.sync.queries;

import java.sql.SQLException;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ForeignKey;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.fields.SavableField;
import com.ianmann.database.orm.Model;

public class AddColumn extends ColumnOperation {
	
	private SavableField field;
	private ManyToManyField m2mField;

	public AddColumn(SavableField _field) {
		super(_field.label);
		this.field = _field;
	}
	
	public AddColumn(ManyToManyField _field) {
		super(null);
		this.m2mField = _field;
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

}
