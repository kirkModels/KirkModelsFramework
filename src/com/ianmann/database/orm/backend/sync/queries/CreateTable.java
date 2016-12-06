package com.ianmann.database.orm.backend.sync.queries;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ForeignKey;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.fields.SavableField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.queries.Query;

import iansLibrary.utilities.JSONMappable;

public class CreateTable extends Query implements JSONMappable {
	
	// This will be an instance of the class we want to migrate.
	// Information in classes can only be obtained through instantiated classes so
	// this is that instantiated object. it is not an actual saved object.
	public ForeignKey[] foreignKeys = null;
	public SavableField[] fields = null;
	public ManyToManyField[] m2mFields = null;

	public CreateTable(String _dbName, Model _tempObject) {
		super(_dbName, _tempObject.tableName);
		// TODO Auto-generated constructor stub
		
		this.fields = new SavableField[_tempObject.savableFields.size()];
		for (int i = 0; i < _tempObject.savableFields.size(); i ++) {
			this.fields[i] = _tempObject.getField(_tempObject.savableFields.get(i));
		}
		this.m2mFields = new ManyToManyField[_tempObject.manyToManyFields.size()];
		for (int i = 0; i < _tempObject.manyToManyFields.size(); i ++) {
			try {
				this.m2mFields[i] = (ManyToManyField) _tempObject.getClass().getField(_tempObject.manyToManyFields.get(i)).get(_tempObject);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.setForeignKeys();
		
		this.setSql();
		this.shortDescription = "Create Model: " + Settings.syncedModels.get(this.tableName);
	}
	
	public CreateTable(String _dbName, String _tableName, SavableField[] _fields, ManyToManyField[] _m2mFields){
		super(_dbName, _tableName);
		
		this.fields = _fields;
		this.m2mFields = _m2mFields;
		this.setForeignKeys();
		
		this.setSql();
		this.shortDescription = "Create Model: " + Settings.syncedModels.get(this.tableName);
	}
	
	public void setForeignKeys(){
		int length = 0;
		for (int i = 0; i < this.fields.length; i++) {
			if (this.fields[i] instanceof ForeignKey) {
				length ++;
			}
		}
		
		this.foreignKeys = new ForeignKey[length];
		int numForeignKeys = 0;
		for (int i = 0; i < this.fields.length; i++) {
			if (this.fields[i] instanceof ForeignKey) {
				this.foreignKeys[numForeignKeys] = (ForeignKey) this.fields[i];
				numForeignKeys ++;
			}
		}
	}
	
	public String getMySqlFieldStrings() {
		String sql = "";

		this.foreignKeys = new ForeignKey[this.fields.length];
		for (int i = 0; i < this.fields.length; i++) {
			SavableField field = fields[i];
			sql = sql + "\n\t" + field.MySqlString().split("::")[0];
			
			if(i != this.fields.length - 1){
				sql = sql + ",";
			}
		}
		
		return sql;
	}
	
	public String getPsqlFieldStrings() {
		String sql = "";
		
		for (int i = 0; i < this.fields.length; i++) {
			SavableField field = this.fields[i];
			sql = sql + "\n\t" + field.PSqlString().split("::")[0];
			
			if(i != this.fields.length - 1){
				sql = sql + ",";
			}
		}
		
		return sql;
	}
	
	public String getFieldStrings() {
		String language = Settings.database.language;
		
		String sql = "";
		
		switch (language) {
		case "MySQL":
			
			sql = this.getMySqlFieldStrings();
			break;
			
		case "postgreSQL":
			
			sql = this.getPsqlFieldStrings();
			break;

		default:
			
			sql = "No default language.";
			break;
		}
		
		return sql;
	}
	
	public String getMySqlForeignKeyStrings() {
		String sql = "";
		
		for (int i = 0; i < foreignKeys.length - 1; i++) {
			ForeignKey fk = foreignKeys[i];
			sql = sql + "\n\tADD CONSTRAINT " + fk.symbol + " FOREIGN KEY " + fk.label + " " + fk.MySqlString().split("::")[1] + ",";
		}
		ForeignKey fk = foreignKeys[foreignKeys.length - 1];
		sql = sql + "\n\tADD CONSTRAINT " + fk.symbol + " FOREIGN KEY " + fk.label + " " + fk.MySqlString().split("::")[1];
		
		return sql;
	}
	
	public String getPsqlForeignKeyStrings() {
		String sql = "";
		
		for (int i = 0; i < foreignKeys.length - 1; i++) {
			ForeignKey fk = foreignKeys[i];
			sql = sql + "\n\tADD CONSTRAINT " + fk.symbol + " FOREIGN KEY (" + fk.label + ") " + fk.MySqlString().split("::")[1] + ",";
		}
		ForeignKey fk = foreignKeys[foreignKeys.length - 1];
		sql = sql + "\n\tADD CONSTRAINT " + fk.symbol + " FOREIGN KEY (" + fk.label + ") " + fk.MySqlString().split("::")[1];
		
		return sql;
	}
	
	public String getForeignKeyStrings() {
		String language = Settings.database.language;
		
		String sql = "";
		
		switch (language) {
		case "MySQL":
			
			sql = this.getMySqlForeignKeyStrings();
			break;
			
		case "postgreSQL":
			
			sql = this.getPsqlForeignKeyStrings();
			break;

		default:
			
			sql = "No default language.";
			break;
		}
		
		return sql;
	}

	@Override
	public void setSql() {
		// TODO Auto-generated method stub
		this.command = this.toString();
		
		for (ManyToManyField m2mf : this.m2mFields) {
			CreateTable createM2mField = new CreateTable(this.dbName, m2mf);
			this.command = this.command + "\n\n" + createM2mField.getCommand();
		}
	}

	@Override
	public void run() throws SQLException {
		// TODO Auto-generated method stub
//		System.out.println(this.command);
		Settings.database.run(this.command);
	}

	@Override
	public String getMySqlString() {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + this.dbName + "." + this.tableName + " (";
		sql = sql + this.getFieldStrings();
		sql = sql + "\n);";
//		if (foreignKeys.length > 0) {
//			sql = sql + "\nALTER TABLE " + this.tableName + this.getForeignKeyStrings() + ";";
//		}
		return sql;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + this.tableName + " (";
		sql = sql + this.getFieldStrings();
		sql = sql + "\n);";
//		if (foreignKeys.length > 0) {
//			sql = sql + "\nALTER TABLE " + this.tableName + this.getForeignKeyStrings() + ";";
//		}
		return sql;
	}

	@Override
	public Constructor getJsonConstructor() {
		// TODO Auto-generated method stub
		Class[] paramTypes = new Class[]{
				String.class,
				String.class,
				SavableField[].class,
				ManyToManyField[].class
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
				"fields",
				"m2mFields",
		};
	}
	
}
