package com.mapzen;

import java.io.File;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import com.mapzen.data.SettingsManager;

import android.app.ProgressDialog;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class MapzenPreferenceActivity extends PreferenceActivity implements OnPreferenceClickListener {
	
	public static final String PREF_DOWNLOAD_OSM = "automatic_osm_data_download";
	
	private CheckBoxPreference isOsmDownloadAutomatic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		getPrefsFromFile();
		
		isOsmDownloadAutomatic = (CheckBoxPreference)findPreference(PREF_DOWNLOAD_OSM);
		isOsmDownloadAutomatic.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean isChecked = (Boolean)newValue ;
				SettingsManager.getInstance().putBoolean(PREF_DOWNLOAD_OSM, isChecked);
				return true;
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			updateCacheSizeOnUI();
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("clear_map_tiles_cache")) {
			File cacheDir = OpenStreetMapTileProviderConstants.OSMDROID_PATH;
			new DeleteCacheTask().execute(cacheDir);
		}
		return true;
	}
	
	private void getPrefsFromFile() {
		boolean isOsmDataDownloadAutomatic = SettingsManager.getInstance().getBoolean(PREF_DOWNLOAD_OSM, true);
		((CheckBoxPreference)findPreference(PREF_DOWNLOAD_OSM)).setChecked(isOsmDataDownloadAutomatic);
	}

	private void updateCacheSizeOnUI() {
		long cacheDirSize = dirSize(OpenStreetMapTileProviderConstants.OSMDROID_PATH);
		String cacheSize = String.format("%.1f", cacheDirSize/1024f/1024f);
		
		Preference clearMapTilesPref = findPreference("clear_map_tiles_cache");
		clearMapTilesPref.setOnPreferenceClickListener(this);
		StringBuilder sb = new StringBuilder(getString(R.string.current_cache_size))
			.append(": ")
			.append(cacheSize)
			.append(" Mb");
		clearMapTilesPref.setSummary(sb.toString());
	}
	
	/**
	 * Return the size of a directory in bytes
	 */
	private static long dirSize(File dir) {
	    long result = 0;
	    File[] fileList = dir.listFiles();

	    for(int i = 0; i < fileList.length; i++) {
	        // Recursive call if it's a directory
	        if(fileList[i].isDirectory()) {
	            result += dirSize(fileList[i]);
	        } else {
	            // Sum the file size in bytes
	            result += fileList[i].length();
	        }
	    }
	    return result; // return the file size
	}
	
	private static boolean delete(File dir) {
		File[] fileList = dir.listFiles();
		boolean result = true;
	    for(int i = 0; i < fileList.length; i++) {
	        // Recursive call if it's a directory
	        if(fileList[i].isDirectory()) {
	            if (!delete(fileList[i]))
	            	result = false;
	        } else {
	            if(!fileList[i].delete())
	            	result = false;
	        }
	    }
	    return result;
	}
	
	class DeleteCacheTask extends AsyncTask<File, Void, Boolean> {
		
		ProgressDialog progressDialog = new ProgressDialog(MapzenPreferenceActivity.this);
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//TODO: open dialog here
			progressDialog.setTitle(R.string.removing_cache);
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(File... params) {
			return delete(params[0]);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(!result)
				Toast.makeText(MapzenPreferenceActivity.this.getApplicationContext(), R.string.failed_to_remove_cache , Toast.LENGTH_SHORT).show();
		
			//TODO: close dialog here.
			progressDialog.dismiss();
		}
				
	}

}
