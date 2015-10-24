package cn.liao.myweather.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyWeatherOpenHelper extends SQLiteOpenHelper {
	
	public MyWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Province表建表语句
	 */
	public static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement," + "province_name text,"
			+ "province_code text)";

	/**
	 * City表建表语句
	 */
	public static final String CREATE_CITY = "create table City ("
			+ "id integer primary key autoincrement," + "city_name text,"
			+ "city_code text,"+"province_code text)";
			
	/**
	 * County表建表语句
	 */
	public static final String CREATE_COUNTY = "create table County ("
			+ "id integer primary key autoincrement," + "city_name text,"
			+ "city_code text,"+"city_id integer)";
	


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
