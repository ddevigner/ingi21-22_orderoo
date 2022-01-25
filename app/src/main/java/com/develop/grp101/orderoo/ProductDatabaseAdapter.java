package com.develop.grp101.orderoo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ProductDatabaseAdapter extends DatabaseAdapter {

    /**
     * Product table "id field" name.
     */
    public static final String KEY_ROWID = "_id";

    /**
     * Title database field name.
     */
    public static final String KEY_NAME = "name";

    /**
     * Body database field name.
     */
    public static final String KEY_PRICE = "price";

    /**
     * Body database field name.
     */
    public static final String KEY_WEIGHT = "weight";

    /**
     * Body database field name.
     */
    public static final String KEY_DESCRIPTION = "description";

    /**
     * Title database field name.
     */
    public static final String KEY_PRODUCT = "_id_product";

    /**
     * Products database table name.
     */
    private static final String DATABASE_TABLE = "products";

    /**
     * Products database table name.
     */
    private static final String QUANTITIES_TABLE = "quantities";

    /**
     * Constructor - takes the context to allow the database to be opened/
     * created.
     *
     * @param ctx the Context within which to work.
     */
    ProductDatabaseAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public void open(){
        super.open();
    }

    /**
     * Creates new Product database object for introducing in the database.
     *
     * @param name new product's name.
     * @param price new product's price.
     * @param weight new product's weight.
     * @param description new product's description.
     * @return product database object (ContentValues as product).
     */
    private ContentValues createProduct_obj(String name, Double price,
                                           Double weight, String description) {
        ContentValues product_obj = new ContentValues();
        product_obj.put(KEY_NAME, name);
        product_obj.put(KEY_PRICE, price);
        product_obj.put(KEY_WEIGHT, weight);
        product_obj.put(KEY_DESCRIPTION, description);
        return product_obj;
    }

    /**
     * Create a new product using the name, price, weight and description
     * given. If product successfully created return its id in the table,
     * otherwise returns -1 for failure.
     *
     * @param name new product's name.
     * @param price new product's price.
     * @param weight new product's weight.
     * @param description new product's description.
     * @return product id or -1 if failure.
     */
    public long createProduct(@NonNull String name, @NonNull Double price,
                              @NonNull Double weight,@NonNull String description) {
        return mDb.insert(DATABASE_TABLE, null, createProduct_obj(name, price,
                weight, description));
    }

    /**
     * Updates the product under given id using the parameters given.
     *
     * @param id product's id
     * @param name new product's name.
     * @param price new product's price.
     * @param weight new product's weight.
     * @param description new product's description.
     * @return true if product was successfully updated, false otherwise.
     */
    public boolean updateProduct(long id, String name, Double price, Double weight,
                                 String description) {
        return mDb.update(DATABASE_TABLE, createProduct_obj(name, price,
                weight, description), KEY_ROWID + "=" + id, null) > 0;
    }

    /**
     * Delete product under given id.
     *
     * @param id product's id to delete.
     * @return true if deleted, false otherwise.
     */
    public boolean deleteProduct(long id) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + id, null) > 0;
    }

    /**
     * Returns Cursor pointing product under given id if exists.
     *
     * @param id product's id to retrieve.
     * @return Cursor pointing founded product.
     * @throws SQLException if any query, SQL error, etc.
     */
    public Cursor fetchProduct(long id) throws SQLException {
        return fetchElement(DATABASE_TABLE, new String[] { KEY_NAME, KEY_PRICE, KEY_WEIGHT,
                KEY_DESCRIPTION }, KEY_ROWID + "=" + id);
    }

    /**
     * Return a Cursor over all products in the database under several parameters.
     *
     * @param orderBy query entries ordering specifications.
     * @return Cursor to resultant query.
     */
    public Cursor fetchAllProducts(String orderBy, String where){
        return fetchAllElements(false, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME,
                KEY_PRICE, KEY_WEIGHT, KEY_DESCRIPTION}, where, null, null,
                orderBy, null);
    }

    /**
     * Return a Cursor over all products related to the given order.
     *
     * @param id order's id.
     * @return Cursor to resultant query.
     */
    public Cursor fetchAllOrders(long id){
        String sql =
                "SELECT " + OrderDatabaseAdapter.KEY_ROWID
                        + " FROM " + DATABASE_TABLE + ", " + QUANTITIES_TABLE
                        + " WHERE " + id + " = " + KEY_PRODUCT;
        return mDb.rawQuery(sql, null);
    }
}