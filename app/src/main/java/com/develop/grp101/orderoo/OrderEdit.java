package com.develop.grp101.orderoo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Implementation of note creation or edition and the correspondent display.
 */
public class OrderEdit extends AppCompatActivity {

    /**
     * Order products quantities edition context menu option.
     */
    private static final int EDIT_QUANTITY_MENU = Menu.FIRST;

    /**
     * Order products quantities removal context menu option.
     */
    private static final int DELETE_QUANTITY_MENU = Menu.FIRST + 1;

    /**
     * Order products quantities edition activity id.
     */
    private static final int ACTIVITY_EDIT_QUANTITIES = 0;

    /**
     * EditText for order client name.
     */
    private EditText mClientText;

    /**
     * EditText for order client phone.
     */
    private EditText mPhoneText;

    /**
     * EditText for order date.
     */
    private EditText mDateText;

    /**
     * ListView for order products.
     */
    private ListView mProducts;

    /**
     * Temporal storage for order products edition.
     * Key: Product_Id, Value: [Product_Name, Product_Quantity]
     */
    private HashMap<Long, ArrayList<String>> mState;

    /**
     * Current order id.
     */
    private Long mId;

    /**
     * OrderDatabaseAdapter for database interaction.
     */
    private OrderDatabaseAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_edit);

        // Inner system variables init.
        mDbHelper = new OrderDatabaseAdapter(this);
        mDbHelper.open();
        mId = (savedInstanceState != null) ? (Long) savedInstanceState.
                getSerializable(OrderDatabaseAdapter.KEY_ROWID) : null;
        if (mId == null){
            Bundle extras = getIntent().getExtras();
            mId = (extras != null) ?
                    extras.getLong(OrderDatabaseAdapter.KEY_ROWID) : null;
        }
        mState = new HashMap<>();

        // View fields.
        mClientText = findViewById(R.id.order_client);
        mPhoneText = findViewById(R.id.order_phone);
        mDateText = findViewById(R.id.order_date);
        mDateText.setEnabled(false);
        mProducts = findViewById(R.id.quantities_list);
        registerForContextMenu(mProducts);

        // "Select date" Button.
        findViewById(R.id.order_date_fab).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DatePickerDialog dPd = new DatePickerDialog(OrderEdit.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay){
                                String dateSet = mDay + "/" + (mMonth + 1) + "/" + mYear;
                                mDateText.setText(dateSet);
                            }
                        }, Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                dPd.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                dPd.show();
            }
        });

        // "Add new products" Button.
        findViewById(R.id.quantity_edit_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { editQuantities(); }
        });

        // "Save order" Button.
        findViewById(R.id.order_edit_fab).setOnClickListener(view -> {
            if (!(mClientText.getText().toString().equals("")  ||
                    mPhoneText.getText().toString().equals("") ||
                    mDateText.getText().toString().equals("")  ||
                    mProducts.getCount() == 0)) {
                setResult(RESULT_OK);
                saveState();
                finish();
            }
        });
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, EDIT_QUANTITY_MENU, Menu.NONE, R.string.order_quantity_edit);
        menu.add(Menu.NONE, DELETE_QUANTITY_MENU, Menu.NONE, R.string.order_quantity_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long product_id = Long.parseLong(((TextView) info.targetView.findViewById(
                R.id.product_quantity_id)).getText().toString());
        switch(item.getItemId()) {
            case EDIT_QUANTITY_MENU:
                View view = getLayoutInflater().inflate( R.layout.dialog_product_quantity, null);
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setView(view);
                final EditText input = view.findViewById(R.id.quantity_edit_text);
                if (mId == null) {
                    input.setText(mState.get(product_id).get(1));
                } else {
                    Cursor product = mDbHelper.fetchProduct(mId, product_id);
                    input.setText(product.getString(product.getColumnIndexOrThrow(
                            OrderDatabaseAdapter.KEY_QUANTITY)));
                }

                view.findViewById(R.id.quantity_edit_confirm).setOnClickListener(
                        new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int quantity = Integer.parseInt(input.getText().toString());
                        if (quantity == 0) {
                            mState.remove(product_id);
                        } else {
                            mState.get(product_id).set(1,Integer.toString(quantity));
                        }
                        mProducts.setAdapter(new QuantityAdapter(mState));
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            case DELETE_QUANTITY_MENU:
                mState.remove(product_id);
                mProducts.setAdapter(new QuantityAdapter(mState));
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        //saveState();
        outState.putSerializable(OrderDatabaseAdapter.KEY_ROWID, mId);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode ==  ACTIVITY_EDIT_QUANTITIES) {
            if (resultCode == QuantityEdit.RESULT_OK) {
                long product_id = intent.getLongExtra("product_id", -1);
                String name = intent.getStringExtra("name");
                int quantity = intent.getIntExtra("quantity", 0);
                if (product_id != -1 && quantity != 0) {
                    ArrayList<String> p = new ArrayList<String>();
                    p.add(name);
                    p.add(Integer.toString(quantity));
                    mState.put(product_id, p);
                    populateFields();
                }
            }
        }
    }

    /**
     * Populate screen elements with the database existing orders.
     */
    private void populateFields(){
        if(mId != null) {
            Cursor o = mDbHelper.fetchOrder(mId);
            startManagingCursor(o);
            mClientText.setText(o.getString(o.getColumnIndexOrThrow(OrderDatabaseAdapter.
                    KEY_CLIENT)));
            mPhoneText.setText(o.getString(o.getColumnIndexOrThrow(OrderDatabaseAdapter.
                    KEY_PHONE)));
            mDateText.setText(o.getString(o.getColumnIndexOrThrow(OrderDatabaseAdapter.
                    KEY_DATE)));

            Cursor q = mDbHelper.fetchAllQuantities(mId);
            while (q.moveToNext()) {
                ArrayList<String> p = new ArrayList<>();
                p.add(q.getString(q.getColumnIndexOrThrow(ProductDatabaseAdapter.KEY_NAME)));
                p.add(q.getString(q.getColumnIndexOrThrow(OrderDatabaseAdapter.KEY_QUANTITY)));
                mState.put(Long.valueOf(q.getString(q.getColumnIndexOrThrow(
                        ProductDatabaseAdapter.KEY_ROWID))), p);
            }
        }
        mProducts.setAdapter(new QuantityAdapter(mState));
    }

    /**
     * Saves activity current state and finishes it, creating a new order or editing existing one.
     */
    private void saveState(){
        String client = mClientText.getText().toString();
        String phone = mPhoneText.getText().toString();
        String date = mDateText.getText().toString();

        if(mId == null){
            long id = mDbHelper.createOrder(client, phone, date);
            if (id > 0) mId = id;
        } else {
            mDbHelper.updateOrder(mId, client, phone, date);
            mDbHelper.deleteQuantities(mId);
        }
        for (HashMap.Entry<Long, ArrayList<String>> entry : mState.entrySet()) {
            mDbHelper.createQuantity(mId, entry.getKey(), Integer.parseInt(
                    entry.getValue().get(1)));
        }
    }

    /**
     * Inits order products quantities edition.
     */
    private void editQuantities() {
        Intent i = new Intent(this, QuantityEdit.class);
        i.putExtra(OrderDatabaseAdapter.KEY_ROWID, mId);
        StringBuilder sb = new StringBuilder("(");
        if (!mState.isEmpty()){
            for (Long id : mState.keySet()){
                sb.append("'");
                sb.append(String.format(Locale.US, "%d", id)).append("',");
            }
            sb = new StringBuilder(sb.substring(0, sb.length() - 1));
        }
        sb.append(")");
        i.putExtra("selected_products", sb.toString());
        startActivityForResult(i, ACTIVITY_EDIT_QUANTITIES);
    }

    /**
     * Custom view adapter for inflating from a HashMap to a custom layout.
     */
    public static class QuantityAdapter extends BaseAdapter {
        private final ArrayList<HashMap.Entry<Long,ArrayList<String>>> mData;

        public QuantityAdapter(HashMap<Long,ArrayList<String>> map) {
            mData = new ArrayList<>();
            if (!map.isEmpty()){
                mData.addAll(map.entrySet());
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public HashMap.Entry<Long,ArrayList<String>> getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View result;

            if (convertView == null) {
                result = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.quantity_row_in_order, parent, false);
            } else {
                result = convertView;
            }

            HashMap.Entry<Long,ArrayList<String>> item = getItem(position);
            ((TextView) result.findViewById(R.id.product_quantity_id)).setText(String.format(
                    Locale.US, "%d", item.getKey()));
            ((TextView) result.findViewById(R.id.product_quantity_name)).setText(
                    item.getValue().get(0));
            ((TextView) result.findViewById(R.id.product_quantity)).setText(
                    item.getValue().get(1));
            return result;
        }
    }
}

