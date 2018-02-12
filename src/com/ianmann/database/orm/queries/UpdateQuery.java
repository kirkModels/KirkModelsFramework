package com.ianmann.database.orm.queries;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.scema.fields.SavableField;

public class UpdateQuery extends WhereConditionedQuery {
	
	ArrayList<WhereCondition> newValues = new ArrayList<WhereCondition>();

	public UpdateQuery(String _tableName, ArrayList<WhereCondition> _newValues, ArrayList<WhereCondition> _conditions) {
		super(Settings.database.dbName, _tableName, _conditions);
		// TODO Auto-generated constructor stub
		
		this.newValues = _newValues;
		this.setSql();
	}
	
	public UpdateQuery(Model updatedInstance) {
		super(Settings.database.dbName, updatedInstance.tableName, null);
		// TODO Auto-generated constructor stub
		
		this.setConditionsFromInstance(updatedInstance);
		this.setNewValuesFromInstance(updatedInstance);
		this.setSql();
	}
	
	public void setConditionsFromInstance(Model instance){
		WhereCondition c = new WhereCondition(instance.id.label, WhereCondition.EQUALS, instance.id.val());
		this.conditions = new ArrayList<WhereCondition>();
		this.conditions.add(c);
	}
	
	public void setNewValuesFromInstance(Model instance){
		for (int i = 0; i < instance.savableFields.size(); i++) {
			SavableField field = instance.getField(instance.savableFields.get(i));
			
			WhereCondition c = new WhereCondition(field.label, WhereCondition.EQUALS, field.val());
			
			this.newValues.add(c);
		}
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
	
	public String getNewValsString(){
		String sql = " SET ";
		
		for (int i = 0; i < this.newValues.size(); i++) {
			WhereCondition c = this.newValues.get(i);
			
			sql = sql + c.getMySqlString();
			
			if(i < this.newValues.size() - 1) {
				sql = sql + ", ";
			}
		}
		
		return sql;
	}
	
	@Override
	public String getMySqlString() {
		String sql = "UPDATE " + this.dbName + "." + this.tableName;
		
		sql = sql + this.getNewValsString();
		
		sql = sql + super.getMySqlString();
		
		sql = end(sql);
		
		return sql;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		String sql = "UPDATE " + this.tableName;
		
		sql = sql + this.getNewValsString();
		
		sql = sql + super.getMySqlString();
		
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
