package com.example.raghiii.inventoryapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.raghiii.inventoryapplication.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventoryapp.db";
    private static final int DATABASE_VERSION = 1;
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "(" +
                                            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                            InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                                            InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL, " +
                                            InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER DEFAULT 0, " +
                                            InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT + " TEXT NOT NULL, " +
                                            InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT);";
    db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
