package com.develop.grp101.orderoo;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter {
    /**
     * Database connection and communication handler.
     */
    private DatabaseHelper mDbHelper;

    /**
     * Database operations support.
     */
    protected SQLiteDatabase mDb;

    /**
     * Current context.
     */
    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be opened/
     * created.
     *
     * @param ctx the Context within which to work.
     */
    DatabaseAdapter(Context ctx) { this.mCtx = ctx;}

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure.
     *
     * @throws SQLException if the database could be neither opened or created.
     */
    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }

    /**
     * Closes the notes database.
     */
    public void close() {
        mDbHelper.close();
    }


    /**
     * Returns Cursor pointing element under given parameters.
     *
     * @param table table where get the element from.
     * @param columns columns to return.
     * @param where filter.
     * @return Cursor pointing founded element.
     */
    public Cursor fetchElement(String table, String[] columns, String where) throws SQLException{
        Cursor mCursor = mDb.query(true, table, columns, where, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Return a Cursor over all elements in the database under several
     * parameters.
     *
     * @param distinct distinct query entries or not.
     * @param table table where to get elements from.
     * @param columns columns to get.
     * @param where query entries filtering specifications.
     * @param groupBy query entries grouping specifications.
     * @param having query entries special filtering specifications.
     * @param orderBy query entries ordering specifications.
     * @param limit limit query result set entries number.
     * @return Cursor to resultant query.
     */
    public Cursor fetchAllElements(boolean distinct, String table, String[] columns, String where,
                                   String groupBy, String having, String orderBy, String limit) {
        return mDb.query(distinct, table, columns, where,null, groupBy, having, orderBy,
                limit);
    }
}
