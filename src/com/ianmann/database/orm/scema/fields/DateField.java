package com.ianmann.database.orm.scema.fields;

import java.util.Date;

public class DateField extends CharField {

	public boolean autoNow;
	public boolean autoNowAdd;
	
	public Date dateVal;
	
	@Deprecated
	public DateField(String label, Boolean isNull, Boolean autoNow, Boolean autoNowAdd) {
		super(label, isNull, null, false, 100);
		// TODO Auto-generated constructor stub
		
		this.autoNow = autoNow;
		this.autoNowAdd = autoNowAdd;
		
		if (autoNow) {
			this.setNow();
		}
	}
	
	public Date setNow(){
		Date now = new Date();
		
		this.set(now);
		
		return now;
	}
	
	@Override
	public void set(Object val) {
		
		if(String.class.isAssignableFrom(val.getClass())) {
			
			this.setAsString((String) val);
			
		} else if (Date.class.isAssignableFrom(val.getClass())) {
			
			this.setAsJavaDate((Date) val);
			
		} else if (java.sql.Date.class.isAssignableFrom(val.getClass())) {
			
			this.setAsSqlDate((java.sql.Date) val);
			
		}
	}
	
	public void setAsString(String val) {
		java.sql.Date sqlDate = java.sql.Date.valueOf(val);
		
		this.dateVal = new Date(sqlDate.getTime());
		
		super.set(val);
	}
	
	public void setAsJavaDate(Date val) {
		this.dateVal = val;
		
		super.set(this.toSqlDate().toString());
	}
	
	public void setAsSqlDate(java.sql.Date val) {
		this.dateVal = new Date(val.getTime());
		
		super.set(val.toString());
	}
	
	public void setAsLong(long val) {
		this.dateVal = new Date(val);
		
		super.set(this.toSqlDate().toString());
	}
	
	public java.sql.Date toSqlDate() {
		
		long dateLong = this.dateVal.getTime();
		
		java.sql.Date sqlVal = new java.sql.Date(dateLong);
		
		return sqlVal;
	}

	@Override
	public String MySqlString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String PSqlString() {
		// TODO Auto-generated method stub
		return null;
	}

}
