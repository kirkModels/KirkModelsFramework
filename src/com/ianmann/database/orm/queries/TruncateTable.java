package com.ianmann.database.orm.queries;

import java.sql.SQLException;

import com.ianmann.database.config.Settings;

public class TruncateTable extends Query {
	
	public String cascade;

	public TruncateTable(String _dbName, String _tabelName, boolean _cascade) {
		super(_dbName, _tabelName);
		// TODO Auto-generated constructor stub
		
		if (_cascade) {
			this.cascade = " CASCADE";
		} else {
			this.cascade = "";
		}
		
		this.setSql();
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
		// TODO Auto-generated method stub
		String str = "TRUNCATE TABLE " + this.dbName + "." + this.tableName + ";";
		return str;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		String str = "TRUNCATE TABLE " + this.tableName + this.cascade + ";";
		return str;
	}

}
