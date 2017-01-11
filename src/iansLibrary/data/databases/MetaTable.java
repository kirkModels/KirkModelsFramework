package iansLibrary.data.databases;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.fields.SavableField;

public class MetaTable {

	private MetaDatabase database;
	private String tableName;
	
	public String getTableName() {
		return tableName;
	}

	public ArrayList<MetaTableColumn> columns = new ArrayList<MetaTableColumn>();
	public ArrayList<MetaForeignKeyConstraint> foreignKeys = new ArrayList<MetaForeignKeyConstraint>();
	
	public MetaTable(MetaDatabase _database, String _tableName) throws SQLException {
		this.database = _database;
		this.tableName = _tableName.replace("_pkey", "");
		
		this.columns = this.getFields();
		this.foreignKeys = this.getForeignKeys();
	}
	
	public ArrayList<MetaTableColumn> getFields() throws SQLException {
		ResultSet fields = this.database.metaData.getColumns(null, null, this.tableName, null);
		ArrayList<MetaTableColumn> fieldsList = new ArrayList<MetaTableColumn>();
		
		while (fields.next()) {
			MetaTableColumn column = this.getSingleField(fields);
			fieldsList.add(column);
		}
		return fieldsList;
	}
	
	public MetaTableColumn getSingleField(ResultSet _fieldResult) throws SQLException {
		String columnName = _fieldResult.getString(MetaTableColumn.COLUMN_NAME);
		int dataType = _fieldResult.getInt(MetaTableColumn.DATA_TYPE_NAME);
		int nullable = _fieldResult.getInt(MetaTableColumn.NULLABLE);
		String defaultValue = _fieldResult.getString(MetaTableColumn.COLUMN_DEF);
		int columnSize = _fieldResult.getInt(MetaTableColumn.COLUMN_SIZE);
		int decimalPlaces = _fieldResult.getInt(MetaTableColumn.DECIMAL_PLACES);
		
		if (defaultValue != null) {
			/*
			 * Values of type varchar are returned as 'default_value'::character varying
			 * So this removes the extra stuff.
			 */
			defaultValue = defaultValue.split("'::")[0];
			defaultValue = defaultValue.replace("'", "");
		}
		
		MetaTableColumn column = new MetaTableColumn(columnName, dataType, nullable, defaultValue, columnSize, decimalPlaces);
		return column;
	}
	
	public ArrayList<MetaForeignKeyConstraint> getForeignKeys() throws SQLException {
		ResultSet constraints = this.database.metaData.getImportedKeys(null, null, this.tableName);
		ArrayList<MetaForeignKeyConstraint> constraintList = new ArrayList<MetaForeignKeyConstraint>();
		
		while (constraints.next()) {
			MetaForeignKeyConstraint constr = this.getSingleForeignKeyConstraint(constraints);
			constraintList.add(constr);
		}
		return constraintList;
	}
	
	public MetaForeignKeyConstraint getSingleForeignKeyConstraint(ResultSet _constraintResult) throws SQLException {
		String pkTableName = _constraintResult.getString(MetaForeignKeyConstraint.PKTABLE_NAME);
		String pkColumnName = _constraintResult.getString(MetaForeignKeyConstraint.PKCOLUMN_NAME);
		String fkTableName = _constraintResult.getString(MetaForeignKeyConstraint.FKTABLE_NAME);
		String fkColumnName = _constraintResult.getString(MetaForeignKeyConstraint.FKCOLUMN_NAME);
		String fkConstraintName = _constraintResult.getString(MetaForeignKeyConstraint.FK_NAME);
		
		MetaForeignKeyConstraint fkConstraint = new MetaForeignKeyConstraint(pkTableName, pkColumnName, fkTableName, fkColumnName, fkConstraintName);
		return fkConstraint;
	}
	
	public MetaForeignKeyConstraint getForeignKeyConstraint(String fieldName) {
		for (MetaForeignKeyConstraint fkConst : this.foreignKeys) {
			if (fkConst.getFkColumnName().equals(fieldName)) {
				return fkConst;
			}
		}
		return null;
	}
	
	public String toString() {
		return this.tableName;
	}
}
