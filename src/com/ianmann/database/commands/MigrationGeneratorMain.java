package com.ianmann.database.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

import com.ianmann.database.config.Settings;
import com.ianmann.database.orm.Project;
import com.ianmann.database.orm.backend.sync.DbSync;
import com.ianmann.database.orm.backend.sync.MigrationGenerator;

/**
 * Abstract class containing the main method to run {@code MigrationGenerator.getMigrations()}.
 * @author kirkp1ia
 *
 */
public abstract class MigrationGeneratorMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Project.makeMigrations("settings/settings.json");
		} catch (FileNotFoundException | ParseException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
