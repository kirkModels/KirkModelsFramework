package com.ianmann.database.orm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.ianmann.database.orm.config.Settings;
import com.ianmann.database.orm.queries.InsertQuery;
import com.ianmann.database.orm.queries.SelectQuery;
import com.ianmann.database.orm.queries.WhereCondition;
import com.ianmann.database.orm.scema.fields.ManyToManyField;
import com.ianmann.database.orm.scema.fields.SavableField;
import com.ianmann.database.orm.utils.exceptions.ObjectAlreadyExistsException;
import com.ianmann.database.orm.utils.exceptions.ObjectNotFoundException;

public class QuerySet<T extends Model> implements Savable<T>, Iterable<T>{
	
	public ArrayList<T> storage;
	public ResultSet results;
	private Class<T> type;
	public ArrayList<WhereCondition> conditions;
	String tableName;
	
	public QuerySet(ResultSet results, ArrayList<WhereCondition> conditions){
		this.results = results;
		this.conditions = conditions;
		this.setTableName();
		this.updateStorage();
	}
	
	public QuerySet(Class<T> type){
		this.type = type;
		try {
			this.tableName = this.type.newInstance().tableName;
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.tableName = this.type.getName().replace(".", "_");
		}
		
		SelectQuery query = new SelectQuery(this.tableName, new ArrayList<WhereCondition>());
		try {
			query.run();
			this.results = query.results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.conditions = new ArrayList<WhereCondition>();
		this.updateStorage();
	}
	
	public QuerySet(Class<T> type, ArrayList<WhereCondition> conditions){
		this.type = type;
		this.tableName = this.type.getName().replace(".", "_");
		
		SelectQuery query = new SelectQuery(this.tableName, conditions);
		try {
			query.run();
			this.results = query.results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.conditions = conditions;
		this.updateStorage();
	}
	
	// creates an empty queryset that the user can then go and fill the storage with.
	public QuerySet(Class<T> type, String tableName) {
		this.type = type;
		this.tableName = tableName;
		
		this.conditions = new ArrayList<WhereCondition>();
		
		this.storage = new ArrayList<T>();
	}
	
	public QuerySet(Class<T> type, String tableName, ArrayList<WhereCondition> conditions){
		this.type = type;
		this.tableName = tableName;

		SelectQuery query = new SelectQuery(this.tableName, conditions);
		try {
			query.run();
			this.results = query.results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.conditions = conditions;
		this.updateStorage();
	}
	
	/**
	 * Returns this {@code QuerySet} as an {@code ArrayList}.
	 * @return
	 */
	public ArrayList<T> asArrayList() {
		ArrayList<T> list = new ArrayList<T>();
		for (T instance : this.all()) {
			list.add(instance);
		}
		return list;
	}
	
	/**
	 * Returns this {@code QuerySet} as an {@code LinkedList}.
	 * @return
	 */
	public LinkedList<T> asLinkedList() {
		LinkedList<T> list = new LinkedList<T>();
		for (T instance : this.all()) {
			list.add(instance);
		}
		return list;
	}
	
	private void updateStorage(){
		this.storage = new ArrayList<T>();
		if(this.results == null){

			SelectQuery query = new SelectQuery(this.tableName, this.conditions);
			try {
				query.run();
				this.results = query.results;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		try {
			
			while (this.results.next()) {
				
				int index = 0;
				
				try {
					index = this.results.getRow();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (index > 0) {
					T newInstance = null;
					try {
						newInstance = this.getObjectFromResults(index);
						
						this.storage.add(newInstance);
						
						newInstance.initializeManyToManyFields();
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getM2MObject(ManyToManyField object, ResultSet results) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchMethodException, SecurityException, NoSuchFieldException{
		String col1Name = results.getMetaData().getColumnName(1);
		String col2Name = results.getMetaData().getColumnName(2);
		String col3Name = results.getMetaData().getColumnName(3);
		
		object.getField(col1Name).set(results.getInt(col1Name));
		object.getField(col1Name).label = col1Name;
		
		object.getField(col2Name).set(results.getInt(col2Name));
		object.getField(col2Name).label = col2Name;
		
		object.getField(col3Name).set(results.getInt(col3Name));
		object.getField(col3Name).label = col3Name;
		
		object.tableName = results.getMetaData().getTableName(1);
	}
	
	private void getDbObject(T object, ResultSet results) throws NoSuchFieldException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SQLException{
		for (int i = 0; i < object.savableFields.size(); i++) {
			String fieldNameTemp = object.savableFields.get(i);
			String fieldName = object.getField(fieldNameTemp).label;
			Class<?> fieldType = object.getClass().getField(fieldNameTemp).getType();
			if (fieldType.isAssignableFrom(ManyToManyField.class)) {
				Class<?>[] cArg = new Class[1];
				cArg[0] = Object.class;
				fieldType.getMethod("getObjects", cArg).invoke(object, new Object[0]);
			}
			else{
				Class<?>[] cArg = new Class[1];
				cArg[0] = Object.class;
				
				SavableField field = (SavableField) object.getClass().getField(fieldNameTemp).get(object);
				
				Method getMethod = fieldType.getMethod("set", cArg);
				Object fieldVal = this.results.getObject(fieldName);
				
				if(fieldVal != null){
					getMethod.invoke(field, fieldVal);
				}
			}
		}
	}
	
	private T getObjectFromResults(int index) throws IndexOutOfBoundsException{
		T object = null;
		try {
			if (this.cursorToRow(index)) {
				object = type.newInstance();
				if (ManyToManyField.class.isAssignableFrom(object.getClass())) {
					this.getM2MObject((ManyToManyField) object, this.results);
				}else {
					this.getDbObject(object, results);
				}
			}
			else {
				// throw an error because the results don't have a value at this index.
				throw new IndexOutOfBoundsException("There is no object at the index: " + index);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return object;
	}
	
	public void setTableName(){
		try {
			this.tableName = this.results.getMetaData().getTableName(1);
			this.type = (Class<T>) Settings.syncedModels.get(this.tableName.replace('_', '.'));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<WhereCondition> combineConditions(ArrayList<WhereCondition> newConditions){
		ArrayList<WhereCondition> tempNewConditions = new ArrayList<WhereCondition>();
		
		for(WhereCondition condition : this.conditions){
			if (!tempNewConditions.contains(condition)) {
				tempNewConditions.add(condition);
			}
		}
		
		for (WhereCondition condition : newConditions) {
			if (!tempNewConditions.contains(condition)) {
				tempNewConditions.add(condition);
			}
		}
		
		return tempNewConditions;
	}
	
	public boolean cursorToRow(int i) throws SQLException{
		this.results.first();
		this.results.previous();
		boolean found = false;
		int count = 0;
		while(!found){
			if(this.results.next()){
				count ++;
			}
			else{
				break;
			}
			if(count == i){
				found = true;
			}
		}
		if(count == i){
			found = true;
		}
//		while(this.results.previous()){}
		return found;
	}

	public String toString(){
		String str = "<";
		
		for(int i = 0; i < this.count(); i ++){
			if(i > 0){
				str = str + ", ";
			}
			
			@SuppressWarnings("unchecked")
			T reference = null;
			reference = this.getRow(i);
			
			str = str + reference.toString();
		}
		
		str = str + ">";
		return str;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return this.storage.iterator();
	}
	
	public int indexOf(T other){
		for(int i = 0; i < this.storage.size(); i ++){
			T object = this.storage.get(i);
			if(object.id.val() == other.id.val()){
				return i;
			}
		}
		return -1;
	}
	
	public String getTableName(){
		return this.tableName;
	}
	
	public static Object[] conditionsContain(ArrayList<WhereCondition> cs, String variableName){
		for (WhereCondition c : cs) {
			if(c.fieldName.equals(variableName)){
				return new Object[]{true, c};
			}
		}
		return new Object[]{false};
	}
	
	
	
	

	
	
	public T getById(int id) throws ObjectNotFoundException{
		for (T instance : this.storage) {
			
			if (instance.id.val() == id) {
				return instance;
			}
			
		}
		
		// if loop finishes and no instance is returned, throw an error cause no instance has this id.
		throw new ObjectNotFoundException(this.type.getSimpleName() + " with id of " + id + " does not exist.");
	}
	
	public T getRow(int i){
		T instance = null;

		instance = this.storage.get(i);
		
		return instance;
	}
	
	@Override
	public int count(){
		return this.storage.size();
	}
	
	@Override
	public T create(ArrayList<WhereCondition> conditions) throws ObjectAlreadyExistsException, SQLException {
		conditions = this.combineConditions(conditions);
		
		if (((Boolean) conditionsContain(conditions, "id")[0])) {
			WhereCondition c = ((WhereCondition) conditionsContain(conditions, "id")[1]);
			int id = (int) c.value;
			
			if(this.filter(new ArrayList<WhereCondition>(){{
								add(c);
							}}).count() > 0)
			{
				throw new ObjectAlreadyExistsException(this.type.getSimpleName() + " object with id: " + id
						+ " already exists.");
			}
			
		}
		
		T newInstance = null;
		try {
			newInstance = this.type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (WhereCondition condition : conditions) {
			Object value = condition.value;
			newInstance.getField(condition.fieldName).set(value);
		}
		
		{
			int newId = Model.getNewId(newInstance);
			newInstance.id.set(newId);
			
			InsertQuery query = new InsertQuery(newInstance);
			query.run();
		}
		
		this.storage.add(newInstance);
		
		return newInstance;
	}

	@Override
	public T get(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException {
		conditions = this.combineConditions(conditions);
		
		QuerySet<T> set = this.filter(conditions);
		
		if(set.count() == 1){
			return set.getRow(0);
		} else if (set.count() == 0) {
			
			throw new ObjectNotFoundException("Found no results of " + this.type + " instance for kwargs: "
								+ conditions);
		} else {
			
			try {
				throw new Exception("Found several results of " + this.type + " instance for kwargs: "
									+ Arrays.toString(conditions.toArray()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
	}

	@Override
	public Entry<T, Boolean> getOrCreate(ArrayList<WhereCondition> conditions) throws SQLException{
		conditions = this.combineConditions(conditions);
		
		try{
			T result = this.get(conditions);
			return new Entry<T, Boolean>() {

				@Override
				public T getKey() {
					// TODO Auto-generated method stub
					return result;
				}

				@Override
				public Boolean getValue() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Boolean setValue(Boolean value) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		} catch (ObjectNotFoundException e) {
			try {
				T newInstance = this.create(conditions);
				return new Entry<T, Boolean>() {

					@Override
					public T getKey() {
						// TODO Auto-generated method stub
						return newInstance;
					}

					@Override
					public Boolean getValue() {
						// TODO Auto-generated method stub
						return true;
					}

					@Override
					public Boolean setValue(Boolean value) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			} catch (ObjectAlreadyExistsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
		}
	}
	
	public void addOperators(ArrayList<WhereCondition> conditions, int operator){
		
		for (WhereCondition c : conditions) {
			c.type = operator;
		}
	}

	@Override
	public QuerySet<T> filter(ArrayList<WhereCondition> conditions) {
		
		ArrayList<WhereCondition> tempConditions = this.combineConditions(conditions);
		
		//empty queryset
		QuerySet<T> newQuerySet = new QuerySet<T>(this.type, this.tableName);
		
		for (T instance : this.storage) {
			if(instance.meetsConditions(tempConditions)){
				newQuerySet.storage.add(instance);
			}
		}
		
		newQuerySet.conditions = tempConditions;
		
		return newQuerySet;
	}

	@Override
	public QuerySet<T> all() {
		return this;
	}
	
	public boolean exists(ArrayList<WhereCondition> conditions){
		QuerySet<T> instances = this.filter(conditions);
		
		if (instances.count() > 0) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean exists(T instance) {
		try {
			boolean exists = this.exists(new ArrayList<WhereCondition>(){{
				add(new WhereCondition("id", WhereCondition.EQUALS, instance.id.val()));
			}});
			
			return exists;
		} catch (NullPointerException e) {
			return false;
		}
	}

	@Override
	public void delete(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException {
		conditions = this.combineConditions(conditions);
		QuerySet<T> results = this.filter(conditions);
		if (results.count() < 1) {
			throw new ObjectNotFoundException(this.type + " object with kwargs: " + conditions + " does not exist.");
		}
		else {
			for (T instance : results) {
				int index = this.indexOf(instance);
				instance.delete();
				this.storage.remove(index);
			}
		}
	}
}
