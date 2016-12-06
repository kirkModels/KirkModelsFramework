package com.ianmann.database.orm.backend.sync.queries;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.fields.SavableField;
import com.ianmann.database.orm.queries.Query;

import iansLibrary.utilities.JSONMappable;

public class AlterTable extends Query implements JSONMappable {
	
	public Operation[] operations;

	public AlterTable(String _dbName, String _tabelName, Operation[] _operations) {
		super(_dbName, _tabelName);
		// TODO Auto-generated constructor stub
		
		this.operations = _operations;
		this.setSql();
		this.shortDescription = "Change Model: " + Settings.syncedModels.get(this.tableName);
	}

	@Override
	public void setSql() {
		// TODO Auto-generated method stub
		this.command = this.toString();
	}

	@Override
	public void run() throws SQLException {
		// TODO Auto-generated method stub
		Settings.database.run(this.command);
	}

	@Override
	public String getMySqlString() {
		String sql = "ALTER TABLE " + this.dbName + "." + this.tableName;
		
		for (int i = 0; i < this.operations.length - 1; i++) {
			Operation oper = this.operations[i];
			sql = sql + "\n\t" + oper.getMySqlString() + ",";
		}
		if (this.operations.length > 0) {
			sql = sql + "\n\t" + this.operations[this.operations.length].getMySqlString();
		}
		
		sql = end(sql);
		return sql;
	}

	@Override
	public String getPsqlString() {
		String sql = "ALTER TABLE " + this.tableName;
		
		for (int i = 0; i < this.operations.length - 1; i++) {
			Operation oper = this.operations[i];
			sql = sql + "\n\t" + oper.getPsqlString() + ",";
		}
		if (this.operations.length > 0) {
			sql = sql + "\n\t" + this.operations[this.operations.length - 1].getPsqlString();
		}
		
		sql = end(sql);
		return sql;
	}

	@Override
	public Constructor getJsonConstructor() {
		// TODO Auto-generated method stub
		Class[] paramTypes = new Class[]{
				String.class,
				String.class,
				Operation[].class,
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
				"dbName",
				"tableName",
				"operations",
		};
	}

}
