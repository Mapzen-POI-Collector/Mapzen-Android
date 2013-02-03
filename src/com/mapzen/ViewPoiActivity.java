/*Copyright (c) 2011-2012, Cloudmade
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/

package com.mapzen;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.ResourceManager;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.map.overlays.OsmPoisOverlay;
import com.mapzen.util.StringPair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ViewPoiActivity extends Activity implements MapzenConstants {

	private OsmNode poi;
	private PoiDataArrayAdapter poiDataListAdapter;
	private ListView poiDataListView; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		Intent i = getIntent();
		if (i != null) {
			setContentView(R.layout.view_poi);
			long id = i.getLongExtra("id", -1);
			poi = OsmPoisOverlay.getPoi(id);
			poiDataListAdapter = new PoiDataArrayAdapter(this,
					R.layout.view_poi_details_first_row);
			poiDataListView = (ListView) findViewById(R.id.viewPoiDetailsListView);
			poiDataListView.setAdapter(poiDataListAdapter);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshViewData();
	}
	
	/* Constructs menu from resources */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_poi_screen_menu, menu);
		if (poi.isReadOnly() || !OsmPoisOverlay.possibleToDrag(poi.getId())) {
			menu.getItem(0).setEnabled(false);
		}
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_poi_menu_item:
			Intent i = new Intent(ViewPoiActivity.this, EditPoiActivity.class);
			i.putExtra("id", poi.getId());
			startActivityForResult(i, EDIT_POI_ACTIVITY_REQUEST_CODE); 
			break;
		//this menu item is disabled
		case R.id.lookup_poi_in_osm:
			StringBuilder sb = new StringBuilder(OSM_SERVER_ADDRESS)
				.append("/browse/node/")
				.append(poi.getId());			
			Intent intent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse(sb.toString()));
			startActivity(intent);
			break;
		default: break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EDIT_POI_ACTIVITY_REQUEST_CODE:
			if (resultCode == POI_SAVED_ACTIVITY_RESULT)
				refreshViewData();
			if (resultCode == POI_DELETED_ACTIVITY_RESULT) {
				this.setResult(POI_DELETED_ACTIVITY_RESULT);
				finish();
			}
			break;

		default:
			break;
		}
	}

	private void refreshViewData() {
		
		poiDataListAdapter.clear();
		
		String name = poi.getName();
		poiDataListAdapter.add(new StringPair("name",((name != null) ? name : "")));

		String poiType = poi.getType();
		if (poiType != null)
			poiDataListAdapter.add(new StringPair("type",poiType));
		else
			poiDataListAdapter.add(new StringPair("type","unknown"));

		String poiAddress = poi.getFullAddressString();
		if (poiAddress != null)
			poiDataListAdapter.add(new StringPair("address_information", poiAddress));

		String website = poi.get_website();
		if (website != null)
			poiDataListAdapter.add(new StringPair("website",website));

		String phone = poi.get_phone();
		if (phone != null)
			poiDataListAdapter.add(new StringPair("phone_number", phone));

		String opening_hours = poi.get_opening_hours();
		if (opening_hours != null)
			poiDataListAdapter.add(new StringPair("opening_hours",opening_hours));

		String description = poi.get_description();
		if (description != null)
			poiDataListAdapter.add(new StringPair("description",description));
		
		if ((!OsmPoisOverlay.possibleToDrag(poi.getId())) || (poi.isReadOnly())) {
			poiDataListAdapter.add(new StringPair("not_editable",getString(R.string.not_editable_message)));
		}
	}
	
	private class PoiDataArrayAdapter extends ArrayAdapter<StringPair> {

		public PoiDataArrayAdapter(Context context,
				int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (position == 0)
				v = vi.inflate(R.layout.view_poi_details_first_row, null);
			else
				v = vi.inflate(R.layout.view_poi_details_general_row, null);
			
			StringPair dataItem = getItem(position);
			
			if (dataItem.key != null) {
				String caption = null;
				String value = dataItem.value;
				if (dataItem.key.equals("name")) {
					ImageView poiIcon = (ImageView) v
							.findViewById(R.id.ViewPoiDetailsListViewIconId);
					TextView poiName = (TextView) v
							.findViewById(R.id.ViewPoiDetailsListViewNameId);
					String iconName = (poi.getType() != null) ? poi.getType() : "unknown";
					poiIcon.setImageDrawable(ResourceManager.getInstance()
							.getDrawableAsset("icons_50x36/" + iconName + ".png"));
					poiName.setText(dataItem.value);
				} else {
					if (dataItem.key.equals("type")) {
						if (!dataItem.value.equals("unknown"))
						{
							String poiCategoryName = ResourceManager.getInstance().getStringResource(OsmDriver.getInstance().getCategory(dataItem.value));
							String poiTypeName = ResourceManager.getInstance().getStringResource(dataItem.value);
							value = poiCategoryName + " : " + poiTypeName;
						} else {
							String tags = ViewPoiActivity.this.poi.getTags().toString();
							value = tags;
						} 
					}
				TextView typeLabel = (TextView) v
					.findViewById(R.id.ViewPoiDetailsListViewLabelId);
				TextView typeValue = (TextView) v
					.findViewById(R.id.ViewPoiDetailsListViewValueId);
				
				caption = ResourceManager.getInstance().getStringResource(dataItem.key);
				typeLabel.setText(caption);
				typeValue.setText(value);
				
				if (dataItem.key.equals("not_editable"))
					typeLabel.setVisibility(View.GONE);
				
				}
			}
			return v;
		}

	}
	

}
