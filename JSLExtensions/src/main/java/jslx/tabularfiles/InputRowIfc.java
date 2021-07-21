package jslx.tabularfiles;

import java.util.Objects;

public interface InputRowIfc {

    Column column(int index);
    Column column(String name);

    double getDouble(Column column);
    int getInt(Column column);
    String getText(Column column);
    boolean getBoolean(Column column);
    long getLong(Column column);

    default double getDouble(int index){
        return getDouble(column(index));
    }

    default int getInt(int index){
        return getInt(column(index));
    }

    default String getText(int index){
        return getText(column(index));
    }

    default boolean getBoolean(int index){
        return getBoolean(column(index));
    }

    default long getLong(int index){
        return getLong(column(index));
    }

    default double getDouble(String columnName){
        return getDouble(column(columnName));
    }

    default int getInt(String columnName){
         return getInt(column(columnName));
    }

    default String getText(String columnName){
        return getText(column(columnName));
    }

    default boolean getBoolean(String columnName){
         return getBoolean(column(columnName));
    }

    default long getLong(String columnName){
        return getLong(column(columnName));
    }

}
