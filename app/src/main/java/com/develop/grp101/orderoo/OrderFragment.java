package com.develop.grp101.orderoo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import java.net.URLEncoder;
import java.util.Objects;

public class OrderFragment extends Fragment {
    /**
     * Order creation activity id.
     */
    private static final int ACTIVITY_CREATE=0;

    /**
     * Order edition activity id.
     */
    private static final int ACTIVITY_EDIT=1;

    /**
     * Delete option id.
     */
    private static final int DELETE_ID = Menu.FIRST;

    /**
     * Edit option id.
     */
    private static final int EDIT_ID = Menu.FIRST + 1;

    /**
     * Share option id.
     */
    private static final int SHARE_ID = Menu.FIRST + 2;

    /**
     * Saves current sort mode.
     */
    private int SORTING = 0;

    /**
     * View for inflating the order fragment into.
     */
    private View mView;

    /**
     * OrderDatabaseAdapter for accessing it's table.
     */
    private OrderDatabaseAdapter mDbHelper;

    /**
     * ListView for existing orders.
     */
    private ListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_order, container, false);
        mDbHelper = new OrderDatabaseAdapter(getActivity());
        mDbHelper.open();
        mList = mView.findViewById(R.id.orders_list);

        registerForContextMenu(mList);
        return mView;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.order_menu_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.order_menu_edit);
        menu.add(Menu.NONE, SHARE_ID, Menu.NONE, R.string.order_menu_share);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteQuantities(info.id);
                mDbHelper.deleteOrder(info.id);
                fillOrders();
                Snackbar.make(mView, "Order deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            case EDIT_ID:
                editOrder(info.id);
                return true;
            case SHARE_ID:
                sendOrder(info.id);
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
            public void onClick(View view) { createOrder(); }
        });
        ((MainActivity) requireActivity()).mSortB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bSd = new BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme);

                View bSv = LayoutInflater.from(getActivity().getApplicationContext()).inflate(
                        R.layout.dialog_order_sorting,
                        getActivity().findViewById(R.id.order_sorting_container));
                bSv.findViewById(R.id.order_sort_default).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 0;
                        fillOrders();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.order_sort_client).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 1;
                        fillOrders();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.order_sort_phone).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 2;
                        fillOrders();
                        bSd.dismiss();
                    }
                });
                bSv.findViewById(R.id.order_sort_date).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SORTING = 3;
                        fillOrders();
                        bSd.dismiss();
                    }
                });
                bSd.setContentView(bSv);
                bSd.show();
            }
        });
        fillOrders();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillOrders();
    }

    /**
     * Returns new instance of an OrderFragment.
     */
    public static OrderFragment newInstance(String text) {
        OrderFragment of = new OrderFragment();
        Bundle b = new Bundle();
        of.setArguments(b);
        return of;
    }

    /**
     * Inits order creation.
     */
    private void createOrder() {
        Intent i = new Intent(getActivity(), OrderEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Inits order edition.
     *
     * @param id order's id.
     */
    protected void editOrder(long id) {
        Intent i = new Intent(getActivity(), OrderEdit.class);
        i.putExtra(OrderDatabaseAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /**
     * Prepares and shares an order.
     *
     * @param id order's id.
     */
    private void sendOrder(long id){
        PackageManager packageManager = this.getContext().getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);

        try {
            Cursor order = mDbHelper.fetchOrder(id);
            String client = order.getString(order.getColumnIndexOrThrow(
                    OrderDatabaseAdapter.KEY_CLIENT));
            String phone = order.getString(order.getColumnIndexOrThrow(
                    OrderDatabaseAdapter.KEY_PHONE));
            String date = order.getString(order.getColumnIndexOrThrow(
                    OrderDatabaseAdapter.KEY_DATE));
            String message = parseOrder(id, client, date);
            String url = "https://api.whatsapp.com/send?phone="+
                    phone +"&text=" + URLEncoder.encode(message, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                this.startActivity(i);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Parse order information for being shared.
     *
     * @param id order's id.
     * @param client client name.
     * @param date order limit date.
     */
    private String parseOrder(long id, String client, String date){
        String message =
                "Hello, welcome back Mr/Ms " + client + ".\n"
                + "This message is in purpose of reminder that you have an order for "
                    + "the next " + date + " and its already available to be picked up.\n"
                + "This is what you have ordered:\n";
        Cursor cq = mDbHelper.fetchAllQuantities(id);
        for (cq.moveToFirst(); !cq.isAfterLast(); cq.moveToNext()){
            message += cq.getString(cq.getColumnIndexOrThrow(
                    ProductDatabaseAdapter.KEY_NAME));
            message += " " + cq.getInt(cq.getColumnIndexOrThrow(
                    OrderDatabaseAdapter.KEY_QUANTITY)) + "unit(s).\n";
        }
        return message + "That's all, thank you Mr/Ms, and we look forward to having new deals " +
                "with you. We will wait for you impatiently.\n";
    }

    /**
     * Fills orders list.
     */
    private void fillOrders(){
        String orderBy = null;
        switch(SORTING){
            case 1:
                orderBy = OrderDatabaseAdapter.KEY_CLIENT + " COLLATE NOCASE";
                break;
            case 2:
                orderBy = OrderDatabaseAdapter.KEY_PHONE;
                break;
            case 3:
                orderBy = OrderDatabaseAdapter.KEY_DATE;
                break;
        }
        String[] from = new String[] { OrderDatabaseAdapter.KEY_CLIENT, "order_price", "order_weight" };
        int[] to = new int[] { R.id.order_row_text, R.id.order_row_price, R.id.order_row_weight};

        Cursor orders = mDbHelper.fetchAllOrders(orderBy);
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(requireActivity(),
                R.layout.element_order_row, orders, from, to, 0);
        mList.setAdapter(mAdapter);
        if (mList.getCount() == 0) {
            requireActivity().findViewById(R.id.empty_message_orders).setVisibility(View.VISIBLE);
        } else {
            requireActivity().findViewById(R.id.empty_message_orders).setVisibility(View.GONE);
        }
    }

}