package org.irri.gqnc.dlog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DbFile extends Object {
	// Define some constants here..
	public static final int NO_LOCALIZED_COLLATORS = SQLiteDatabase.NO_LOCALIZED_COLLATORS;
	public static final int OPEN_READWRITE = SQLiteDatabase.OPEN_READWRITE;
	public static final int OPEN_READONLY =  SQLiteDatabase.OPEN_READONLY;
	
	private final String fileName; // contains the name of the database file
	private final int dbMode; // use this as flag of the database mode: ReadWrite or ReadOnly  
	private SQLiteDatabase db = null; // use this for the SQLiteDatabase object

	// Specify the filename and the desired database mode when opening a database file.
	public DbFile(String fileName, int dbMode) {
		super(); // mandatory call...
		this.fileName = fileName;
		this.dbMode = dbMode;
	}

	// Create a method to open the database file
	public void open() {
		// set the CursorFactory parameter to null... it's no use at this time..
		try {
			this.db = SQLiteDatabase.openDatabase(this.fileName, null, NO_LOCALIZED_COLLATORS | OPEN_READWRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Close the database file
	public void close() {
		// Close the open database
		this.db.close();
	}

	// Support transactions
	public void beginTransaction() { this.db.beginTransaction(); }
	public void setTransactionSuccessful() { this.db.setTransactionSuccessful(); }	// commit changes when transaction is ended
	public void endTransaction() { this.db.endTransaction(); }
	
	
	// Allows rawQuery 
	public Cursor rawQuery(String query, String[] selectionArgs) {
		return this.db.rawQuery(query, selectionArgs);
	}
	
}
