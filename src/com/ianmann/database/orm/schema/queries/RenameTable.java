package com.ianmann.database.orm.schema.queries;

public class RenameTable extends Operation {
	
	protected String newName;

	public RenameTable(String _newName) {
		this.newName = _newName;
	}

	@Override
	public String getMySqlString() {
		// TODO Auto-generated method stub
		return "RENAME TO " + this.newName;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		return "RENAME TO " + this.newName;
	}

}
