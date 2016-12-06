package com.ianmann.database.orm.queries;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.fields.SavableField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.queries.scripts.InsertValue;

public class InsertQuery extends Query {
	
	public ArrayList<InsertValue> insertVals = new ArrayList<InsertValue>();

	public InsertQuery(String _tabelName, ArrayList<InsertValue> _insertVals) {
		super(Settings.database.dbName, _tabelName);
		// TODO Auto-generated constructor stub
		
		this.insertVals = _insertVals;
		this.setSql();
	}
	
	public InsertQuery(Model instance) {
		super(Settings.database.dbName, instance.tableName);
		
		this.setValuesFromInstance(instance);
		this.setSql();
	}
	
	public void setValuesFromInstance(Model instance){
		for (int i = 0; i < instance.savableFields.size(); i++) {
			
			SavableField field = instance.getField(instance.savableFields.get(i));
			
			InsertValue c = new InsertValue(field.label, field.val());
			
			this.insertVals.add(c);
		}
	}
	
	public void setSql(){
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
		String sql = "INSERT INTO " + this.dbName + "." + this.tableName;
		
		sql = sql + this.getInsertValString();
		
		sql = end(sql);
		
		return sql;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		String sql = "INSERT INTO " + this.tableName;
		
		sql = sql + this.getInsertValString();
		
		sql = end(sql);
		
		return sql;
	}
	
	public String getInsertValString(){
		String fields = " ( ";
		String values = " VALUES ( ";
		
		for (int i = 0; i < this.insertVals.size(); i++) {
			InsertValue c = this.insertVals.get(i);
			
			// fields
			fields = fields + c.fieldName;
			
			if (i < this.insertVals.size() - 1) {
				fields = fields + ",";
			}
			
			fields = fields + " ";
			
			// values
			if(c.value != null) {
				values = values + InsertValue.sqlStr(c.value);
			} else {
				values = values + "NULL";
			}
			
			if (i < this.insertVals.size() - 1) {
				values = values + ",";
			}
			
			values = values + " ";
		}
		
		fields = fields + ")";
		values = values + ")";
		
		return fields + values;
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
