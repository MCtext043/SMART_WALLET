package com.example.smartwallet.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    
    public static boolean insert(SQLiteDatabase db, String table, ContentValues values) {
        try {
            long result = db.insert(table, null, values);
            return result != -1;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error inserting into " + table, e);
            return false;
        }
    }
    
    public static boolean update(SQLiteDatabase db, String table, ContentValues values, 
                                String whereClause, String[] whereArgs) {
        try {
            int result = db.update(table, values, whereClause, whereArgs);
            return result > 0;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error updating " + table, e);
            return false;
        }
    }
    
    public static boolean delete(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
        try {
            int result = db.delete(table, whereClause, whereArgs);
            return result > 0;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error deleting from " + table, e);
            return false;
        }
    }
    
    public static Cursor query(SQLiteDatabase db, String table, String[] columns, 
                              String selection, String[] selectionArgs, String groupBy, 
                              String having, String orderBy) {
        try {
            return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error querying " + table, e);
            return null;
        }
    }
    
    public static boolean tableExists(SQLiteDatabase db, String tableName) {
        try {
            Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
            );
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error checking if table exists: " + tableName, e);
            return false;
        }
    }
    
    public static boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            boolean exists = false;
            
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext() && !exists) {
                    String name = cursor.getString(columnIndex);
                    exists = columnName.equals(name);
                }
                cursor.close();
            }
            
            return exists;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error checking if column exists: " + columnName, e);
            return false;
        }
    }
    
    public static List<String> getTableColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String name = cursor.getString(columnIndex);
                    columns.add(name);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error getting columns for table: " + tableName, e);
        }
        
        return columns;
    }
    
    public static boolean executeSql(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error executing SQL: " + sql, e);
            return false;
        }
    }
    
    public static boolean beginTransaction(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            return true;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error beginning transaction", e);
            return false;
        }
    }
    
    public static boolean setTransactionSuccessful(SQLiteDatabase db) {
        try {
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error setting transaction successful", e);
            return false;
        }
    }
    
    public static boolean endTransaction(SQLiteDatabase db) {
        try {
            db.endTransaction();
            return true;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error ending transaction", e);
            return false;
        }
    }
    
    public static boolean isDatabaseLocked(SQLiteDatabase db) {
        try {
            return db.isDatabaseIntegrityOk();
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error checking database lock", e);
            return false;
        }
    }
    
    public static long getDatabaseSize(SQLiteDatabase db) {
        try {
            Cursor cursor = db.rawQuery("SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()", null);
            long size = 0;
            if (cursor != null && cursor.moveToFirst()) {
                size = cursor.getLong(0);
                cursor.close();
            }
            return size;
        } catch (Exception e) {
            Logger.e("DatabaseUtils", "Error getting database size", e);
            return 0;
        }
    }
}
