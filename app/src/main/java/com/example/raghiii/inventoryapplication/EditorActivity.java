package com.example.raghiii.inventoryapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.raghiii.inventoryapplication.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private static final int IMAGE_RESULT = 0;
    private static final int REQUEST_PERMISSIONS = 1;
    private Uri mCurrentItemUri;
    private boolean mItemHasChanged = false;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText contactEditText;
    private ImageView itemImageView;
    private Uri imageUri = null;
    private Button decrementButton;
    private Button incrementButton;
    private Button orderButton;
    private static int count = 0;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        orderButton = findViewById(R.id.order_button);
        orderButton.setVisibility(View.GONE);
        final Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        if (mCurrentItemUri == null) {
            setTitle(R.string.add_item);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_item);
            orderButton.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }
        nameEditText = (EditText) findViewById(R.id.item_name);
        priceEditText = (EditText) findViewById(R.id.item_price);
        quantityEditText = (EditText) findViewById(R.id.item_quantity);
        contactEditText = (EditText) findViewById(R.id.item_supplier);

        itemImageView = (ImageView) findViewById(R.id.item_image);
        itemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditorActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditorActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                    return;
                }
                imageSelection();
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_DIAL);
                String contact = "tel:";
                contact += contactEditText.getText().toString().trim();
                Uri uri = Uri.parse(contact);
                intent1.setData(uri);
                startActivity(intent1);
            }
        });
        quantityEditText = findViewById(R.id.item_quantity);
        decrementButton = findViewById(R.id.decrement_button);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = quantityEditText.getText().toString().trim();
                if (quantity.equals("") || quantity.equals(null)) {
                    quantityEditText.setText("0");
                    return;
                }
                int quantityValue = Integer.parseInt(quantity);
                if (quantityValue > 0) {
                    quantityValue -= 1;
                    quantity = Integer.toString(quantityValue);
                    quantityEditText.setText(quantity);
                }
            }
        });

        incrementButton = findViewById(R.id.increment_button);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = quantityEditText.getText().toString().trim();
                if (quantity.equals("") || quantity.equals(null)) {
                    quantityEditText.setText("1");
                    return;
                }
                int quantityValue = Integer.parseInt(quantity);
                quantityValue += 1;
                quantity = Integer.toString(quantityValue);
                quantityEditText.setText(quantity);
            }
        });

        nameEditText.setOnTouchListener(mOnTouchListener);
        priceEditText.setOnTouchListener(mOnTouchListener);
        quantityEditText.setOnTouchListener(mOnTouchListener);
        decrementButton.setOnTouchListener(mOnTouchListener);
        incrementButton.setOnTouchListener(mOnTouchListener);
        contactEditText.setOnTouchListener(mOnTouchListener);
    }

    private void imageSelection() {
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                imageSelection();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == IMAGE_RESULT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                imageUri = resultData.getData();
                itemImageView.setImageURI(imageUri);
                itemImageView.invalidate();
                mItemHasChanged = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveItem();
                if (count == 0)
                    finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete);
        builder.setPositiveButton(R.string.dialog_delete_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0)
                Toast.makeText(this, R.string.delete_item_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.delete_item_successful, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_edit);
        builder.setPositiveButton(R.string.dialog_edit_quit, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_edit_keep_writing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItem() {
        String name = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            count++;
            return;
        }
        String price = priceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            count++;
            return;
        }
        int priceValue = Integer.parseInt(price);
        String quantity = quantityEditText.getText().toString().trim();
        if (mCurrentItemUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(price)
                && TextUtils.isEmpty(quantity)) {
            return;
        }
        int quantityValue = 0;
        if (!TextUtils.isEmpty(quantity)) {
            quantityValue = Integer.parseInt(quantity);
        }
        String contact = contactEditText.getText().toString().trim();
        if (TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "Contact is required", Toast.LENGTH_SHORT).show();
            count++;
            return;
        }
        if (contact.length() != 10) {
            Toast.makeText(this, "Contact should be of 10 digit", Toast.LENGTH_SHORT).show();
            count++;
            return;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
            count++;
            return;
        }
        String image = imageUri.toString();
        count = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, name);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, priceValue);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantityValue);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT, contact);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE, image);
        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
            if (newUri == null)
                Toast.makeText(this, R.string.add_item_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.add_item_successful, Toast.LENGTH_SHORT).show();
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, contentValues, null, null);
            if (rowsAffected == 0)
                Toast.makeText(this, R.string.update_item_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.update_item_successful, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT,
                InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE};
        return new CursorLoader(this, mCurrentItemUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToNext()) {
            nameEditText.setText(data.getString(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)));
            priceEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE))));
            quantityEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY))));
            contactEditText.setText(data.getString(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_CONTACT)));
            String image = data.getString(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE));
            imageUri = Uri.parse(image);
            itemImageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        contactEditText.setText("");
    }
}
