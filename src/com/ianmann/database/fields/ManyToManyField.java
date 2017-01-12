package com.ianmann.database.fields;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.ianmann.database.config.Settings;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.Savable;
import com.ianmann.database.orm.queries.DeleteQuery;
import com.ianmann.database.orm.queries.InsertQuery;
import com.ianmann.database.orm.queries.SelectQuery;
import com.ianmann.database.orm.queries.UpdateQuery;
import com.ianmann.database.orm.queries.scripts.WhereCondition;
import com.ianmann.database.utils.exceptions.ObjectAlreadyExistsException;
import com.ianmann.database.utils.exceptions.ObjectNotFoundException;

import iansLibrary.utilities.JSONMappable;

public class ManyToManyField<T extends Model, R extends Model> extends Model implements Savable<R>, JSONMappable {
	
	public ForeignKey<T> reference1; // label: "host_<T>_id"
	public Class<T> hostClass;
	
	public ForeignKey<R> reference2; // label: "reference_<R>_id"
	public Class<R> refClass;
	
	public QuerySet<R> objectSet;
	public QuerySet<ManyToManyField<T, R>> objects;
	
	/**
	 * This is the constructor for the table and class definition. This does not cantain any actual relationships. to instantiate a relationship, use the constructor with a parent many2many field and and instance passed to it.
	 * @param _host
	 * @param _refClass
	 */
	public ManyToManyField(String _tableName, Model _host, Class<R> _refClass){
		this.hostClass = (Class<T>) _host.getClass();
		this.refClass = _refClass;
		
		String firstTable = _host.getClass().getSimpleName().toLowerCase();
		String refTable = _refClass.getSimpleName().toLowerCase();
		this.tableName = "mtm___" + _tableName + "___" + firstTable + "___" + refTable;
		
		this.reference1 = new ForeignKey<T>("host_" + firstTable + "_id", (Class<T>) _host.getClass(), false, null, false, "CASCADE");
		this.reference2 = new ForeignKey<R>("reference_" + refTable + "_id", _refClass, false, null, false, "CASCADE");
	}
	
	public ManyToManyField(String _label, Class<T> _hostClass, Class<R> _refClass){
		this.hostClass = _hostClass;
		this.refClass = _refClass;
		
		String firstTable = _hostClass.getSimpleName().toLowerCase();
		String refTable = _refClass.getSimpleName().toLowerCase();
		this.tableName = _label;
		
		this.reference1 = new ForeignKey<T>("host_" + firstTable + "_id", _hostClass, false, null, false, "CASCADE");
		this.reference2 = new ForeignKey<R>("reference_" + refTable + "_id", _refClass, false, null, false, "CASCADE");
	}
	
	/**
	 * This is an instance for an actual relationship. to asave it, call this.saveRelationship. don't use the following methods:
	 * <br>
	 * <br>
	 * * all
	 * <br>
	 * * filter
	 * <br>
	 * * create
	 * <br>
	 * * get
	 * <br>
	 * * getOrCreate
	 * <br>
	 * <br>
	 * These methods should not be used because it is a specific relationship... not a single one.
	 * <br>
	 * <br>
	 * <br>
	 * <br>
	 * @param parent
	 * @param instance
	 */
	public ManyToManyField(){
		this.reference1 = new ForeignKey<T>();
		this.reference2 = new ForeignKey<R>();
	}
	
