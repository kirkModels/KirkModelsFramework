package com.ianmann.database.orm.backend.sync;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.text.NumberFormatter;

import org.json.simple.parser.JSONParser;

import com.ianmann.database.config.Settings;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.backend.sync.migrationTracking.MigrationFile;
import com.ianmann.database.orm.backend.sync.migrationTracking.MigrationTracking;
import com.ianmann.database.orm.backend.sync.queries.AddColumn;
import com.ianmann.database.orm.backend.sync.queries.AddForeignKey;
import com.ianmann.database.orm.backend.sync.queries.AlterTable;
import com.ianmann.database.orm.backend.sync.queries.ColumnDefinitionChange;
import com.ianmann.database.orm.backend.sync.queries.ColumnOperation;
import com.ianmann.database.orm.backend.sync.queries.CreateTable;
import com.ianmann.database.orm.backend.sync.queries.DropTable;
import com.ianmann.database.orm.backend.sync.queries.Operation;
import com.ianmann.database.orm.backend.sync.queries.RenameTable;
import com.ianmann.database.orm.queries.Query;
import com.ianmann.database.orm.queries.scripts.WhereCondition;
import com.ianmann.database.tests.Person;
import com.ianmann.database.utils.exceptions.ObjectAlreadyExistsException;
import com.ianmann.database.utils.exceptions.ObjectNotFoundException;

import iansLibrary.data.databases.MetaTable;
import iansLibrary.utilities.JSONClassMapping;
import iansLibrary.utilities.JSONFormat;
import iansLibrary.utilities.ObjectParser;

public final class MigrationGenerator {
	
	public String rootMigrationFolderPath;
	public PrintWriter migrationWriter;
	public Migration migration;
	public File migrationFile;
	public Class<? extends Model> type;
	
