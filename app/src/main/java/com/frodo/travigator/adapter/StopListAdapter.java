package com.frodo.travigator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frodo.travigator.R;
import com.frodo.travigator.models.Stop;

/**
 * Created by durgesh on 4/29/16.
 */
public class StopListAdapter extends BaseAdapter {
    private Stop[] stops;
    private LayoutInflater layoutInflater;
    public StopListAdapter(Context context, Stop[]stops) {
        this.stops = stops;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return this.stops.length;
    }

    @Override
    public Object getItem(int position) {
        return this.stops[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = layoutInflater.inflate(R.layout.stops_list_item, null) ;
        TextView name = (TextView)rootView.findViewById(R.id.stop_name);
        name.setText(stops[position].getStop_name());
        return rootView;
    }
}
