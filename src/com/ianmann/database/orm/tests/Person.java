package com.ianmann.database.orm.tests;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.scema.fields.CharField;
import com.ianmann.database.orm.scema.fields.ForeignKey;
import com.ianmann.database.orm.scema.fields.IntegerField;
import com.ianmann.database.orm.scema.fields.ManyToManyField;

public class Person extends Model{
	
	public static QuerySet<Person> objects;

	public CharField name = new CharField("name", false, null, false, 45);
	public IntegerField age = new IntegerField("age", false, null, false, 150);
	public ForeignKey<Person> mother = new ForeignKey<Person>("mother", Person.class, true, null, false, "NO ACTION");
	public ForeignKey<Person> father = new ForeignKey<Person>("father", Person.class, true, null, false, "NO ACTION");
	public ManyToManyField<Person, Course> classes = new ManyToManyField<Person, Course>("classes", this, Course.class);
//	public IntegerField mother = new IntegerField("mother", true, null, false, null);
	public ManyToManyField<Person, Person> friends = new ManyToManyField<Person, Person>("friends", this, Person.class);
//	public ManyToManyField<Person, Person> enemies = new ManyToManyField<Person, Person>("enemies", this, Person.class);
//	public ManyToManyField<Person, Person> teachers = new ManyToManyField<Person, Person>("teachers", this, Person.class);
	public int fjslkfsjflasfjslfs;
	
	public String toString(){
		return "Person '" + this.name + "': " + this.age + " years old";
	}
}