	public MigrationGenerator(Class<? extends Model> _type) {
		this.type = _type;
		try {
			this.rootMigrationFolderPath = Settings.MIGRATION_FOLDER + this.type.newInstance().tableName + "-migrations/";
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the migration file for a given class type.
	 * @param _key - the key which will be sent to {@code Settings.syncedModels} to retrieve the class type to migrate.
	 * @param _indexInStorage - the index in which this migration file will be stored in migration array attributes.
	 * @throws IOException
	 */
	public void genterateMigrationFile() throws IOException {
		
		/*
		 * instantiated migration folder
		 */
		this.migrationFile = this.getMigrationFile();
		
		try {
			PrintWriter pw = new PrintWriter(this.migrationFile);
			this.migrationWriter = pw;
			/*
			 * From here, generate a migration for type and add it to this.migrations at index i.
			 * later, we will loop through this.migrations and call those migration.
			 */
//			this.migrations[i] = this.makeMigration(type);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.migrationWriter = null;
		}
	}
	
	/**
	 * namespaces a file with the format "####". It should have 4 digits padded by zeros.
	 * @param num
	 * @return
	 */
	public String getFileName(Integer num) {
		String fileBeginning = String.format("%04d", num);
		
		fileBeginning = fileBeginning;
		
		return fileBeginning;
	}
	
	/**
	 * creates a new migration file in the migration folder at {@code _migrationFolder}. if folder is empty,
	 * this method will create a file called "0001_initial.json". otherwise, it will create a file called "????_.json"
	 * where ???? is the number of json files in this folder + 1 and padded by zeros.
	 * @param _migrationFolder
	 * @return File - the newly created migration file
	 * @throws IOException
	 */
	public File createMigrationFile(File _migrationFolder) throws IOException {
		/*
		 * search for any json files in migrtionFolder which will be pre-existing migration files.
		 * 
		 * found at http://stackoverflow.com/questions/13515150/how-to-get-file-from-directory-with-pattern-filter
		 */
		File[] migrationFiles = _migrationFolder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().contains(".json")) {
					return true;
				}
				return false;
			}
		});
		
		/*
		 * If the folder exists, make sure that it contains other migration files. if not, skip this and
		 * create the initial migration file for this class.
		 */
		if (migrationFiles.length > 0) {
			// there are migration files in the migration folder
			
			/*
			 * Take the length of migrationFiles and add one to it. set the result as the index
			 * of the migration file which we will create and return in this method.
			 */
			int nextIndex = migrationFiles.length + 1;
			
			/*
			 * name of the file to create and return.
			 */
			String nextFileName = _migrationFolder.getAbsolutePath() + "/" + this.getFileName(nextIndex) + "_.json";
			
			//abstract next file. don't know at this point in the method if this file exists or not. It shouldn't.
			File nextMigrationFile = new File(nextFileName);
			
			/*
			 * Make sure the file doesn't exist. if it does, throw an error.
			 * This means that some migration file is missing so the number of files don't match the highest file index.
			 */
			if (nextMigrationFile.exists()) {
				throw new IOException("It looks as if a migration file has been deleted. there should be " + (nextIndex - 1) + " files in the migration folder: " + _migrationFolder.getAbsolutePath());
			} else {
				/*
				 * create the new migration file and return it.
				 */
				nextMigrationFile.createNewFile();
				return nextMigrationFile;
			}
		} else {
			//no migration files found.
			// get name for initial file to create
			String initialMigrationName = _migrationFolder.getAbsolutePath() + "/" + "0001_initial.json";
			
			//create the initial file and return it.
			File initialFile = new File(initialMigrationName);
			
			initialFile.createNewFile();
			return initialFile;
		}
	}
	
	/**
	 * This method returns the file that will contain the migration operations for the class
	 * returned when sending {@code keyForSyncedModels} to {@code Settings.syncedModels}.
	 * @param keyForSyncedModels - key used to get the model type to which the migration file coresponds
	 * @param pathToFolder - path to base migration folder
	 * @return
	 * @throws IOException - if a migration file has been deleted.
	 */
	public File getMigrationFile() throws IOException {
		/*
		 * the file returned will be used to print a migration operation to.
		 * 
		 * This file should be contained in the following directory system:
		 * 
		 * - <migrationFolder>
		 * 		- <DbObject_type_folderName>-migration
		 * 			- [migrationfiles]
		 */
		
		/*
		 * This file is the folder that contains the migration files for the given type.
		 */
		File migrationFolder = new File(this.rootMigrationFolderPath);
		
		if (migrationFolder.exists()) {
			File nextMigrationFile = this.createMigrationFile(migrationFolder);
			System.out.println("next one: " + nextMigrationFile.getAbsolutePath());
			return nextMigrationFile;
		} else {
			if (migrationFolder.mkdirs()) { //create folder to store migration files
				File newMigrationInitFile = this.createMigrationFile(migrationFolder);
				System.out.println("new one: " + newMigrationInitFile.getAbsolutePath());
				return newMigrationInitFile;
			} else {
				return null;
			}
		}
	}
	
	public boolean tableExists(MetaTable _table) {
		for (Class modelClass : Settings.syncedModels.values()) {
			try {
				Model modelObject = (Model) modelClass.newInstance();
				if (modelObject.tableName.equals(_table.getTableName())) {
					return true;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * This method will loop through the fields in a class and determin the differences between them and
	 * the fields contained in the database. It will also loop through the database and determin which
	 * fields have been dropped.<br><br>
	 * This method assumes the table exists in the database for the type {@code _newDef}.
	 * @param _newDef
	 * @param _databaseState
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<Query> getTableDifferences() throws SQLException {
		MetaTable databaseState = null;
		try {
			databaseState = Settings.database.getSpecificTable(this.type.newInstance().tableName);
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ArrayList<Query> queries = new ArrayList<Query>();
		
		try {
			//make a temporary object out of that class. This is because the fields need to be instantiated in order
			//		to do anything with them.
			Model modelObject = (Model) this.type.newInstance();
			//table to compare the fields with modelObject
			MetaTable savedTable = Settings.database.getSpecificTable(modelObject.tableName);
			
			if (savedTable == null) {
				//If this is true, then the class has been added recently since the last migration file
				return new ArrayList<Query>(){{ add(new CreateTable(modelObject.tableName, modelObject)); }};
			} else {
				//table was found but there may be differences
				ArrayList<Operation> diffs = modelObject.getOperationDifferences(savedTable);
				ArrayList<CreateTable> m2mFieldAdds = new ArrayList<CreateTable>();
				ArrayList<DropTable> m2mFieldRemoves = new ArrayList<DropTable>();
				ArrayList<String> m2mFieldsDealtWith = new ArrayList<String>();
				
				//adding create table for m2m field
				for (String m2mFieldName : modelObject.manyToManyFields) {
					ManyToManyField m2mField = (ManyToManyField) modelObject.getFieldGeneric(m2mFieldName);
					if (Settings.database.getSpecificTable(m2mField.tableName) == null && !m2mFieldsDealtWith.contains(m2mField.tableName)) {
						m2mFieldAdds.add(new CreateTable(m2mField.tableName, m2mField));
						m2mFieldsDealtWith.add(m2mField.tableName);
					}
				}
				
				for (MetaTable metam2mTable : Settings.database.getTables()) {
					if (m2mFieldsDealtWith.contains(metam2mTable.getTableName())) {
						continue;
					}
					if (metam2mTable.getTableName().split("___").length == 4 && metam2mTable.getTableName().split("___")[0].equals("mtm") && !metam2mTable.getTableName().contains("pkey")) {
						/*
						 * This is a many to many field
						 * If this m2m table belongs to this this model. We don't know if it's necesarilly the right field yet.
						 */
						if (metam2mTable.getTableName().split("___")[2].equals(databaseState.getTableName().split("_")[databaseState.getTableName().split("_").length - 1])) {
							/*
							 * does the field exist still? if not, drop this table/m2m field
							 */
							if (!modelObject.manyToManyFields.contains(metam2mTable.getTableName().split("___")[1])) {
								//Then this field has been dropped.
								m2mFieldRemoves.add(new DropTable(Settings.database.dbName, metam2mTable.getTableName()));
								m2mFieldsDealtWith.add(metam2mTable.getTableName());
							} else {
								//The field still exists but may have been altered
								String metaTableRefName = metam2mTable.getTableName().split("___")[3];
								ManyToManyField m2mf = (ManyToManyField) modelObject.getFieldGeneric(metam2mTable.getTableName().split("___")[1]);
								
								if (!metaTableRefName.equals(m2mf.refClass.getSimpleName().toLowerCase())) {
									/*
									 * Then the field has been altered. drop the table. It has laready been taken care of above while
									 * adding tables that have been created as new.
									 */
									m2mFieldRemoves.add(new DropTable(Settings.database.dbName, metam2mTable.getTableName()));
									m2mFieldsDealtWith.add(metam2mTable.getTableName());
								} else {
									m2mFieldsDealtWith.add(metam2mTable.getTableName());
								}
								
							}
						}
					}
				}
				
				if (diffs.size() > 0) {
					Operation[] operations = new Operation[diffs.size()];
					AlterTable at = new AlterTable(Settings.database.dbName, modelObject.tableName, diffs.toArray(operations));
					queries.add(at);
				}
				
				if (m2mFieldAdds.size() > 0) {
					queries.addAll(m2mFieldAdds);
				}
				
				if (m2mFieldRemoves.size() > 0) {
					queries.addAll(m2mFieldRemoves);
				}
			}
			return queries;
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getLastMigrationRan() {
		try {
			File migrationFolder = new File(this.rootMigrationFolderPath + this.type.newInstance().tableName + "-migrations/");
			File[] migrationFiles = migrationFolder.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().contains(".json")) {
						return true;
					}
					return false;
				}
			});
			int max = 0;
			for (File migFile : migrationFiles) {
				int otherNum = Integer.parseInt(migFile.getName().split("_")[0]);
				if (max - otherNum > 0) {
					max = otherNum;
				}
			}
			for (File migFile : migrationFiles) {
				if (Integer.parseInt(migFile.getName().split("_")[0]) == max) {
					return migFile.getName();
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			return null;
		}
		return null;
	}
	
	public void generate() {
		String tableName = null;
		try {
			tableName = this.type.newInstance().tableName;
		} catch (InstantiationException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (IllegalAccessException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		try {
			if (Settings.database.getSpecificTable(this.type.newInstance().tableName) == null) {
				//table has been added so we need to make a create table query.
				try {
					this.genterateMigrationFile();
					this.migration = new Migration(this.type);
					return;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.type.getField("DROP_TABLE");
			/*
			 * add drop table stuff here.
			 */
			Migration newMigration = null;
			
			String last = this.getLastMigrationRan();
			if (last == null) {
				newMigration = new Migration(this.getLastMigrationRan(), new Query[]{
						new DropTable(Settings.database.dbName, tableName)
				}, new Query[0]);
			} else {
				newMigration = new Migration(null, new Query[]{
						new DropTable(Settings.database.dbName, tableName)
				}, new Query[0]);
			}
			
			this.genterateMigrationFile();
			this.migration = newMigration;
			
		} catch (NoSuchFieldException e) {
			//This field does not exist so now see if this table was renamed.
			
			try {
				Field renameField = this.type.getField("RENAME_TABLE");
				Migration newMigration = null;
				
				String last = this.getLastMigrationRan();
				if (last == null) {
					String newName = (String) renameField.get(null);
					newMigration = new Migration(last, new Query[]{
							new AlterTable(Settings.database.dbName, this.type.newInstance().tableName, new Operation[]{
									new RenameTable(newName)
							})
					}, new Query[0]);
				} else {
					String newName = (String) renameField.get(null);
					newMigration = new Migration(null, new Query[]{
							new AlterTable(Settings.database.dbName, this.type.newInstance().tableName, new Operation[]{
									new RenameTable(newName)
							})
					}, new Query[0]);
				}
				this.genterateMigrationFile();
				this.migration = newMigration;
			} catch (NoSuchFieldException e1) {
				//This field does not exist so now get the differences in the class from the database.
				
				try {
					ArrayList<Query> queryDifferences = this.getTableDifferences();
					ArrayList<Query> foreignKeysAL = new ArrayList<Query>();
					
					if (queryDifferences.size() > 0) {
						ArrayList<Query> queryDifferencesTemp = (ArrayList<Query>) queryDifferences.clone();
						for (int i = 0; i < queryDifferencesTemp.size(); i ++) {
							Query query = queryDifferencesTemp.get(i);
							if (query instanceof AlterTable) {
								for (Operation op : ((AlterTable) query).operations) {
									if (op instanceof AddForeignKey) {
										foreignKeysAL.add(queryDifferences.get(i));
										queryDifferences.remove(i);
									}
								}
							}
						}
						Query[] queries = new Query[queryDifferences.size()];
						queries = queryDifferences.toArray(queries);
						Query[] foreignKeys = new Query[foreignKeysAL.size()];
						foreignKeys = foreignKeysAL.toArray(foreignKeys);
						if (this.getLastMigrationRan() == null) {
							this.genterateMigrationFile();
							this.migration = new Migration(this.getLastMigrationRan(), queries, foreignKeys);
						} else {
							//This is the initial migration
							this.genterateMigrationFile();
							this.migration = new Migration(null, queries, foreignKeys);
						}
					}
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<MigrationGenerator> getMigrations(){
		ArrayList<MigrationGenerator> migrations = new ArrayList<MigrationGenerator>();
//		ArrayList<PrintWriter> migrationWriter = new ArrayList<PrintWriter>();
		
		/*
		 * loop through synced models and evaluate the differences between this software and the database
		 */
		for (String tableName : Settings.syncedModels.keySet()) {
			Class<? extends Model> classType = Settings.syncedModels.get(tableName);
			MigrationGenerator migGen = new MigrationGenerator(classType);
			migGen.generate();
			migGen.printToSqlSheet();
			migrations.add(migGen);
		}
		
		return migrations;
	}
	
	public void printToSqlSheet() {
		try {
			String jsonToPrint = JSONFormat.formatJSON(ObjectParser.anyObjectToJSON(this.migration), 0);
			this.migrationWriter.print(jsonToPrint);
			this.migrationWriter.close();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
				| ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("No migration for class: " + this.type);
		}
	}
	
	public String toString() {
		try {
			return this.type.newInstance().tableName;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
