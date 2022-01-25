package com.develop.grp101.orderoo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import androidx.annotation.NonNull;

public class OrderDatabaseAdapter extends DatabaseAdapter {

    /**
     * Order table "id field" name.
     */
    public static final String KEY_ROWID = "_id";

    /**
     * Client database field name.
     */
    public static final String KEY_CLIENT = "client";

    /**
     * Phone database field name.
     */
    public static final String KEY_PHONE = "phone";

    /**
     * Date database field name.
     */
    public static final String KEY_DATE = "date";

    /**
     *
     */
    public static final String KEY_QUANTITY = "quantity";

    /**
     * Orders database table name.
     */
    private static final String DATABASE_TABLE = "orders";

    /**
     * Products database table name.
     */
    private static final String PRODUCTS_TABLE = "products";

    /**
     * Orders primary key field in quantities table.
     */
    private static final String KEY_ORDER = "_id_order";

    /**
     * Quantities database table name.
     */
    private static final String QUANTITIES_TABLE = "quantities";

    /**
     * Constructor - takes the context to allow the database to be opened/
     * created.
     *
     * @param ctx the Context within which to work.
     */
    OrderDatabaseAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public void open() throws SQLException {
        super.open();
    }

    /**
     * Creates new Order database object for introducing it in the database.
     *
     * @param client new order's client.
     * @param phone new order's phone.
     * @param date new order's date.
     * @return order database object (ContentValues as order).
     */
    private ContentValues createOrder_obj(String client, String phone, String date) {
        ContentValues order_obj = new ContentValues();
        order_obj.put(KEY_CLIENT, client);
        order_obj.put(KEY_PHONE, phone);
        order_obj.put(KEY_DATE, date);
        return order_obj;
    }

    /**
     * Creates new Quantities database object for introducing it in the database.
     *
     * @param order associated quantity order id.
     * @param product associated product id.
     * @param quantity quantity of product.
     * @return quantity database object (ContentValues as quantity).
     */
    public ContentValues createQuantity_obj(long order, long product, int quantity) {
        ContentValues quantity_obj = new ContentValues();
        quantity_obj.put(KEY_ORDER, order);
        quantity_obj.put(ProductDatabaseAdapter.KEY_PRODUCT, product);
        quantity_obj.put(KEY_QUANTITY, quantity);
        return quantity_obj;
    }

    /**
     * Create a new order using the client, phone and date given. If order
     * successfully created return its id in the table, otherwise returns -1
     * for failure.
     *
     * @param client new order's client.
     * @param phone new product's phone.
     * @param date new product's date.
     * @return order id or -1 if failure.
     */
    public long createOrder(@NonNull String client,@NonNull String phone,@NonNull String date) {
        return mDb.insert(DATABASE_TABLE, null, createOrder_obj(client, phone, date));
    }

    /**
     * Create a new Quantity using the order id, product id and the product quantity
     * given. If product successfully created return its id in the table,
     * otherwise returns -1 for failure.
     *
     * @param order order
     * @param product product to introduce
     * @param quantity quantity
     * @return product id or -1 if failure.
     */
    public long createQuantity(long order, long product, int quantity) {
        return mDb.insert(QUANTITIES_TABLE, null,
                createQuantity_obj(order, product, quantity));
    }

    /**
     * Updates the order under given id using the parameters given.
     *
     * @param id order's id
     * @param client new order's client.
     * @param phone new product's phone.
     * @param date new product's date.
     * @return true if order was successfully updated, false otherwise.
     */
    public boolean updateOrder(long id, String client, String phone, String date) {
        return mDb.update(DATABASE_TABLE, createOrder_obj(client, phone, date),
                KEY_ROWID + "=" + id, null) > 0;
    }

    /**
     * Updates the Quantity under given order and product ids.
     *
     * @param order order
     * @param product product to introduce
     * @param quantity quantity
     * @return true if product was successfully updated, false otherwise.
     */
    public boolean updateQuantity(long order, long product, int quantity) {
        return mDb.update(QUANTITIES_TABLE, createQuantity_obj(order, product, quantity),
                KEY_ORDER + "=" + order + " AND " + ProductDatabaseAdapter.KEY_PRODUCT
                        + "=" + product, null) > 0;
    }

