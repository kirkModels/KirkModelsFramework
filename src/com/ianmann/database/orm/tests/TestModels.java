package com.ianmann.database.orm.tests;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import org.json.simple.parser.ParseException;

import com.ianmann.database.orm.Project;
import com.ianmann.database.orm.schema.migrationTracking.DbSync;
import com.ianmann.database.orm.schema.migrationTracking.MigrationGenerator;
import com.ianmann.database.orm.utils.exceptions.ObjectNotFoundException;

public abstract class TestModels {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ParseException, SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ObjectNotFoundException {
		// TODO Auto-generated method stub

		Project.initialize("settings/settings.json");
		
		System.out.println(Person.objects.all());
	}
}
