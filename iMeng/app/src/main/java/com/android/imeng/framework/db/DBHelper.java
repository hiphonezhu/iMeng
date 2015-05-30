package com.android.imeng.framework.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.imeng.AppDroid;
import com.android.imeng.framework.log.Logger;
import com.android.imeng.util.SPDBHelper;

/**
 * 数据库轻量级操作封装
 * 
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2013-3-18]
 */
public class DBHelper
{
    private DataBaseHelper dbHelper;
    private SQLiteDatabase writableDB;
    private SQLiteDatabase readableDB;
    /** 数据库名称 */
    private static final String DATABASE_NAME = "project.db";
    /** 数据库版本 */
    private static final int DATABASE_VERSION = 1;

    public DBHelper()
    {
        dbHelper = new DataBaseHelper(AppDroid.getInstance().getApplicationContext());
    }

    /**
     * 获取数据库操作对象
     * 
     * @return SQLiteDatabase
     */
    public synchronized SQLiteDatabase getWritableSQLiteDatabase()
    {
        writableDB = dbHelper.getWritableDatabase();
        return writableDB;
    }
    
    /**
     * 获取数据库操作对象
     * 
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getReadableSQLiteDatabase()
    {
        readableDB = dbHelper.getReadableDatabase();
        return readableDB;
    }

    /**
     * 关闭数据库
     */
    public void close()
    {
        writableDB = null;
        readableDB = null;
        if (dbHelper != null)
        {
            dbHelper.close();
        }
    }

    public class DataBaseHelper extends SQLiteOpenHelper
    {
        private static final String TAG = "DataBaseHelper";

        public DataBaseHelper(Context context)
        {
            super(context, DATABASE_NAME,
                    null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.beginTransaction();
            try
            {
                db.execSQL(SPDBHelper.TABLE_CREATE_SQL);
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Logger.e(TAG,
                        e);
            }
            finally
            {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
        }
    }
}
