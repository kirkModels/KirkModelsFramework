package com.ianmann.database.orm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.ianmann.database.orm.queries.scripts.WhereCondition;
import com.ianmann.database.utils.exceptions.ObjectAlreadyExistsException;
import com.ianmann.database.utils.exceptions.ObjectNotFoundException;

public interface Savable <T extends Model> {
	
	public QuerySet<T> all();
	
	public T create(ArrayList<WhereCondition> conditions) throws ObjectAlreadyExistsException, SQLException;
	
	public T get(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException;
	
	public Entry<T, Boolean> getOrCreate(ArrayList<WhereCondition> conditions) throws SQLException;
	
	public QuerySet<T> filter(ArrayList<WhereCondition> conditions);
	
	public void delete(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException;
	
	public int count();
}
