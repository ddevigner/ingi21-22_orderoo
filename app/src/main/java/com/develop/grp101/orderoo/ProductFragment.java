package com.develop.grp101.orderoo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Objects;

public class ProductFragment extends Fragment {

    /**
     * Product creation activity id.
     */
    private static final int ACTIVITY_CREATE=0;

    /**
     * Product edition activity id.
     */
    private static final int ACTIVITY_EDIT=1;

    /**
     * Product Delete option id.
     */
    private static final int DELETE_ID = Menu.FIRST + 3;

    /**
     * Product Edit option id.
     */
    private static final int EDIT_ID = Menu.FIRST + 4;

    private int SORTING = 0;
    private View mView;
    private ProductDatabaseAdapter mDbHelper;
    private ListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_product, container, false);

        mDbHelper = new ProductDatabaseAdapter(getActivity());
        mDbHelper.open();
        mList = mView.findViewById(R.id.products_list);
        fillProducts();
        registerForContextMenu(mList);
        return mView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.product_menu_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.product_menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case DELETE_ID:
                Cursor are_orders = mDbHelper.fetchAllOrders(info.id);
                if (mDbHelper.fetchAllOrders(info.id).getCount() == 0) {
                    mDbHelper.deleteProduct(info.id);
                    fillProducts();
                    Snackbar.make(mView, "Product deleted", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                return true;
            case EDIT_ID:
                editProduct(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) { onResume(); }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) { return; }

        ((MainActivity) requireActivity()).mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProduct();
            }
        });
        ((MainActivity) requireActivity()).mSortB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bSd = new BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme);
                View bSv = LayoutInflater.from(((MainActivity)getActivity()).getApplicationContext()).inflate(
                        R.layout.dialog_product_sorting,
                        getActivity().findViewById(R.id.product_sorting_container));
                bSv.findViewById(R.id.product_sort_default).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 0;
                        fillProducts();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.product_sort_name).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 1;
                        fillProducts();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.product_sort_price).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 2;
                        fillProducts();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.product_sort_weight).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 3;
                        fillProducts();
                        bSd.dismiss();
                    }
                });
                bSd.setContentView(bSv);
                bSd.show();
            }
        });
        if (mList.getCount() == 0) {
            requireActivity().findViewById(R.id.empty_message_products).setVisibility(View.VISIBLE);
        } else {
            requireActivity().findViewById(R.id.empty_message_products).setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillProducts();
    }

    public static ProductFragment newInstance(String text) {
        ProductFragment pf = new ProductFragment();
        Bundle b = new Bundle();
        pf.setArguments(b);
        return pf;
    }

    /**
     * Inits product creation.
     */
    private void createProduct() {
        Intent i = new Intent(getActivity(), ProductEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Inits product edition.
     *
     * @param id note id.
     */
    protected void editProduct(long id) {
        Intent i = new Intent(getActivity(), ProductEdit.class);
        i.putExtra(ProductDatabaseAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /**
     * Fills products list.
     */
    private void fillProducts(){
        String orderBy = null;
        switch(SORTING){
            case 1:
                orderBy = ProductDatabaseAdapter.KEY_NAME + " COLLATE NOCASE";
                break;
            case 2:
                orderBy = ProductDatabaseAdapter.KEY_PRICE;
                break;
            case 3:
                orderBy = ProductDatabaseAdapter.KEY_WEIGHT;
                break;
        }
        String[] from = new String[] { ProductDatabaseAdapter.KEY_NAME,
                ProductDatabaseAdapter.KEY_PRICE, ProductDatabaseAdapter.KEY_WEIGHT};
        int[] to = new int[] { R.id.product_row_text, R.id.product_row_price,
                R.id.product_row_weight };

        Cursor products = mDbHelper.fetchAllProducts(orderBy, null);
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.element_product_row, products, from, to, 0);
        mList.setAdapter(mAdapter);
    }
}
