/**
 * @TODO: TODO
 *
 * @author Ian
 * Created: Feb 11, 2018
 */
package com.ianmann.database.orm.queries;

/**
 * @TODO: TODO
 *
 * @author Ian
 * Created: Feb 11, 2018
 *
 */
public class InnerJoin {
	
	public static enum Comparator {
		EQUAL("="), LESS_THAN("<"), GREATER_THAN(">"), LESS_THAN_EQUAL("<="), GREATER_THAN_EQUAL(">=");
		public String value;
		Comparator(String value) { this.value = value; }
	}

	public String leftTableName;
	public String rightTableName;
	public String leftFieldName;
	public String rightFieldName;
	public Comparator comparator;
}
