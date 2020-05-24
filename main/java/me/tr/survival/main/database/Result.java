package me.tr.survival.main.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Deprecated
// Class not used nor continued
public class Result {

    private Map<String, Object> values;
    private boolean next;

    public Result(Map<String, Object> values, boolean next) {
        this.values = values;
        this.next = next;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public int getInt(String columnName) {
        return (int) values.get(columnName);
    }

    public long getLong(String columnName) {
        return (long) values.get(columnName);
    }

    public String getString(String columnName) {
        return String.valueOf(values.get(columnName));
    }

    public boolean getBoolean(String columnName) {
        return (boolean) values.get(columnName);
    }

    public double getDouble(String columnName) {
        return (double) values.get(columnName);
    }

    public boolean next() {
        return this.next;
    }

    public static Result fromResultSet(ResultSet result) throws SQLException {

        Map<String, Object> values = new HashMap<>();

        for(int i = 0; i < result.getMetaData().getColumnCount(); i++) {
            values.put(result.getMetaData().getColumnName(i), result.getObject(i));
        }
        values.forEach((key, value) -> System.out.println(key + ":" + value));
        return new Result(values, result.next());
    }

}
