package com.ianmann.database.tests;

import com.ianmann.database.fields.CharField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;

public class Course extends Model {

	public static QuerySet<Course> objects;
	
	public CharField name = new CharField("name", false, null, true, 100);
	public CharField designator = new CharField("designator", false, null, true, 100);
	
	
}
