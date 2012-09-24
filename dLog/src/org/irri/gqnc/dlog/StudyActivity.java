package org.irri.gqnc.dlog;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

public class StudyActivity extends Activity {
	private static final String TAG = StudyActivity.class.getSimpleName();
	static final String MOUNT_SD_CARD = Environment.getExternalStorageDirectory().getPath();
	static final String mntPath = "/dLog1009/studies";

	// Define the controls used in this activity
	ListView listViewStudies;

	// Use a different approach: ArrayList, SimpleAdapter, and HashMap
	HashMap<String, String> studyObjectMap;
	ArrayList<HashMap<String, String>> studyArrayList;
	SimpleAdapter studyListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // include this for all @Override
		
		// Load the layout and get a handle of the ListView
		setContentView(R.layout.study);	
		listViewStudies = (ListView) findViewById(R.id.listViewStudies);

		// -- Use SimpleAdapter
		studyArrayList = new ArrayList<HashMap<String, String>>();	//create the  new ArrayList

		// Read the contents of the mount path
		File folder = new File(MOUNT_SD_CARD + mntPath);	//point to the contents of the study files.
		
		// Check if the folder exists.
		if (!folder.exists()) {
			Toast.makeText(getApplicationContext(), MOUNT_SD_CARD + mntPath + " not found.", Toast.LENGTH_LONG).show();
		} else {
			// Loop through all entries of the folder array and put each file into the ArrayList
			String md5 = null;
			for (final File fileEntry : folder.listFiles()) {
				if (!fileEntry.isDirectory()) {
					// Record the filename and the file data into the sampleArrayList
					studyObjectMap = new HashMap<String, String>();
					studyObjectMap.put("filename", fileEntry.getName());
					studyObjectMap.put(
							"datemodified",
							DateUtils.getRelativeTimeSpanString(
									this.getApplicationContext(),
									fileEntry.lastModified()).toString());
					studyObjectMap.put("fullpath", fileEntry.getAbsolutePath());	//will come in handy later
					
					// Compute the md5 of the file to serve as version control
					try {
						FileInputStream fis = new FileInputStream(new File(MOUNT_SD_CARD + mntPath + "/" + fileEntry.getName()));
						md5 = new String(Hex.encodeHex(DigestUtils.md5(fis)));	//will this cause memory leaks??
					} catch (Exception e) {
						md5 = "";
					} finally {
						studyObjectMap.put("version", md5);
					}
					
					studyArrayList.add(studyObjectMap); // one element array only
				}
			}
			
			// Sort the ArrayList by name
			Collections.sort(studyArrayList, new Comparator<HashMap<String, String>>() {
				@Override
				public int compare(HashMap<String, String> arg0, HashMap<String, String> arg1) {
					return (arg0.get("filename")).compareToIgnoreCase(arg1.get("filename"));
				}
				
			});
			// sampleArrayList must be sorted at this time..
		}

		// Define the adapter
		studyListAdapter= new SimpleAdapter(this, studyArrayList, R.layout.studyrow, 
				new String[] {"filename", "datemodified", "version"},
				new int[] { R.id.textViewStudyName, R.id.textViewStudyDate, R.id.textViewStudyVersion}
		);
		studyListAdapter.setViewBinder(viewBinder);
		
		// Associate the newly defined adapter to the listView
		listViewStudies.setAdapter(studyListAdapter);

		// Implement an onClickListener
		listViewStudies.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// determine the item clicked using the "position" parameter
				HashMap<String, String> studyObjectMapLocal = studyArrayList.get(position);
				final String studyObj = studyObjectMapLocal.get("filename");
				final String studyPath = studyObjectMapLocal.get("fullpath");	//will come in handy later..
				
				// Try the DbHelper class
				String dbName = "/mnt/sdcard/dLog1009/studies/ontology.db";	// just to try the DbHelper class
//				String dbName = "/mnt/sdcard/ontology_root.db";
				
				SQLiteDatabase sqlDb=null;
				try {
					sqlDb = SQLiteDatabase.openDatabase(dbName, null, 
							SQLiteDatabase.OPEN_READWRITE 
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS
							| SQLiteDatabase.CREATE_IF_NECESSARY);
					
					ContentValues val = new ContentValues();
				    val.put("domain", "property_new");	// could add more val.puts

				    // this is working...
				    int rowsAffected = sqlDb.update("vocabulary", val, "id=?", new String[]{"1"});
				    
				    // use this to extract the column names in the database just read..
//				    Cursor cursor = sqlDb.query("vocabulary",null,null,null,null,null,null);
				    Cursor cursor = sqlDb.rawQuery("select * from sqlite_master", null);
				    Toast.makeText(getApplicationContext(), Arrays.toString(cursor.getColumnNames()), Toast.LENGTH_LONG).show();
				    if (!cursor.isClosed()) cursor.close();
				    
//					Toast.makeText(getApplicationContext(), String.valueOf(rowsAffected)  ,Toast.LENGTH_LONG).show();
//					Toast.makeText(getApplicationContext(), String.valueOf(sqlDb.getMaximumSize()), Toast.LENGTH_LONG).show();
					
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				
				if (sqlDb != null) sqlDb.close(); //close the opened database
				// Just show the name of the opened database
//				Toast.makeText(getApplicationContext(), db.getDatabaseName(), Toast.LENGTH_LONG).show();
				
//				db.close();	//close what is opened.
				
				
			}
				
		});
		
		// Just try opening the database to test some code... Refactor later
		String pathToDb = MOUNT_SD_CARD + mntPath + "/controlled_vocabulary.db";
		
		DbFile dbFile = new DbFile(pathToDb, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
		dbFile.open();
		Toast.makeText(getApplicationContext(), (dbFile == null?"Error opening file":"Great!"), Toast.LENGTH_LONG).show();
		dbFile.close();
		
		// -- End of SimpleAdapter
		Log.d(TAG, "*****onCreate*****");
	}

	static final ViewBinder viewBinder = new ViewBinder() {

		// view is the mapped TextViews in the studyrow layout
		// data is the component of the hashmap that is mapped to the particular
		// textview
		// textRepresentation is the value of the object
		@Override
		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			// ((TextView) view).setText(data.toString());
			// return true;
			return false;
		}

	};

}
