package com.example.raghiii.inventoryapplication;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.raghiii.inventoryapplication.data.InventoryContract;

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        ImageView imageView = view.findViewById(R.id.item_image);
        int price = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE));
        String priceValue = Integer.toString(price);
        nameTextView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)));
        priceTextView.setText(priceValue);
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));
        String quantityValue = Integer.toString(quantity);
        quantityTextView.setText("Quantity: " + quantityValue);
    }
}
