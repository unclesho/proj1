package org.irri.gqnc.dlog;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FieldbookActivity extends SherlockActivity {
	public static int THEME = R.style.Theme_Sherlock;	// Don't delete!
    ActionMode mMode;	// for the contextual ActionMode
	Boolean isDisplayedActionMode;	// signals that the ActionMode is up already

	static final String MOUNT_SD_CARD = Environment.getExternalStorageDirectory().getPath();
	static final String mntPath = "/dLog1009/studies";

	// Define the controls and configures the ModelBean for the ListView adapter
	ListView listViewStudies;
	List<ModelBean> model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.study);	
		listViewStudies = (ListView) findViewById(R.id.listViewStudies);
		// Prepare the ArrayAdapter in a non-UI thread
		new PrepareArrayAdapter().execute(MOUNT_SD_CARD + mntPath);
		
		isDisplayedActionMode = false;	// ActionMode initially off -- of course.
	} //onCreate

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("New")
            .setIcon(R.drawable.content_new)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Refresh")
            .setIcon(R.drawable.navigation_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
		return true;
	}*/

	/**
	 *  This will prepare the array adapter in the background and then associate it to the ListView.
	 This ensures that the UI will not block when the number of files in the assigned directory
	 for the fieldbook files is unusually large.
	 The first parameter(of the generic class) is the input to the doInBackground function. The
	 2nd parameter goes to the progress indicator, and the 3rd parameter goes to the result.
	
	 There is a very nice tutorial at [32:48] in 08_18 video by marakana. 
	 The way to access resources in the Activity from this internal asynchronous task is by using a
	 call similar to this:
	 		FieldbookActivity.this.getString(R.string.theStringHere);
	 * @author RAnacleto
	 *
	 */
	private final class PrepareArrayAdapter extends AsyncTask<String, String, List<ModelBean>> {
		
		// This is called to do the work inside this separate thread.
		@Override
		protected List<ModelBean> doInBackground(String... pathToFieldbook) {
			// Define the return object
			List<ModelBean> model = new ArrayList<ModelBean>();
			
			// Read the contents of the mount path: i.e. "MOUNT_SD_CARD + mntPath"
			File folder = new File(pathToFieldbook[0]);	// just get the first parameter in the input String array
			
			if (folder.exists()) {
				// Loop through all entries of the folder array and put each file into the ArrayList
				String md5 = null;
				for (final File fileEntry : folder.listFiles()) {
					if (!fileEntry.isDirectory()) {
						// Compute the md5 of the file to serve as version control
						try {
							FileInputStream fis = new FileInputStream(new File(pathToFieldbook[0] + "/" + fileEntry.getName()));
							md5 = new String(Hex.encodeHex(DigestUtils.md5(fis)));
						} catch (Exception e) {
							md5 = "";
						} 
						
						//add new list entry into the ArrayList<ModelBean>
						model.add(new ModelBean(fileEntry.getName(),
							DateUtils.getRelativeTimeSpanString(FieldbookActivity.this.getApplicationContext(),
								fileEntry.lastModified()).toString(), 
								md5));
					}
				}
				
				//Sort the model
				Collections.sort(model, new Comparator<ModelBean>() {
					@Override
					public int compare(ModelBean lhs, ModelBean rhs) {
						return lhs.getFieldBookName().compareToIgnoreCase(rhs.getFieldBookName());
					}
				});
			}
			return model;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
		}

		// This will be called when the thread is done with its work.
		@Override
		protected void onPostExecute(List<ModelBean> result) {
			super.onPostExecute(result);

			// Instantiate a new ArrayAdapter.
			ArrayAdapter<ModelBean> adapter = new MultiSelectAdapter(FieldbookActivity.this, result);

			// Associate the listview with an adapter
			FieldbookActivity.this.listViewStudies.setAdapter(adapter);
			FieldbookActivity.this.listViewStudies.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
					CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBoxFieldbook);
					
					ModelBean element = (ModelBean) checkbox.getTag();
//					Toast.makeText(FieldbookActivity.this.getApplicationContext(), element.getFieldBookName() + "-voila!", Toast.LENGTH_SHORT).show();
					// TODO: Create the logic here to open the sqlite file when it is clicked directly..
				}
			});
			
			Toast.makeText(FieldbookActivity.this.getApplicationContext(),
					"AsyncTask done.", Toast.LENGTH_SHORT).show();
		}		
	} // end of the asynchronous task
	
	private final class FieldbookActivityActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add("Archive")
                .setIcon(R.drawable.device_access_sd_storage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menu.add("Share")
                .setIcon(R.drawable.social_share)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            Toast.makeText(ActionModes.this, "Got click: " + item, Toast.LENGTH_SHORT).show();
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    } // FieldbookActivityActionMode

	// Use this private class to hold state values for each checkbox
	private final static class ModelBean {
	 	private String fieldBookName;	//prefix this with the index number in the list to prevent name clashes in case of duplicates
	 	private String fieldBookDate;
	 	private String fieldBookVersion;
		private boolean selected;
		static int intOnCount;

		//instantiate a new ModelBean
	 	public ModelBean(String fieldBookName, String fieldBookDate, String fieldBookVersion) {
			this.fieldBookName = fieldBookName;
			this.fieldBookDate= fieldBookDate;
			this.fieldBookVersion = fieldBookVersion;
			selected = false;
			intOnCount=0;
		}

		public String getFieldBookName() {
			return fieldBookName;
		}

		public void setFieldBookName(String fieldBookName) {
			this.fieldBookName = fieldBookName;
		}

		public String getFieldBookDate() {
			return fieldBookDate;
		}

		public void setFieldBookDate(String fieldBookDate) {
			this.fieldBookDate = fieldBookDate;
		}

		public String getFieldBookVersion() {
			return fieldBookVersion;
		}

		public void setFieldBookVersion(String fieldBookVersion) {
			this.fieldBookVersion = fieldBookVersion;
		}

	 	public boolean isSelected() {
			return selected;
		}

	 	public void setSelected(boolean selected) {
			this.selected = selected;
			if (selected) intOnCount++;
			else intOnCount--;
		}
	}

	private class MultiSelectAdapter extends ArrayAdapter<ModelBean> {
	 	private final List<ModelBean> list;
		private final Activity context;

	 	public MultiSelectAdapter(Activity context, List<ModelBean> list) {
			super(context, R.layout.fieldbookrow2, list);
			this.context = context;
			this.list = list;
		}

	 	// use this to hold the elements of the row layout
	 	private class ViewHolder {
			protected TextView textViewFieldbookDate;
			protected CheckBox checkBoxFieldbookName;
			protected TextView textViewFieldbookVersion;
			protected TextView textViewFieldbookName;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				LayoutInflater inflator = context.getLayoutInflater();
				view = inflator.inflate(R.layout.fieldbookrow2, null);
				
				//map the row layout elements to the ViewHolder object
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.textViewFieldbookDate = (TextView) view.findViewById(R.id.textViewStudyDate);
				viewHolder.textViewFieldbookVersion = (TextView) view.findViewById(R.id.textViewStudyVersion);
				viewHolder.textViewFieldbookName = (TextView) view.findViewById(R.id.textViewStudyName);
				viewHolder.checkBoxFieldbookName = (CheckBox) view.findViewById(R.id.checkBoxFieldbook);

				// setup a click listener to catch toggle changes.. 
				viewHolder.checkBoxFieldbookName.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkBoxFieldbook);
						ModelBean element = (ModelBean) viewHolder.checkBoxFieldbookName.getTag();
						element.setSelected(checkbox.isChecked());

						// TODO: Put the logic here for the ActionMode
						if (ModelBean.intOnCount == 0) {
							FieldbookActivity.this.mMode.finish(); // Close the ActionBar
							FieldbookActivity.this.isDisplayedActionMode = false;
						} else if(!FieldbookActivity.this.isDisplayedActionMode) {
							FieldbookActivity.this.mMode = startActionMode(new FieldbookActivityActionMode());
							FieldbookActivity.this.isDisplayedActionMode = true; //raise the flag
						}
						
						/*Toast.makeText(
							getApplicationContext(),
							String.valueOf(ModelBean.intOnCount) + "-checkbox-" + element.getFieldBookName(),
							Toast.LENGTH_SHORT).show();*/
					}
				});

				//This is the inflated view modified by the inflater object above.
				view.setTag(viewHolder);
				viewHolder.checkBoxFieldbookName.setTag(list.get(position));	//so that the listbox will be uniquely identified
			} else {
				view = convertView;
				((ViewHolder) view.getTag()).checkBoxFieldbookName.setTag(list.get(position));
			}
			
			//
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.textViewFieldbookName.setText(list.get(position).getFieldBookName());
			holder.textViewFieldbookDate.setText(list.get(position).getFieldBookDate());
			holder.textViewFieldbookVersion.setText(list.get(position).getFieldBookVersion());
			holder.checkBoxFieldbookName.setChecked(list.get(position).isSelected());
			
			return view;
		}
	}
}
