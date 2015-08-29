package tests;

import java.util.HashMap;

import kirkModels.objects.CharField;
import kirkModels.objects.IntegerField;
import kirkModels.objects.Model;
import kirkModels.objects.SQLField;

public class TestModel extends Model {
	
	public String name;
	public Integer age;

	@SuppressWarnings("serial")
	public TestModel(String name, int age) {
		super(new HashMap<String, SQLField<?>>(){{
			put("name", new CharField("name", false, "Nothing", true, 20));
			put("age", new IntegerField("age", true, null, false, false, 150));
		}});
		// TODO Auto-generated constructor stub
		this.sqlFields.get("name").set(name);
		this.sqlFields.get("age").set(age);
	}
	
	@SuppressWarnings("serial")
	public TestModel(){
		super(new HashMap<String, SQLField<?>>(){{
			put("name", new CharField("name", false, "Nothing", true, 20));
			put("age", new IntegerField("age", true, null, false, false, 150));
		}});
	}

}
