package com.frodo.travigator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frodo.travigator.R;
import com.frodo.travigator.activities.MainActivity;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final ArrayList<String> values;

  public MyAdapter(Context context, ArrayList<String> values) {
    super(context, R.layout.adlayout, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.adlayout, parent, false);
    TextView textView = (TextView) rowView.findViewById(R.id.listLabel);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.listImage);

	textView.setText(values.get(position));
	switch ( MainActivity.stateList.get(position) ) {
		case 0: imageView.setImageResource(R.drawable.neutral);
			break;
		case -1: imageView.setImageResource(R.drawable.passed);
			break;
		case 1: imageView.setImageResource(R.drawable.next);
			break;
		case 2:
//            if(MainActivity.currRes == R.drawable.neutral) {
                imageView.setImageResource(R.drawable.next);
 /*               MainActivity.currRes = R.drawable.next;
            }
            else {
                imageView.setImageResource(R.drawable.neutral);
                MainActivity.currRes = R.drawable.neutral;
            }
   */ //        new Blink(imageView).execute();
			break;
		case 10: imageView.setImageResource(R.drawable.dest2);
			break;
		default: break;
	}
    return rowView;
  }
} 