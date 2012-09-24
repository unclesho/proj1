package org.irri.gqnc.dlog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 1;
	private static final CursorFactory CURSOR_FACTORY = null;
	private Context context;

	public DbHelper(Context context, String dbName, CursorFactory factory, int version) {
		//CursorFactory and DbVersion are actually not relevant for dLog
		super(context, dbName, CURSOR_FACTORY, DB_VERSION);
		this.context = context;	// just take note of the context for whatever it's worth
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
