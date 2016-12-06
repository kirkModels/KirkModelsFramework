package com.ianmann.database.orm.queries;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.queries.scripts.WhereCondition;

public abstract class WhereConditionedQuery extends Query {

	public ArrayList<WhereCondition> conditions = new ArrayList<WhereCondition>();

	public WhereConditionedQuery(String _dbName, String _tabelName, ArrayList<WhereCondition> _conditions) {
		super(_dbName, _tabelName);
		// TODO Auto-generated constructor stub
		
		this.conditions = _conditions;
	}

	@Override
	public String getMySqlString() {
		// TODO Auto-generated method stub
		String str = "";
		
		if(this.conditions.size() > 0){
			str = str + " WHERE ";
		}
		int i = 0;
		for(WhereCondition condition : this.conditions){
			str = str + condition.getMySqlString();
			i ++;
			if(i < this.conditions.size()){
				str = str + " AND ";
			}
		}
		
		return str;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		String str = "";
		
		if(this.conditions.size() > 0){
			str = str + " WHERE ";
		}
		
		int i = 0;
		for(WhereCondition condition : this.conditions){
			str = str + condition.getPsqlString();
			i ++;
			if(i < this.conditions.size()){
				str = str + " AND ";
			}
		}
		
		return str;
	}

}
