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
	public static int THEME = R.style.Theme_Sherlock;
	private static final String TAG = StudyActivity.class.getSimpleName();
	static final String MOUNT_SD_CARD = Environment.getExternalStorageDirectory().getPath();
	static final String mntPath = "/dLog1009/studies";

	// Define the controls used in this activity
	ListView listViewStudies;

	// This portion configures the ModelBean for the new ListView adapter
	List<ModelBean> model;	//Define a list of ModelBeans

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.study);	
		listViewStudies = (ListView) findViewById(R.id.listViewStudies);

		
		//TODO: setting up the ArrayAdapter should be done in another thread or via 
		//an asynchronous task because it could may require a long time to terminate
		//worst-case.
		
		// -- Used in the arrayadapter
		model = new ArrayList<ModelBean>();
		
		// Read the contents of the mount path
		File folder = new File(MOUNT_SD_CARD + mntPath);	//point to the contents of the study files.
		
		// Check if the folder exists.
		if (!folder.exists()) {
			Toast.makeText(getApplicationContext(), MOUNT_SD_CARD + mntPath + " not found.", Toast.LENGTH_SHORT).show();
		} else {
			// Loop through all entries of the folder array and put each file into the ArrayList
			String md5 = null;
			for (final File fileEntry : folder.listFiles()) {
				if (!fileEntry.isDirectory()) {
					// Compute the md5 of the file to serve as version control
					try {
						FileInputStream fis = new FileInputStream(new File(MOUNT_SD_CARD + mntPath + "/" + fileEntry.getName()));
						md5 = new String(Hex.encodeHex(DigestUtils.md5(fis)));	//will this cause memory leaks??
					} catch (Exception e) {
						md5 = "";
					} 
					
					//add new list entry into the ArrayList<ModelBean>
					model.add(new ModelBean(fileEntry.getName(),
							DateUtils.getRelativeTimeSpanString(this.getApplicationContext(),
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

		// Instantiate a new ArrayAdapter.
		ArrayAdapter<ModelBean> adapter = new MultiSelectAdapter(this, model);

		// Associate the listview with an adapter
		listViewStudies.setAdapter(adapter);
		listViewStudies.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBoxFieldbook);
				
				ModelBean element = (ModelBean) checkbox.getTag();
				Toast.makeText(getApplicationContext(), element.getFieldBookName() + "-voila!", Toast.LENGTH_SHORT).show();
			}
		});

//		Log.d(TAG, "*****onCreate*****");
	}


	@Override
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
	}
	
	private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //Used to put dark icons on light action bar
//            boolean isLight = SampleList.THEME == R.style.Theme_Sherlock_Light;

//            menu.add("Save")
//                .setIcon(isLight ? R.drawable.ic_compose_inverse : R.drawable.ic_compose)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            menu.add("Search")
//                .setIcon(isLight ? R.drawable.ic_search_inverse : R.drawable.ic_search)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            menu.add("Refresh")
//                .setIcon(isLight ? R.drawable.ic_refresh_inverse : R.drawable.ic_refresh)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            menu.add("Save")
//                .setIcon(isLight ? R.drawable.ic_compose_inverse : R.drawable.ic_compose)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            menu.add("Search")
//                .setIcon(isLight ? R.drawable.ic_search_inverse : R.drawable.ic_search)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            menu.add("Refresh")
//                .setIcon(isLight ? R.drawable.ic_refresh_inverse : R.drawable.ic_refresh)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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
    }

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
						Toast.makeText(getApplicationContext(), String.valueOf(element.intOnCount) + "-checkbox-" + element.getFieldBookName(), Toast.LENGTH_SHORT).show();
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
