package com.example.pizza_mania.cart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CartDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pizza_cart.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "cart_items";
    private static final String COL_ID = "id";
    private static final String COL_ITEM_NAME = "itemName";
    private static final String COL_ITEM_IMAGE = "itemImage";
    private static final String COL_SIZE = "size";
    private static final String COL_PRICE = "price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_BRANCH = "branchName";

    public CartDbHelper (Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " ( " +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
                COL_ITEM_NAME + " TEXT, " +
                COL_ITEM_IMAGE + " TEXT, " +
                COL_SIZE + " TEXT, " +
                COL_PRICE + " INTEGER, " +
                COL_QUANTITY + " INTEGER, " +
                COL_BRANCH + " TEXT" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // add items. if items is exist, increase the quantity
    public void addOrUpdateItem(String itemName, String itemImage, String size, int price, String branch) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ITEM_NAME + "=? AND " + COL_SIZE + "=? AND " + COL_BRANCH + "=?";
        String[] args = new String[] {itemName, size, branch == null ? "" : branch};
        Cursor cursor = db.query(TABLE, new String[] {COL_ID, COL_QUANTITY}, selection, args, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            ContentValues cv = new ContentValues();
            cv.put(COL_QUANTITY, qty + 1);
            db.update(TABLE, cv, COL_ID  + "=?", new String[] {String.valueOf(id)});
            cursor.close();
        } else {
            ContentValues cv = new ContentValues();
            cv.put(COL_ITEM_NAME, itemName);
            cv.put(COL_ITEM_IMAGE, itemImage);
            cv.put(COL_SIZE, size);
            cv.put(COL_PRICE, price);
            cv.put(COL_QUANTITY, 1);
            cv.put(COL_BRANCH, branch == null ? "" : branch);
            db.insert(TABLE, null, cv);
            if (cursor != null) cursor.close();
        }
        db.close();
    }

    public List<CartItem> getAllItems() {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CartItem ci = new CartItem();
                ci.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)));
                ci.setItemImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_IMAGE)));
                ci.setSize(cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE)));
                ci.setPrice(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRICE)));
                ci.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)));
                ci.setBranchName(cursor.getString(cursor.getColumnIndexOrThrow(COL_BRANCH)));
                list.add(ci);
            }cursor.close();
        }
        db.close();
        return list;
    }

    public void updateQuantity(String itemName, String size, String branch, int newQty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUANTITY, newQty);
        String where = COL_ITEM_NAME + "=? AND " + COL_SIZE + "=? AND " + COL_BRANCH + "=?";
        String[] args = new String[] {itemName, size, branch == null ? "" : branch};
        db.update(TABLE, cv, where, args);
        db.close();
    }

    public void removeItem(String itemName, String size, String branchName) {
        SQLiteDatabase db = getWritableDatabase();
        String where = COL_ITEM_NAME + "=? AND " + COL_SIZE + "=? AND " + COL_BRANCH + "=?";
        String[] args = new String[] {itemName, size, branchName == null ? "" : branchName};
        db.delete(TABLE, where, args);
        db.close();
    }

    public void clearCart() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

}
