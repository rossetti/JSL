package jslx.tabularfiles;

import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.jooq.*;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.UpdatableRecordImpl;

public class TestTabularWork {

    public static void main(String[] args) {
        DatabaseIfc db = DatabaseFactory.createSQLiteDatabase("test.db");
        DSLContext dsl = db.getDSLContext();
        dsl.createTable("Data")
                .column("c1", SQLDataType.DOUBLE)
                .column("c2", SQLDataType.INTEGER)
                .column("c3", SQLDataType.BIGINT)
                .column("c4", SQLDataType.VARCHAR).execute();

        Table<?> table = db.getTable("Data");
        Record record = table.newRecord();

        Field<Double> field = (Field<Double>) record.field("c1");
        System.out.println(field);
//        Row row = record.fieldsRow();
//        System.out.println(row);
//        record.setValue(field, Double.valueOf(20.0));
//
//        Result<Record> records = db.selectAll("Data");
        //dsl.batchStore(records).

        UpdatableRecordImpl record1 = new UpdatableRecordImpl(table);

        record1.set(field, Double.valueOf(20.0));
//        record1.store();


        db.printTableAsText("Data");
    }
}
