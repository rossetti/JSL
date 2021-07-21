package jslx.tabularfiles;

import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.jooq.*;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.UpdatableRecordImpl;

import static org.jooq.impl.DSL.constraint;

public class TestTabularWork {

    public static void main(String[] args) {
        DatabaseIfc db = DatabaseFactory.createSQLiteDatabase("test.db");
        DSLContext dsl = db.getDSLContext();
        dsl.createTable("Data")
                .column("ID", SQLDataType.INTEGER)
                .column("c1", SQLDataType.DOUBLE)
                .column("c2", SQLDataType.INTEGER)
                .column("c3", SQLDataType.BIGINT)
                .column("c4", SQLDataType.VARCHAR)
                .constraints(
                        constraint("PK").primaryKey("ID"))
                .execute();

        Table<?> table = db.getTable("Data");
        Record record = table.newRecord();


        Field<Double> field1 = (Field<Double>) record.field("c1");
        Field<Integer> field2 = (Field<Integer>) record.field("c2");
        Field<Long> field3 = (Field<Long>) record.field("c3");
        Field<String> field4 = (Field<String>) record.field("c4");
        Field<Integer> field0 = (Field<Integer>) record.field("ID");

        System.out.println(field1);
//        Row row = record.fieldsRow();
//        System.out.println(row);
//        record.setValue(field, Double.valueOf(20.0));
//
//        Result<Record> records = db.selectAll("Data");
        //dsl.batchStore(records).

        UpdatableRecordImpl record1 = new UpdatableRecordImpl(table);

        record1.set(field1, Double.valueOf(20.0));
        dsl.insertInto(table, field0, field1, field2, field3, field4)
                .values(100, 20.d, 111, 1000L, "testit")
                .execute();

//        record1.attach(dsl.configuration());
//        int i = record1.store();
//        System.out.println("i = " + i);

//        record1.store();


        db.printTableAsText("Data");
    }
}
