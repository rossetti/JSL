package jslx.tabularfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputRow implements InputRowIfc {

    private final Map<Column, Object> myFields;
    private final List<Column> myColumns;
    private final List<String> myColumnNames;
    private final TabularFileIfc myTabularFile;

    InputRow(TabularFileIfc tabularFile){
        myTabularFile = tabularFile;
        myColumns = myTabularFile.getColumns();
        myColumnNames = myTabularFile.getColumnNames();
        myFields = new HashMap<>();
    }
    
    @Override
    public Column column(int index) {
        return myColumns.get(index);
    }

    @Override
    public Column column(String name) {
        return column(myColumnNames.indexOf(name));
    }

    @Override
    public double getDouble(Column column) {
        return 0;
    }

    @Override
    public int getInt(Column column) {
        return 0;
    }

    @Override
    public String getText(Column column) {
        return null;
    }

    @Override
    public boolean getBoolean(Column column) {
        return false;
    }

    @Override
    public long getLong(Column column) {
        return 0;
    }
}
