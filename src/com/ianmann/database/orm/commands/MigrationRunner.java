package com.ianmann.database.orm.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

import com.ianmann.database.orm.Project;
import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.schema.migrationTracking.DbSync;
import com.ianmann.database.orm.utils.exceptions.ObjectNotFoundException;

import iansLibrary.data.databases.MetaDatabase;

public abstract class MigrationRunner {

	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ObjectNotFoundException {
		// TODO Auto-generated method stub
		try {
			Project.migrate("settings/settings.json");
		} catch (FileNotFoundException | ParseException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
