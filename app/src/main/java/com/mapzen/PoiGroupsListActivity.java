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

import java.util.List;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.ResourceManager;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PoiGroupsListActivity extends ListActivity implements MapzenConstants {

    private PoiGroupsAdapter poiGroupsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.poi_categories_list);

        List<String> categories = OsmDriver.getInstance().getCategories();
        poiGroupsListAdapter = new PoiGroupsAdapter(getApplicationContext(), R.layout.categories_list_view_row, categories);
        setListAdapter(poiGroupsListAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String category_name = poiGroupsListAdapter.getItem(position);

        Intent i = new Intent(this.getApplicationContext(),PoiSubTypesListActivity.class);
        i.putExtra("category", category_name);
        startActivityForResult(i, SELECT_POI_SUB_TYPE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case SELECT_POI_SUB_TYPE_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                this.setResult(RESULT_OK, data);
                this.finish();
            }
            break;
        default:
            break;
        }
    }

    private class PoiGroupsAdapter extends ArrayAdapter<String> {

        public PoiGroupsAdapter(Context context, int textViewResourceId, List<String> categories) {
            super(context, textViewResourceId, categories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
              View v = convertView;
              if (v == null) {
                  v = getLayoutInflater().inflate(R.layout.categories_list_view_row, parent, false);
              }
              String c = getItem(position);
              if (c != null) {
                      TextView nameView = (TextView) v.findViewById(R.id.categoryListViewItemNameId);
                      nameView.setText(ResourceManager.getInstance().getStringResource(c));
                      ImageView imageView = (ImageView) v.findViewById(R.id.categoryListViewItemIconId);
                      imageView.setImageDrawable(ResourceManager.getInstance().getDrawableAsset("categories_large/"+c+".png"));

              }
              return v;
        }


    }
}
