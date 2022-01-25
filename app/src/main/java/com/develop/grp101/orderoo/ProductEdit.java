package com.develop.grp101.orderoo;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;


/**
 * Implementation of note creation or edition and the correspondent display.
 */
public class ProductEdit extends AppCompatActivity {

    /**
     * EditText element associated to product name.
     */
    private EditText mNameText;

    /**
     * EditText element associated to product price.
     */
    private EditText mPriceText;

    /**
     * EditText element associated to product weight.
     */
    private EditText mWeightText;

    /**
     * EditText element associated to product description.
     */
    private EditText mDescriptionText;

    /**
     * Current database row id.
     */
    private Long mId;

    /**
     * ProductDatabaseAdapter for database interaction.
     */
    private ProductDatabaseAdapter mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ProductDatabaseAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.activity_product_edit);
        mNameText        = findViewById(R.id.product_name);
        mPriceText       = findViewById(R.id.product_price);
        mWeightText      = findViewById(R.id.product_weight);
        mDescriptionText = findViewById(R.id.product_description);

        mId = (savedInstanceState != null) ? (Long) savedInstanceState.
                getSerializable(ProductDatabaseAdapter.KEY_ROWID) : null;
        if (mId == null){
            Bundle extras = getIntent().getExtras();
            mId = (extras != null) ?
                    extras.getLong(ProductDatabaseAdapter.KEY_ROWID) : null;
        }

        FloatingActionButton confirm_button = findViewById(R.id.product_edit_fab);
        confirm_button.setOnClickListener(view -> {
            if (!(mNameText.getText().toString().equals("")  ||
                    (mPriceText.getText().toString().equals("") ||
                            Double.parseDouble(mPriceText.getText().toString()) == 0.0) ||
                    (mWeightText.getText().toString().equals("") ||
                            Double.parseDouble(mWeightText.getText().toString()) == 0.0) ||
                    mDescriptionText.getText().toString().equals(""))) {
                setResult(RESULT_OK);
                saveState();
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        //saveState();
        outState.putSerializable(ProductDatabaseAdapter.KEY_ROWID, mId);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        populateFields();
    }

    /**
     * Populate screen elements with the database existing products.
     */
    private void populateFields(){
        if(mId != null){
            Cursor note = mDbHelper.fetchProduct(mId);
            startManagingCursor(note);
            mNameText.setText(note.getString(note.getColumnIndexOrThrow(
                    ProductDatabaseAdapter.KEY_NAME)));
            mPriceText.setText(note.getString(note.getColumnIndexOrThrow(
                    ProductDatabaseAdapter.KEY_PRICE)));
            mWeightText.setText(note.getString(note.getColumnIndexOrThrow(
                    ProductDatabaseAdapter.KEY_WEIGHT)));
            mDescriptionText.setText(note.getString(note.getColumnIndexOrThrow(
                    ProductDatabaseAdapter.KEY_DESCRIPTION)));
        }
    }


    /**
     * Saves activity current state and finishes it, creating a new product
     * or editing existing one.
     */
    private void saveState(){
        String name   = mNameText.getText().toString();
        Double price  = (!mPriceText.getText().toString().equals("")) ?
                Double.parseDouble(mPriceText.getText().toString()) : 0.0;
        Double weight = (!mWeightText.getText().toString().equals("")) ?
                Double.parseDouble(mWeightText.getText().toString()) : 0.0;
        String description = mDescriptionText.getText().toString();

        if(mId == null){
            long id = mDbHelper.createProduct(name, price, weight, description);
            if (id > 0) mId = id;
        }
        else mDbHelper.updateProduct(mId, name, price, weight, description);
    }
}
