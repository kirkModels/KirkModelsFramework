package com.ianmann.database.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.tests.Person;
import com.ianmann.database.utils.Utilities;

import iansLibrary.data.databases.MetaDatabase;

public abstract class Settings {

	/**
	 * The global database object that contains meta data for the project. All SQL queries are run using the Meta Data in this object. The project also uses this object to run migrations and generate migration files.
	 */
	public static MetaDatabase database;
	
	/**
	 * All models in the project that are synced to the database.
	 * <br>Model classes can be accessed by keys where each key is the
	 * classes proper name with the "."'s replaced by "_"'s and the name
	 * is all lowercase. For instance, model {@code foo.bar.Pop} can be
	 * accessed by key {@code "foo_bar_pop"}.
	 */
	public static HashMap<String, Class<? extends Model>> syncedModels = new HashMap<String, Class<? extends Model>>();
	
	/**
	 * Full path to the root folder of the source code
	 */
	public static String ROOT_FOLDER;
	
	/**
	 * Full path to the root folder of the compiled code
	 */
	public static String BINARY_ROOT;
	
	/**
	 * Full path to the root folder containing all migrations
	 * in a project.
	 */
	public static String MIGRATION_FOLDER;
	
	/**
	 * Boolean which determins whether the project is in debug or not.
	 * If so, SQL queries will be printed to std out.
	 */
	public static Boolean DEBUG;
	
	/**
	 * Pulls settings from file found at {@code _pathToSettingsFile} and sets the values as java constants found in Settings class.
	 * 
	 * @param _pathToSettingsFile - Relative path to settings json file
	 * @throws FileNotFoundException if no file at {@code _pathToSettingsFile} can be found
	 * @throws ParseException if the file at {@code _pathToSettingsFile} does not contain valid json data
	 * @throws SQLException if no connection to the database can be made
	 */
	public static void syncSettings(File _pathToSettingsFile) throws FileNotFoundException, ParseException, SQLException{
		JSONObject settingsJson = Utilities.json(_pathToSettingsFile);
		
		//set database
		try{
			database = new MetaDatabase((String) settingsJson.get("defaultDb"), _pathToSettingsFile);
		} catch (SQLException e){
			throw new SQLException("No connection found at " + settingsJson.get("defaultDb") + ".", e);
		}
		
		//set syncedModels
		JSONArray tempSyncedModels = (JSONArray) settingsJson.get("synced_models");
		for (int i = 0; i < tempSyncedModels.size(); i++) {
			String model = (String) tempSyncedModels.get(i);
			try {
				// Key: Value pair is formated as "foo_bar_pop": class foo.bar.Pop
				syncedModels.put(model.replace(".", "_").toLowerCase(), (Class<? extends Model>) Class.forName(model));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//set migration folder
		MIGRATION_FOLDER = (String) settingsJson.get("migrations_folder");
		
		//set Root Folder for project development
		ROOT_FOLDER = (String) settingsJson.get("project_development_root_parent");
		
		//set root folder for binary files
		BINARY_ROOT = (String) settingsJson.get("binary_root_parent");
		
		//Set debug
		DEBUG = (1 == (long)settingsJson.get("debug"));
	}
	
	/**
	 * populates each models object query set with the objects found in the database
	 * in it's respective table. This also populates many to many relationships.
	 */
	public static void setObjectsForModels(){
		for (Class<? extends Model> type : syncedModels.values()) {
			QuerySet<? extends Model> objects = new QuerySet(type);
			
			try {
				type.getField("objects").set(null, objects);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException | NoSuchFieldException | IllegalAccessException e) {
				System.err.println("The class " + type.getName() + " must declare a static variable objects:\n\t" + 
									"public static QuerySet<" + type.getSimpleName() + "> objects;");
				System.exit(1);
			}
		}
		updateStoragesForManyToManyFields();
	}
	
	/**
	 * Refresh the many to many relationships for each model in {@code syncedModels}
	 */
	public static void updateStoragesForManyToManyFields(){
		for (Class<? extends Model> type : syncedModels.values()) {
			try {
				/*
				 * Loop through each object in the query set representing all objects of the model type
				 * and refresh any many to many fields in that object with it's relationships.
				 */
				for(Model object : (QuerySet<? extends Model>) type.getField("objects").get(null)){
					
					/*
					 * refresh this field with the objects that fill it's relationship
					 */
					for (String fieldName : object.manyToManyFields) {
						Object field = null;
						try {
							field = object.getClass().getField(fieldName).get(object);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchFieldException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ManyToManyField temp_field = (ManyToManyField) field;
						temp_field.getRelatedObjects();
					}
					
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Query database for instances in the table corresponding to {@code type} and
	 * populate this classes objects with those instances.
	 * @param _type
	 */
	public static void setObjectsForModel(Class<?> _type){
		QuerySet<? extends Model> objects = new QuerySet(_type);
		
		try {
			_type.getField("objects").set(null, objects);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateStorageForManyToManyFields(_type);
	}
	
	/**
	 * populates the models object query set with the objects found in the database
	 * in it's respective table. This also populates many to many relationships.
	 */
	public static void updateStorageForManyToManyFields(Class<?> _type){
		try {
			/*
			 * Loop through each object in the query set representing all objects of the model type
			 * and refresh any many to many fields in that object with it's relationships.
			 */
			for(Model object : (QuerySet<? extends Model>) _type.getField("objects").get(null)){

				/*
				 * refresh this field with the objects that fill it's relationship
				 */
				for (String fieldName : object.manyToManyFields) {
					Object field = null;
					try {
						field = object.getClass().getField(fieldName).get(object);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					ManyToManyField temp_field = (ManyToManyField) field;
					temp_field.getRelatedObjects();
				}
				
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
