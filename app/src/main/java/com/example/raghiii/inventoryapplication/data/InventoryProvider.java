package com.example.raghiii.inventoryapplication.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class InventoryProvider extends ContentProvider {
    private static final String LOG_TAG = InventoryProvider.class.getName();
    private InventoryDbHelper mDbHelper;
    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new InventoryDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = uriMatcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case ITEMS:
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, null);
                break;
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null)
            throw new IllegalArgumentException("Item requires a name");
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        if (price == null)
            throw new IllegalArgumentException("Item requires a price");
        if (price != null && price < 0)
            throw new IllegalArgumentException("Item requires a valid price");
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity != null && quantity < 0)
            throw new IllegalArgumentException("Item requires a valid quantity");
        String contact = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT);
        if (contact.length() != 10 || contact.equals(null))
            throw new IllegalArgumentException("Item requires a valid contact no of supplier");
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case ITEMS:
                rowsUpdated = updateItem(uri, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = updateItem(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null)
                throw new IllegalArgumentException("Item requires a name");
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if (price == null)
                throw new IllegalArgumentException("Item requires a price");
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0)
                throw new IllegalArgumentException("Item requires a valid quantity");
        }
        if (values.size() == 0)
            return 0;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
