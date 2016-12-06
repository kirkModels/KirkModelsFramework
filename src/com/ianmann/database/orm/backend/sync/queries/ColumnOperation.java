package com.ianmann.database.orm.backend.sync.queries;

public abstract class ColumnOperation extends Operation {
	
	public String fieldName;

	public ColumnOperation(String _fieldName) {
		// TODO Auto-generated constructor stub
		this.fieldName = _fieldName;
	}

}
