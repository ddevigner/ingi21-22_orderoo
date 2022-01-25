package com.develop.grp101.orderoo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class QuantityEdit extends AppCompatActivity {

    /**
     * Current database row id.
     */
    private Long mId;

    /**
     * DatabaseAdapter for database products interaction.
     */
    private DatabaseAdapter mDbHelper;

    /**
     * DatabaseAdapter for database products interaction.
     */
    private String selected_ids;

    /**
     * ListView for product not added yet.
     */
    private ListView mProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quantity_edit);

        mId = (savedInstanceState != null) ? (Long) savedInstanceState.
                getSerializable(OrderDatabaseAdapter.KEY_ROWID) : null;
        if (mId == null){
            Bundle extras = getIntent().getExtras();
            mId = (extras != null) ?
                    extras.getLong(OrderDatabaseAdapter.KEY_ROWID) : null;
        }
        mDbHelper = new ProductDatabaseAdapter(this);
        selected_ids = (savedInstanceState != null) ? (String) savedInstanceState.
                getSerializable("selected_products") : null;
        if (selected_ids == null){
            Bundle extras = getIntent().getExtras();
            selected_ids = (extras != null) ? extras.getString("selected_products") : null;
        }
        mDbHelper.open();

        mProducts = findViewById(R.id.quantities_list);
        mProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent();
                result.putExtra("product_id", Long.parseLong(((TextView) view.findViewById(
                        R.id.pq_selection_id)).getText().toString()));
                result.putExtra("name", ((TextView) view.findViewById(
                        R.id.pq_selection_name)).getText());
                result.putExtra("quantity", 1);
                setResult(QuantityEdit.RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(OrderDatabaseAdapter.KEY_ROWID, mId);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        fillAvailProducts();
        if (mProducts.getCount() == 0) {
            findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.empty_message).setVisibility(View.GONE);
        }
    }

    /**
     * Populate screen elements with those products that are not added yet to the current order.
     */
    private void fillAvailProducts(){
        String[] from = new String[] { ProductDatabaseAdapter.KEY_ROWID,
                ProductDatabaseAdapter.KEY_NAME };
        int[] to = new int[] { R.id.pq_selection_id, R.id.pq_selection_name };

        System.out.println(ProductDatabaseAdapter.KEY_ROWID + " NOT IN " + selected_ids);
        Cursor quantities = ((ProductDatabaseAdapter) mDbHelper).fetchAllProducts(null,
                    ProductDatabaseAdapter.KEY_ROWID + " NOT IN " + selected_ids);
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this,
                R.layout.product_row_in_selection, quantities, from, to, 0);
        mProducts.setAdapter(mAdapter);
    }
}
