package com.ianmann.database.orm.schema.migrationTracking;

import java.sql.SQLException;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.scema.fields.CharField;
import com.ianmann.database.orm.schema.queries.CreateTable;

public class MigrationTracking extends Model {

	public static QuerySet<MigrationTracking> objects;

	public CharField model_name = new CharField("model_name", false, null, true, 100);
	public CharField last_ran = new CharField("last_ran", true, null, true, 100);
	
	public MigrationTracking() {
		super();
		this.tableName = "orm_migration_tracking";
	}
	
	public void setLastRan(MigrationFile f) {
		this.last_ran.set(f.file_name.val());
	}
	
	public String toString() {
		return this.model_name.val() + " - last ran: " + this.last_ran.val();
	}
	
	public static void syncTable(){
		CreateTable createTracking = new CreateTable(Settings.database.dbName, new MigrationTracking());
		try {
			createTracking.run();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
