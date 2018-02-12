package com.ianmann.database.orm.queries;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.utils.exceptions.LanguageNotSupportedError;

public abstract class Query {

	public String tableName;
	public String dbName;
	
	public String shortDescription;
	protected String command;
	
	public Query(String _dbName, String _tabelName) {
		this.dbName = _dbName;
		this.tableName = _tabelName;
	}
	
	public abstract void setSql();
	
	public abstract void run() throws SQLException;
	
	public abstract String getMySqlString();
	
	public abstract String getPsqlString();
	
	public String toString(){
		String language = Settings.database.language;
		
		String sql = "";
		
		if (language.equals(Settings.database.MYSQL)) {
			
			sql = this.getMySqlString();
			
		} else if (language.equals(Settings.database.POSTRESQL)) {
			
			sql = this.getPsqlString();
			
		} else {
			
			throw new LanguageNotSupportedError();
			
		}
		
		return sql;
	}
	
	public String getCommand(){
		return this.command;
	}
	
	public String end(String sql){
		sql = sql + ";";
		
		return sql;
	}
}
