package com.ianmann.database.orm.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.ianmann.database.config.Settings;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.backend.sync.migrationTracking.MigrationTracking;
import com.ianmann.database.orm.queries.scripts.WhereCondition;

public class SelectQuery extends WhereConditionedQuery {
	
	public ArrayList<String> fields = new ArrayList<String>();
	
	public ResultSet results;

	public SelectQuery(String _tabelName, ArrayList<WhereCondition> _conditions) {
		super(Settings.database.dbName, _tabelName, _conditions);
		
		this.fields.add("*");
		this.setSql();
	}
	
	public SelectQuery(String _tabelName, ArrayList<String> fields, ArrayList<WhereCondition> _conditions) {
		super(Settings.database.dbName, _tabelName, _conditions);
		
		this.fields = fields;
		this.setSql();
	}
	
	public void setSql(){
		this.command = this.toString();
	}
	
	public void run() throws SQLException{
		ResultSet results = Settings.database.executeQuery(this.command);
		
		this.results = results;
	}
	
	public String getFieldsString(){
		String fields = "";
		
		for (String field : this.fields) {
			fields = fields + field;
			
			if (this.fields.indexOf(field) < this.fields.size() - 1) {
				fields = fields + ", ";
			}
		}
		
		return fields;
	}

	@Override
	public String getMySqlString() {
		String str = "SELECT ";
		
		str = str + this.getFieldsString();
		
		str = str + " FROM " + this.dbName + "." + this.tableName;
		
		str = str + super.getMySqlString();
		
		str = end(str);
		return str;
	}

	@Override
	public String getPsqlString() {
		String str = "SELECT ";
		
		str = str + this.getFieldsString();
		
		str = str + " FROM " + this.tableName;
		
		str = str + super.getPsqlString();
		
		str = end(str);
		
		return str;
	}
}
