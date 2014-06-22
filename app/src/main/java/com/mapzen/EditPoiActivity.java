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

import java.util.ArrayList;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.ResourceManager;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.io.OSMFacade;
import com.mapzen.map.overlays.OsmPoisOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditPoiActivity extends Activity implements MapzenConstants {

    private static final int EDIT_NAME_DIALOG = 0;
    private static final int EDIT_WEBSITE_DIALOG = 1;
    private static final int EDIT_PHONE_DIALOG = 2;
    private static final int EDIT_ADDRESS_DIALOG = 3;
    private static final int EDIT_OPENING_HOURS_DIALOG = 4;
    private static final int EDIT_DESCRIPTION_DIALOG = 5;
    private static final int SAVE_TO_OSM_PROGRESS_DIALOG = 6;
    private static final int UNSAVED_POI_WARNING_DIALOG = 7;

    private static OsmNode poi;
    private PoiTypeControlArrayAdapter poiInfoListAdapter;
    private ListView poiDataListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        if (i != null) {
            setContentView(R.layout.edit_poi);
            long id = i.getLongExtra("id", -10);
            poi = new OsmNode(OsmPoisOverlay.getPoi(id));
        }

        if (poi == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            poiInfoListAdapter = new PoiTypeControlArrayAdapter(this,
                    R.layout.edit_poi_details_first_row);
            poiDataListView = (ListView) findViewById(R.id.poiTypeListView);
            poiDataListView.setAdapter(poiInfoListAdapter);
            poiDataListView.setOnItemClickListener(onPoiDetailsListItemClickListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPoiTypeEditControlAndIcon();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SELECT_POI_CATEGORY_REQUEST_CODE:
        case SELECT_POI_SUB_TYPE_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                String type = data.getStringExtra("type");
                if (type != null) {
                    poi.setType(type);
                    refreshPoiTypeEditControlAndIcon();
                }
            }
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_poi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.savePoiMenuItemId:
            trySavePOI();
            break;
        case R.id.cancelPoiMenuItemId:
            cancelEditPOI();
            break;
        case R.id.deletePoiMenuItemId:
            new DeleteInOsmAsyncTask().execute(poi);
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelEditPOI() {
        setResult(POI_CANCELED_ACTIVITY_RESULT);
        finish();
    }

    private void trySavePOI() {
        if (validatePoi()) {
            new SaveInOsmAsyncTask().execute(poi);
        } else {
            Toast.makeText(this,R.string.poi_is_invalid, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isPoiChanged() {
        return !poi.equals(OsmPoisOverlay.getPoi(poi.getId()));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (isPoiChanged())) {
            showDialog(UNSAVED_POI_WARNING_DIALOG);
            return true;
        }
        else
            return super.onKeyDown(keyCode, event);
    }

    protected boolean validatePoi() {
        OsmNode vPoi = poi;
        String type = vPoi.getType();
        boolean validationResult = true;

        if (type == null)
            validationResult = false;
        else
            if (type.equals("unknown"))
                validationResult = false;

        return validationResult;
    }

    private void refreshPoiTypeEditControlAndIcon() {
        poiInfoListAdapter.clear();

        String name = poi.getName();
        poiInfoListAdapter.add(((name != null) ? name : ""));

        String poiType = poi.getType();
        if (poiType != null)
            poiInfoListAdapter.add(poiType);
        else
            poiInfoListAdapter.add(new String("select_place_type"));

        String poiAddress = poi.get_addr_housenumber();

        if (poiAddress == null)
            poiInfoListAdapter.add(getResources().getString(
                    R.string.add_address_info));
        else
            poiInfoListAdapter.add(poi.getFullAddressString());

        String website = poi.get_website();
        if (website != null)
            poiInfoListAdapter.add(website);
        else
            poiInfoListAdapter.add(getResources().getString(
                    R.string.add_website));

        String phone = poi.get_phone();
        if (phone != null)
            poiInfoListAdapter.add(phone);
        else
            poiInfoListAdapter.add(getResources().getString(
                    R.string.add_phone));

        String opening_hours = poi.get_opening_hours();
        if (opening_hours != null)
            poiInfoListAdapter.add(opening_hours);
        else
            poiInfoListAdapter.add(getResources().getString(
                    R.string.add_opening_hours));

        String description = poi.get_description();
        if (description != null)
            poiInfoListAdapter.add(description);
        else
            poiInfoListAdapter.add(getResources().getString(
                    R.string.add_description));


    }

    protected Dialog onCreateDialog(int id) {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v;
        AlertDialog dialog = null;
        switch (id) {
        case EDIT_NAME_DIALOG:
            v = vi.inflate(R.layout.edit_name_dialog, null);
            if (poi.getName() != null)
                ((EditText)v).setText(poi.getName());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.name_caption)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.setName(((EditText)v).getText().toString());
                        removeDialog(EDIT_NAME_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_NAME_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_NAME_DIALOG);
                }
            });
            break;

        case EDIT_WEBSITE_DIALOG:
            v = vi.inflate(R.layout.edit_website_dialog, null);
            if (poi.get_website() != null)
                ((EditText)v).setText(poi.get_website());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.website)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.set_website(((EditText)v).getText().toString());
                        removeDialog(EDIT_WEBSITE_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_WEBSITE_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_WEBSITE_DIALOG);
                }
            });
            break;
        case EDIT_PHONE_DIALOG:
            v = vi.inflate(R.layout.edit_phone_dialog, null);
            if (poi.get_phone() != null)
                ((EditText)v).setText(poi.get_phone());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.phone_number)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.set_phone(((EditText)v).getText().toString());
                        removeDialog(EDIT_PHONE_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_PHONE_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_PHONE_DIALOG);
                }
            });
            break;
        case EDIT_DESCRIPTION_DIALOG:
            v = vi.inflate(R.layout.edit_description_dialog, null);
            if (poi.get_description() != null)
                ((EditText)v).setText(poi.get_description());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.description)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.set_description(((EditText)v).getText().toString());
                        removeDialog(EDIT_DESCRIPTION_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_DESCRIPTION_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_DESCRIPTION_DIALOG);
                }
            });
            break;
        case EDIT_ADDRESS_DIALOG:
            v = vi.inflate(R.layout.edit_address_dialog, null);
            if (poi.get_addr_housenumber() != null)
                ((EditText)v.findViewById(R.id.poiAddrHousenumber)).setText(poi.get_addr_housenumber());
            if (poi.get_addr_street() != null)
                ((EditText)v.findViewById(R.id.poiAddrStreet)).setText(poi.get_addr_street());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.address_information)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.set_addr_housenumber(((EditText)v.findViewById(R.id.poiAddrHousenumber)).getText().toString());
                        poi.set_addr_street(((EditText)v.findViewById(R.id.poiAddrStreet)).getText().toString());
                        removeDialog(EDIT_ADDRESS_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_ADDRESS_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_ADDRESS_DIALOG);
                }
            });
            break;
        case EDIT_OPENING_HOURS_DIALOG:
            v = vi.inflate(R.layout.edit_opening_hours_dialog, null);
            if (poi.get_opening_hours() != null)
                ((EditText)v).setText(poi.get_opening_hours());
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.opening_hours)
                .setView(v)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        poi.set_opening_hours(((EditText)v).getText().toString());
                        removeDialog(EDIT_OPENING_HOURS_DIALOG);
                        refreshPoiTypeEditControlAndIcon();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(EDIT_OPENING_HOURS_DIALOG);
                    }
                })
                .create();
            dialog.setOnCancelListener(new OnCancelListener() {


                public void onCancel(DialogInterface dialog) {
                    removeDialog(EDIT_OPENING_HOURS_DIALOG);
                }
            });
            break;
        case SAVE_TO_OSM_PROGRESS_DIALOG:
            v = vi.inflate(R.layout.saving_to_osm_dialog, null);
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setView(v)
                .setCancelable(false)
                .create();
            break;
        case UNSAVED_POI_WARNING_DIALOG:
            dialog = new AlertDialog.Builder(EditPoiActivity.this)
                .setTitle(R.string.dialog_poi_unsaved)
                .setMessage(R.string.dialog_poi_unsaved_message)
                .setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(UNSAVED_POI_WARNING_DIALOG);
                        trySavePOI();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(UNSAVED_POI_WARNING_DIALOG);
                        cancelEditPOI();
                    }
                })
                .create();
        default:
            break;
        }
        return dialog;
    }

    private class PoiTypeControlArrayAdapter extends ArrayAdapter<String> {

        public PoiTypeControlArrayAdapter(Context context,
                int textViewResourceId) {
            super(context, textViewResourceId);
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (position == 0)
                if (getItem(position).equals("")) {
                    v = vi.inflate(R.layout.edit_poi_details_first_row_empty, null);
                }
                else v= vi.inflate(R.layout.edit_poi_details_first_row, null);
            else
                v = vi.inflate(R.layout.poi_type_control_type_row, null);

            String c = getItem(position);
            if (c != null) {
                String caption = null;
                String value = c;
                if (position == 0) {
                    ImageView poiIcon = (ImageView) v
                            .findViewById(R.id.poiIconInEditMode);
                    TextView poiName = (TextView) v
                            .findViewById(R.id.poiNameTextViewInEditMode);
                    String iconName = (poi.getType() != null) ? poi.getType() : "unknown";
                    poiIcon.setImageDrawable(ResourceManager.getInstance()
                            .getDrawableAsset("icons_50x36/" + iconName + ".png"));
                    if (poi.getType() == null)
                        poiIcon.setVisibility(View.GONE);
                    else
                        poiIcon.setVisibility(View.VISIBLE);
                    if (!c.equals(""))
                        poiName.setText(c);
                } else {
                    TextView typeLabel = (TextView) v
                            .findViewById(R.id.poiTypeEditControlTypeLabel);
                    TextView typeValue = (TextView) v
                            .findViewById(R.id.poiTypeEditControlTypeValue);
                    switch (position) {
                    case 1:
                        caption = getResources().getString(R.string.type);
                        String poiCategory = OsmDriver.getInstance().getCategory(c);
                        String poiTypeCaption = ResourceManager.getInstance()
                        .getStringResource(c);
                        if (poiCategory != "unknown") {
                            String poiCategoryCaption = ResourceManager.getInstance().getStringResource(poiCategory);
                            value = poiCategoryCaption + " : " + poiTypeCaption;
                        } else {
                            value = poiTypeCaption;
                        }

                        break;
                    case 2:
                        caption = getResources().getString(
                                R.string.address_information);
                        break;
                    case 3:
                        caption = getResources().getString(R.string.website);
                        break;
                    case 4:
                        caption = getResources().getString(
                                R.string.phone_number);
                        break;
                    case 5:
                        caption = getResources().getString(
                                R.string.opening_hours);
                        break;
                    case 6:
                        caption = getResources()
                                .getString(R.string.description);
                        break;
                    default:
                        throw new IllegalStateException(
                                "it is impossible to be more than 6 items here");
                    }
                    typeLabel.setText(caption);
                    typeValue.setText(value);
                }
            }
            return v;
        }

    }

    private OnItemClickListener onPoiDetailsListItemClickListener = new OnItemClickListener() {


        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            Intent i = new Intent();
            switch (position) {
            case 0:
                showDialog(EDIT_NAME_DIALOG);
                break;
            case 1:
                i.setClass(EditPoiActivity.this, PoiGroupsListActivity.class);
                startActivityForResult(i, SELECT_POI_CATEGORY_REQUEST_CODE);
                break;
            case 2:
                showDialog(EDIT_ADDRESS_DIALOG);
                break;
            case 3:
                showDialog(EDIT_WEBSITE_DIALOG);
                break;
            case 4:
                showDialog(EDIT_PHONE_DIALOG);
                break;
            case 5:
                showDialog(EDIT_OPENING_HOURS_DIALOG);
                break;
            case 6:
                showDialog(EDIT_DESCRIPTION_DIALOG);
                break;
            default:
                break;
            }
        }

    };


    /*****************************************************************
     * Inner classes
     ****************************************************************/

    private class SaveInOsmAsyncTask extends AsyncTask<OsmNode, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            showDialog(SAVE_TO_OSM_PROGRESS_DIALOG);
        }

        @Override
        protected Boolean doInBackground(OsmNode... params) {
            long poiId = params[0].getId();
            Boolean result;
            if (poiId < 0) {
                result = OSMFacade.createNode(poi);
                if (result)
                    OsmPoisOverlay.removePoi(poiId); //remove poi with negative id because poiId changed
            } else
                result = OSMFacade.updateNode(poi);

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialog(EditPoiActivity.SAVE_TO_OSM_PROGRESS_DIALOG);
            if (result) {
                OsmPoisOverlay.addPoi(poi); //replaces old poi with new one
                setResult(POI_SAVED_ACTIVITY_RESULT);
                finish();
            }
        }
    }

    private class DeleteInOsmAsyncTask extends AsyncTask<OsmNode, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            showDialog(SAVE_TO_OSM_PROGRESS_DIALOG);
        }

        @Override
        protected Boolean doInBackground(OsmNode... params) {
            long poiId = params[0].getId();
            Boolean result = true;
            if (poiId > 0) {
                result = OSMFacade.removeNode(poi);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialog(SAVE_TO_OSM_PROGRESS_DIALOG);
            if (result) {
                OsmPoisOverlay.removePoi(poi.getId());
                setResult(POI_DELETED_ACTIVITY_RESULT);
                finish();
            }
        }
    }

}
