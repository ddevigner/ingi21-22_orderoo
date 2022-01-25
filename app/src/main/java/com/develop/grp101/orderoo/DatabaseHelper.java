package com.develop.grp101.orderoo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Enables database conection and communication.
 */
class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * Database name.
     */
    private static final String DATABASE_NAME = "data";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 6;

    /**
     * Database tag.
     */
    private static final String TAG = "DatabaseHelper";

    /**
     * Database Products Table SQL Creation Statement.
     */
    private static final String CREATE_PRODUCTS =
            "create table products ("
                    + "_id integer primary key autoincrement, "
                    + "name text not null, "
                    + "price double not null, "
                    + "weight double not null, "
                    + "description text not null"
                    + ");";

    /**
     * Database Orders Table SQL Creation Statement.
     */
    private static final String CREATE_ORDERS =
            "create table orders ("
                    + "_id integer primary key autoincrement, "
                    + "client text not null, "
                    + "phone text not null, "
                    + "date text not null "
                    + ");";

    /**
     * Database Orders-Products Relationship Table SQL Creation Statement.
     */
    private static final String CREATE_RELATIONSHIP =
            "create table quantities ("
                    + "_id_order integer not null, "
                    + "_id_product integer not null, "
                    + "quantity integer not null, "
                    + "Primary key(_id_order, _id_product), "
                    + "foreign key(_id_order) references orders(_id), "
                    + "foreign key(_id_product) references products(_id)"
                    + ");";

    /**
     * Database constructor.
     *
     * @param cxt context of creation.
     */
    DatabaseHelper(Context cxt) {
        super(cxt, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCTS);
        db.execSQL(CREATE_ORDERS);
        db.execSQL(CREATE_RELATIONSHIP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version,
                          int new_version) {
        Log.w(TAG, "Upgrading database from version " + old_version + " to "
                + new_version + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_products");
        onCreate(db);
    }
}