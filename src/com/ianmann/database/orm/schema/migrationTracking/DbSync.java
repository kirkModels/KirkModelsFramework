package com.ianmann.database.orm.schema.migrationTracking;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.queries.Query;
import com.ianmann.database.orm.queries.WhereCondition;
import com.ianmann.database.orm.scema.fields.ManyToManyField;
import com.ianmann.database.orm.schema.queries.AlterTable;
import com.ianmann.database.orm.schema.queries.CreateTable;
import com.ianmann.database.orm.utils.exceptions.ObjectNotFoundException;

import iansLibrary.data.databases.MetaDatabase;
import iansLibrary.utilities.JSONClassMapping;

public class DbSync {
	
	static ArrayList<AlterTable> foreignKeyOperations = null;
	
	public static void migrate() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, SQLException, ObjectNotFoundException {
		if (Settings.database.getSpecificTable(new MigrationTracking().tableName) == null) {
			MigrationTracking.syncTable();
			MigrationFile.syncTable();
		}
		Settings.setObjectsForModel(MigrationTracking.class);
		Settings.setObjectsForModel(MigrationFile.class);
		syncBaseDatabase();
		migrateModels();
	}
	
	public static void syncBaseDatabase() {
		try {
			for (String tableName : Settings.syncedModels.keySet()) {
				Class<? extends Model> modelClass = Settings.syncedModels.get(tableName);
				MigrationTracking mt;
					mt = MigrationTracking.objects.getOrCreate(new ArrayList<WhereCondition>(){{
						add(new WhereCondition("model_name", WhereCondition.EQUALS, modelClass.getName()));
					}}).getKey();
				
				for (int i = 0; i < getMigrationFilesForModel(getMigrationFolderForModel(modelClass)).length; i++) {
					File migFile = getMigrationFilesForModel(getMigrationFolderForModel(modelClass))[i];
					String fileName = migFile.getName();
					String dirName = migFile.getParentFile().getAbsolutePath().replace(Settings.BINARY_ROOT, "");
					MigrationFile f = MigrationFile.objects.getOrCreate(new ArrayList<WhereCondition>(){{
						add(new WhereCondition("file_name", WhereCondition.EQUALS, fileName));
						add(new WhereCondition("dir_path", WhereCondition.EQUALS, dirName));
						add(new WhereCondition("model_name", WhereCondition.EQUALS, modelClass.getName()));
						add(new WhereCondition("migration_tracker", WhereCondition.EQUALS, mt.id.val()));
					}}).getKey();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static File[] getMigrationFilesForModel(File migrationFolder) {
		File[] migrationFiles = migrationFolder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().contains(".json")) {
					return true;
				}
				return false;
			}
		});
		return migrationFiles;
	}
	
	public static File getMigrationFolderForModel(Class<? extends Model> _modelClass) {
		try {
			String path = Settings.MIGRATION_FOLDER + _modelClass.newInstance().tableName + "-migrations/";
			return new File(path);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void migrateModels() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, SQLException, ObjectNotFoundException {
		foreignKeyOperations = new ArrayList<AlterTable>();
		for (String modelKey : Settings.syncedModels.keySet()) {
			Class<? extends Model> modelClass = Settings.syncedModels.get(modelKey);

			runNextMigrationForModel(modelClass);
		}
		runForeignKeyOperations();
	}
	
	public static void runNextMigrationForModel(Class<? extends Model> modelClass) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, SQLException, ObjectNotFoundException {
		System.out.println("Applying Migrations for " + modelClass.getName() + "...");
		MigrationTracking migrationTracker = MigrationTracking.objects.get(new ArrayList<WhereCondition>(){{
			add(new WhereCondition("model_name", WhereCondition.EQUALS, modelClass.getName()));
		}});
		
		MigrationFile toRun = null;
		if (migrationTracker.last_ran.val() != null) {
			toRun = MigrationFile.objects.get(new ArrayList<WhereCondition>(){{
				add(new WhereCondition("file_name", WhereCondition.EQUALS, migrationTracker.last_ran.val()));
				add(new WhereCondition("migration_tracker", WhereCondition.EQUALS, migrationTracker.id.val()));
			}}).getNextMigrationFile();
		} else {
			QuerySet<MigrationFile> migs = MigrationFile.objects.filter(new ArrayList<WhereCondition>(){{
				add(new WhereCondition("migration_tracker", WhereCondition.EQUALS, migrationTracker.id.val()));
			}});
			
			if (migs.count() > 0) {
				toRun = MigrationFile.objects.get(new ArrayList<WhereCondition>(){{
					add(new WhereCondition("file_name", WhereCondition.EQUALS, "0001_initial.json"));
					add(new WhereCondition("migration_tracker", WhereCondition.EQUALS, migrationTracker.id.val()));
				}});
			}
		}
		
		if (toRun != null) {
			Migration toMigrate = Migration.getMigrationFromFile(new File(toRun.getPathToFile()));
			String dependsFileName = toMigrate.dependsOn.split("/")[toMigrate.dependsOn.split("/").length - 1];
			String dependsDirName = toMigrate.dependsOn.replace(dependsFileName, "");
			try {
				MigrationFile depended = MigrationFile.objects.get(new ArrayList<WhereCondition>(){{
					add(new WhereCondition("file_name", WhereCondition.EQUALS, dependsFileName));
					add(new WhereCondition("dir_path", WhereCondition.EQUALS, dependsDirName));
				}});
				if (depended.compareTo(toRun) == 0) {
					// it is a migration that is not for modelClass.
					
				} else {
					// assume it's already run.
					toMigrate.run();
					migrationTracker.setLastRan(toRun);
					migrationTracker.save();
					runNextMigrationForModel(modelClass);
				}
			} catch (ObjectNotFoundException e) {
				System.out.println("\t" + toRun.file_name + "...");
				toMigrate.run();
				foreignKeyOperations.addAll(toMigrate.getForeignKeyOpertations());
				migrationTracker.setLastRan(toRun);
				migrationTracker.save();
				runNextMigrationForModel(modelClass);
			}
		} else {
			System.out.println("\tNo Migrations need to be applied for " + modelClass.getName() + ".");
		}
	}
	
	public static void runForeignKeyOperations() throws SQLException{
		for (AlterTable query : foreignKeyOperations) {
			query.run();
		}
		foreignKeyOperations = null;
	}
	
	
}
