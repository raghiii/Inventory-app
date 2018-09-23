package com.example.raghiii.inventoryapplication;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.raghiii.inventoryapplication.data.InventoryContract;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        mCursorAdapter = new InventoryCursorAdapter(this, null) {
            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                super.bindView(view, context, cursor);
                ImageView button = view.findViewById(R.id.sale_image);
                button.setTag(cursor.getPosition());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sellItem(cursor, (int) view.getTag());
                    }
                });
            }
        };
        ListView itemListView = (ListView) findViewById(R.id.list);
        itemListView.setAdapter(mCursorAdapter);
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    private void sellItem(Cursor cursor, int position) {
        cursor.moveToPosition(position);
        ContentValues values = new ContentValues();
        int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));
        if (quantity < 1) {
            Toast.makeText(this, "Not enough Items", Toast.LENGTH_SHORT).show();
            return;
        }
        quantity--;
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
        Uri stockUrl = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
        int rowsAffected = getContentResolver().update(stockUrl, values, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_new:
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                deleteItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.e("Catalog Activity", rowsDeleted + " rows deleted from database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT,
                InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE};
        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
