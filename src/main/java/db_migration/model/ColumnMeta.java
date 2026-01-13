package db_migration.model;

public class ColumnMeta {
	
	private String name;
    private int jdbcType;
    private int size;
    private boolean nullable;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getJdbcType() { return jdbcType; }
    public void setJdbcType(int jdbcType) { this.jdbcType = jdbcType; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

}
