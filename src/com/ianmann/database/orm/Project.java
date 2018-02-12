package com.ianmann.database.orm;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.schema.migrationTracking.DbSync;
import com.ianmann.database.orm.schema.migrationTracking.MigrationGenerator;
import com.ianmann.database.orm.utils.exceptions.ObjectNotFoundException;

/**
 * Core class that contains methods often used in development for the project.
 * @author kirkp1ia
 *
 */
public final class Project {

	/**
	 * This is an abstract class but because it is also final, the abstract keyword cannot be applied. Therefore, this disables instantiation.
	 */
	private Project(){}
	
	/**
	 * <p>initializes global variables needed in the project.</p>
	 * <p>
	 * - Read settings from {@code _pathToSettingsFile}.
	 * <br>
	 * - Connect {@code Settings.database} to the database referred to in the settings JSON file.
	 * <br>
	 * - read the database in to the objects for each Model.
	 * </p>
	 * @param _pathToSettingsFile - Relative path to settings json file
	 * @throws FileNotFoundException if no file at {@code _pathToFile} is found.
	 * @throws ParseException if a syntax error is found in the file at {@code _pathToFile}. This file must be of JSON format.
	 * @throws SQLException if Settings cannot connect the MetaDatabase object stored in {@code Settings.database} to the database using the configurations found in the file at {@code _pathToFile}.
	 */
	public static void initialize(String _pathToSettingsFile) throws FileNotFoundException, ParseException, SQLException {
		Settings.syncSettings(new File(_pathToSettingsFile));
			
		Settings.database.connect();

		Settings.setObjectsForModels();
	}
	
	public static void makeMigrations(String _pathToSettingsFile) throws FileNotFoundException, ParseException, SQLException {
		Settings.syncSettings(new File(_pathToSettingsFile));
		
		Settings.database.connect();
		
		MigrationGenerator.getMigrations();
	}
	
	public static void migrate(String _pathToSettingsFile) throws FileNotFoundException, ParseException, SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ObjectNotFoundException {
		Settings.syncSettings(new File(_pathToSettingsFile));
		
		Settings.database.connect();
		
		DbSync.migrate();
	}
}