    /**
     * Delete order under given id.
     *
     * @param id order's id to delete.
     * @return true if deleted, false otherwise.
     */
    public boolean deleteOrder(long id) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + id, null) > 0;
    }

    /**
     * Delete Quantity under given order and product ids.
     *
     * @param order order id.
     * @param product product id.
     * @return true if deleted, false otherwise.
     */
    public boolean deleteQuantity(long order, long product) {
        return mDb.delete(QUANTITIES_TABLE, KEY_ORDER + "=" + order + " AND " +
                ProductDatabaseAdapter.KEY_PRODUCT + "=" + product, null) > 0;
    }

    /**
     * Delete all Quantities under given order id.
     *
     * @param order order id.
     * @return true if deleted, false otherwise.
     */
    public boolean deleteQuantities(long order) {
        return mDb.delete(QUANTITIES_TABLE, KEY_ORDER + "=" + order, null) > 0;
    }

    /**
     * Returns Cursor pointing order under given id if exists.
     *
     * @param id order's id to retrieve.
     * @return Cursor pointing founded order.
     * @throws SQLException if any query, SQL error, etc.
     */
    public Cursor fetchOrder(long id) throws SQLException {
        return fetchElement(DATABASE_TABLE, new String[] { KEY_CLIENT, KEY_PHONE, KEY_DATE },
                KEY_ROWID + "=" + id);
    }

    /**
     * Return a Cursor over all orders in the database under several parameters.
     *
     * @param orderBy query entries ordering specifications.
     * @return Cursor to resultant query.
     */
    public Cursor fetchAllOrders(String orderBy) throws SQLException {
        String sql =
                "SELECT " + DATABASE_TABLE + "." + KEY_ROWID + ", order_price, order_weight, "
                        + KEY_CLIENT + ", " + KEY_PHONE + ", " + KEY_DATE
                + " FROM " + DATABASE_TABLE + ", " +
                        "(SELECT " + KEY_ORDER + ", "
                            + "SUM(" + ProductDatabaseAdapter.KEY_PRICE + "*" + KEY_QUANTITY
                        + ") as order_price,"
                            + "SUM(" + ProductDatabaseAdapter.KEY_WEIGHT + "*" + KEY_QUANTITY
                        + ") as order_weight"
                        + " FROM " + PRODUCTS_TABLE + ", " + QUANTITIES_TABLE
                        + " WHERE " + ProductDatabaseAdapter.KEY_PRODUCT + "="
                        + ProductDatabaseAdapter.KEY_ROWID
                        + " GROUP BY " + KEY_ORDER + ") as totals"
                + " WHERE " + DATABASE_TABLE + "." + KEY_ROWID +  "= totals." + KEY_ORDER
                + " ORDER BY " + orderBy;
        return mDb.rawQuery(sql, null);
    }

    /**
     * Returns Cursor pointing product asociated to order in quantities table.
     *
     * @param order order id.
     * @param product product id.
     * @return Cursor to resultant query.
     */
    public Cursor fetchProduct(long order, long product) throws SQLException {
        return fetchElement(QUANTITIES_TABLE, new String[] { ProductDatabaseAdapter.KEY_PRODUCT,
            KEY_QUANTITY }, ProductDatabaseAdapter.KEY_PRODUCT + "=" + product
            + " AND " + KEY_ORDER + "=" + order);
    }


    /**
     * Return a Cursor over all quantities associated to the given order.
     *
     * @param order order's id.
     * @return Cursor to resultant query.
     */
    public Cursor fetchAllQuantities(long order){
        String sql =
                "SELECT " + ProductDatabaseAdapter.KEY_ROWID + ","
                        + ProductDatabaseAdapter.KEY_NAME + "," + KEY_QUANTITY + " "
                + "FROM " + PRODUCTS_TABLE + "," + QUANTITIES_TABLE + " "
                + "WHERE " + KEY_ORDER + "=" + order + " AND " + ProductDatabaseAdapter.KEY_ROWID
                        + "=" + ProductDatabaseAdapter.KEY_PRODUCT;
        return mDb.rawQuery(sql, null);
    }

}
