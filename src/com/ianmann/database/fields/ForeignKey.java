package com.ianmann.database.fields;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.queries.scripts.WhereCondition;
import com.ianmann.database.utils.exceptions.ObjectNotFoundException;

import iansLibrary.utilities.JSONMappable;

public class ForeignKey<T extends Model> extends IntegerField implements JSONMappable {
	
	public T referencedInstant;
	public Class<T> referenceClass;
	public String symbol;
	
	public String onDelete;

	public ForeignKey(String _label, Class<T> _referenceClass, Boolean _isNull, Integer _defaultValue, Boolean _unique, String _onDelete) {
		super(_label, _isNull, _defaultValue, _unique, null);
		// TODO Auto-generated constructor stub
		this.referenceClass = _referenceClass;
		if(_defaultValue != null){
			this.set(_defaultValue);
		}
		this.onDelete = _onDelete;
		this.symbol = ("fk__" + _label + "__" + _referenceClass.getSimpleName() + "__id").toLowerCase();
	}
	
	public ForeignKey(String label, String _symbol, Class<T> reference, Boolean isNull, Integer defaultValue, Boolean unique, String onDelete) {
		super(label, isNull, defaultValue, unique, null);
		// TODO Auto-generated constructor stub
		this.referenceClass = reference;
		if(defaultValue != null){
			this.set(defaultValue);
		}
		this.onDelete = onDelete;
		this.symbol = _symbol;
	}
	
	public ForeignKey() {
		super("", true, null, false, null);
	}
	
	public Constructor getJsonConstructor(){
		Class[] paramTypes = new Class[]{
				String.class,
				Class.class,
				Boolean.class,
				Integer.class,
				Boolean.class,
				String.class
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
				"label",
				"referenceClass",
				"isNull",
				"defaultValue",
				"unique",
				"onDelete"
		};
	}
	
	/**
	 * set the Model that this field will reference
	 * @param value
	 */
	public void setObject(T value){
		// T in this case is not an int, but the object instance that is being referenced
		if (value.id.val() == null) {
			try {
				throw new Exception(value.getClass() + " object 'value' does not exist in the database.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			int valueID = (Integer)value.id.val();
			this.value = valueID;
			this.referencedInstant = value;
		}
	}
	
	public T getRef() throws ObjectNotFoundException{
		
		if (this.value != null && this.referencedInstant != null) {
			return this.referencedInstant;
		} else if (this.value != null && this.referencedInstant == null) {
			ArrayList<WhereCondition> conditions = new ArrayList<WhereCondition>();
			WhereCondition id = new WhereCondition("id", WhereCondition.EQUALS, value);
			conditions.add(id);
			return Model.getObjectsForGenericType(this.referenceClass).get(conditions);
		} else if (this.value == null) {
			return null;
		}
		
		return null;
	}

	@Override
	public String MySqlString() {
		String sql = super.MySqlString();
		sql = sql + "::REFERENCES " + this.referenceClass.getName().replace(".", "_").toLowerCase() + " (id)\n\t\tON UPDATE CASCADE ON DELETE " + this.onDelete;
		return sql;
	}
	
	public String PSqlString(){
		String sql = super.PSqlString();
		sql = sql + "::REFERENCES " + this.referenceClass.getName().replace(".", "_").toLowerCase() + " (id)\n\t\tON UPDATE CASCADE ON DELETE " + this.onDelete;
		return sql;
	}
	
	@Override
	public String toString(){
		T ref_value = null;
		
		try {
			ref_value = this.getRef();
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			return super.toString();
		}
		
		if(ref_value != null){
			return ref_value.toString();
		} else {
			return "NONE";
		}
	}

	//FOREIGN KEY (" + this.referenceClass.getSimpleName().toLowerCase() + "_id, " + this.label + ")\n\t\t
}
