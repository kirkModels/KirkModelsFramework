package com.ianmann.database.orm.schema.queries;

public class DropConstraint extends ColumnOperation {

	// in this case, we will use this.fieldName as the symbol for the constraint.
	public DropConstraint(String _symbol) {
		super(_symbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMySqlString() {
		// TODO Auto-generated method stub
		return "DROP CONSTRAINT " + this.fieldName;
	}

	@Override
	public String getPsqlString() {
		// TODO Auto-generated method stub
		return "DROP CONSTRAINT " + this.fieldName;
	}

}
