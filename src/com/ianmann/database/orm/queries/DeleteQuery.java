package com.ianmann.database.orm.queries;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.config.Settings;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.queries.scripts.WhereCondition;

public class DeleteQuery extends WhereConditionedQuery {

	public DeleteQuery(String _tabelName, ArrayList<WhereCondition> _conditions) {
		super(Settings.database.dbName, _tabelName, _conditions);
		// TODO Auto-generated constructor stub
		
		this.setSql();
	}
	
	public void setSql(){
		this.command = this.toString();
	}

	@Override
	public void run() throws SQLException {
		// TODO Auto-generated method stub
		Settings.database.run(this.toString());
	}
	
	public String getMySqlString(){
		String sql = "DELETE FROM " + this.dbName + "." + this.tableName;
		
		sql = sql + super.getMySqlString();
		
		sql = end(sql);
		
		return sql;
	}
	
	public String getPsqlString(){
		String sql = "DELETE FROM " + this.tableName;
		
		sql = sql + super.getPsqlString();
		
		sql = end(sql);
		
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
