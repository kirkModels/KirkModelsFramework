package iansLibrary.data.databases;

public class MetaTableColumn {
	
	public static final int COLUMN_NAME = 4;
	public static final int DATA_TYPE_NAME = 5;
	public static final int NULLABLE = 11;
	public static final int COLUMN_DEF = 13;
	public static final int COLUMN_SIZE = 7;

	private String columnName;
	private int dataType;
	private int nullable;
	private Object defaultValue;
	private int columnSize;

	public MetaTableColumn(String _columnName, int _dataType, int _nullable, Object _defaultValue, int _columnSize) {
		super();
		this.columnName = _columnName;
		this.dataType = _dataType;
		this.nullable = _nullable;
		this.defaultValue = _defaultValue;
		this.columnSize = _columnSize;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getDataType() {
		return dataType;
	}

	public int getNullable() {
		return nullable;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public int getColumnSize() {
		return columnSize;
	}
	
	
}