	@Override
	public Constructor getJsonConstructor(){
		Class[] paramTypes = new Class[]{
				String.class,
				Class.class,
				Class.class,
		};
		try {
			return this.getClass().getConstructor(paramTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String[] getConstructorFieldOrder() {
		return new String[]{
				"tableName",
				"hostClass",
				"refClass",
		};
	}
	
	public QuerySet<R> getRelatedObjects() {
		QuerySet<R> values = null;
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Class<R> refClass = null;
		refClass = (Class<R>) this.refClass;
		Class m2mClass = this.getClass();
		
		QuerySet<ManyToManyField<T, R>> tempQ = new QuerySet<ManyToManyField<T, R>>(m2mClass, this.tableName, new ArrayList<WhereCondition>(){{
			add(new WhereCondition(reference1.label, WhereCondition.EQUALS, reference1.val()));
		}});
		
		objects = tempQ;
		
		ArrayList<WhereCondition> conditions = new ArrayList<WhereCondition>();
		
		for (ManyToManyField<T, R> relationship : tempQ) {
			int relId = relationship.reference2.val();
			ids.add(relId);
		}
		
		if(ids.size() > 1){
			
			WhereCondition c = new WhereCondition("id", WhereCondition.CONTAINED_IN, ids);
			conditions.add(c);
			
		} else if (ids.size() == 1) {
			
			WhereCondition c = new WhereCondition("id", WhereCondition.EQUALS, ids.get(0));
			conditions.add(c);
			
		} else {
			
			WhereCondition c = new WhereCondition("id", WhereCondition.EQUALS, 0);
			conditions.add(c);
			
		}
		
		values = Model.getObjectsForGenericType(refClass).filter(conditions);
		values.conditions = new ArrayList<WhereCondition>();
		
		this.objectSet = values;
		return this.objectSet;
	}
	
	public SavableField getField(String fieldName){
		
		SavableField field = null;
		
		if (fieldName.contains("reference_")) {
			field =  this.reference2;
		} else if (fieldName.contains("host_")){
			field =  this.reference1;
		} else if (fieldName.equals("id")){
			field =  this.id;
		} else {
			field =  super.getField(fieldName);
		}
		
		return field;
	}
	
	public QuerySet<R> refClassObjects(){
		return (QuerySet<R>) Model.getObjectsForGenericType((Class<T>) this.refClass);
	}
	
	public int getNewId(ManyToManyField instance){
		int newId = this.count() + 1;
		boolean idWorks = false;
		
		while (!idWorks){
			newId ++;
			
			ManyToManyField<T, R> rel = null;
			
			WhereCondition c = new WhereCondition("id", WhereCondition.EQUALS, newId);
			
			try {
				rel = this.objects.get(new ArrayList<WhereCondition>(){{
					add(c);
				}});
			} catch (ObjectNotFoundException e) {
				// ID is not taken so use it.
				idWorks = true;
				break;
			}
			
			if (rel == null) {
				// not found so id is unique
				idWorks = true;
				break;
			}
		}
		
		return newId;
	}
	
	protected ManyToManyField<T, R> saveRelationship(R instance){
		ManyToManyField<T, R> newRelationship = new ManyToManyField<>();
		
		// need to find total number of relationships in the m2m table and use that for new id
		SelectQuery allRelsResults = new SelectQuery(this.tableName, new ArrayList<WhereCondition>(){{}});
		try {
			allRelsResults.run();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int newId = this.getNewId(newRelationship);
		
		newRelationship.id.set(newId);
		
		newRelationship.tableName = this.tableName;
		
		newRelationship.reference1.set(this.reference1.val());
		newRelationship.reference1.label = this.reference1.label;
		
		newRelationship.reference2.set(instance.id.val());
		newRelationship.reference2.label = this.reference2.label;
		
		InsertQuery query = new InsertQuery(newRelationship);
		try {
			query.run();
			this.objects.storage.add(newRelationship);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return newRelationship;
	}
	
	protected ManyToManyField<T, R> getRelationship(ManyToManyField<T, R> parentFieldDef, R instance){
		ManyToManyField<T, R> rel = null;
		
		try {
			rel = (ManyToManyField<T, R>) this.objects.get(new ArrayList<WhereCondition>(){{
				add(new WhereCondition(parentFieldDef.reference2.label, WhereCondition.EQUALS, instance.id.val()));
			}});
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rel;
	}
	
	protected void deleteRelationship(R instance){
		ManyToManyField<T, R> relToDelete = this.getRelationship(this, instance);
		
		DeleteQuery query = new DeleteQuery(this.tableName, new ArrayList<WhereCondition>(){{
			add(new WhereCondition("id", WhereCondition.EQUALS, relToDelete.id.val()));
		}});
		
		try {
			query.run();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected QuerySet<ManyToManyField<T, R>> getAllRelationships(){
		int id = this.reference1.val();
		
		//get all relationships from this table including other host relationships
		QuerySet<ManyToManyField<T, R>> allRelationships = new QuerySet<ManyToManyField<T, R>>((Class<ManyToManyField<T, R>>) this.getClass());
		//filter down to just the ones related to this reference1
		allRelationships = allRelationships.filter(new ArrayList<WhereCondition>(){{ add(new WhereCondition(reference1.label, WhereCondition.EQUALS, id)); }});
		
		return allRelationships;
	}

	@Override
	public QuerySet<R> all() {
		return this.objectSet.all();
	}

	@Override
	public R create(ArrayList<WhereCondition> conditions) throws ObjectAlreadyExistsException, SQLException {
		R newInstance = this.objectSet.create(conditions);
		
		this.add(newInstance);
		
		return newInstance;
	}
	
	public R add(R instance) throws ObjectAlreadyExistsException{
		
		if(this.objectSet.filter(new ArrayList<WhereCondition>(){{
			add(new WhereCondition("id", WhereCondition.EQUALS, instance.id.val()));
		}}).count() == 0) {
		
			this.saveRelationship(instance);
			
			if (!this.objectSet.storage.contains(instance)) {
				
				this.objectSet.storage.add(instance);
				
			}
			
			return instance;
			
		} else {
			throw new ObjectAlreadyExistsException(this.refClass.getSimpleName() + " instance with id " + instance.id.val() + " already is related to this object.");
		}
		
	}

	@Override
	public R get(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException {
		return this.objectSet.get(conditions);
	}

	@Override
	public Entry<R, Boolean> getOrCreate(ArrayList<WhereCondition> conditions) throws SQLException {
		
		try{
			R result = this.get(conditions);
			return new Entry<R, Boolean>() {

				@Override
				public R getKey() {
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
				R instance = this.create(conditions);
				
				return new Entry<R, Boolean>() {
	
					@Override
					public R getKey() {
						// TODO Auto-generated method stub
						return instance;
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
				return null;
			}
		}
	}
	
	public Entry<R, Boolean> getOrAdd(ArrayList<WhereCondition> conditions) throws SQLException, ObjectNotFoundException {
		
		try{
			R result = this.get(conditions);
			return new Entry<R, Boolean>() {

				@Override
				public R getKey() {
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
			try{
				R instance = this.refClassObjects().get(conditions);
				this.add(instance);
				
				return new Entry<R, Boolean>() {
	
					@Override
					public R getKey() {
						// TODO Auto-generated method stub
						return instance;
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
				return null;
			}
		}
	}

	@Override
	public QuerySet<R> filter(ArrayList<WhereCondition> conditions) {
		return this.objectSet.filter(conditions);
	}

	@Override
	public void delete(ArrayList<WhereCondition> conditions) throws ObjectNotFoundException {
		
		QuerySet<R> instances = this.filter(conditions);
		
		for(R instance : instances){
			this.remove(instance);
			instance.delete();
		}
		
		if (instances.count() == 0) {
			throw new ObjectNotFoundException("Sorry, " + this.refClass.getSimpleName()
					+ " intance with conditions: " + conditions + " does not exist in this relationship.");
		}
		
	}
	
	public void remove(R instance) throws ObjectNotFoundException{
		R tempInstance = null;
		
		tempInstance = this.get(new ArrayList<WhereCondition>(){{
			add(new WhereCondition("id", WhereCondition.EQUALS, instance.id.val()));
		}});
		
		if(tempInstance != null){
			this.deleteRelationship(instance);
		} else {
			throw new ObjectNotFoundException("Sorry, " + instance.getClass().getSimpleName() + " instance with id: " + instance.id.val()
								+ " does not exists.");
		}
		
		this.objectSet.storage.remove(instance);
	}
	
	public void setHostId(int id){
		this.reference1.set(id);
	}

	@Override
	public void initializeManyToManyFields() {}

	@Override
	public int count() {
		// TODO Auto-generated method stub
		return this.objectSet.count();
	}
}
