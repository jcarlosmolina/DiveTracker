package org.cjc.mydives.divetracker;

import org.cjc.mydives.divetracker.db.DiveDbAdapter;
import org.cjc.mydives.divetracker.db.FormatterHelper;
import org.cjc.mydives.divetracker.entity.Dive;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class DiveDetailsActivity extends MapActivity {
	private DiveDbAdapter diveDbAdapter = new DiveDbAdapter(this);

	Dive dive = new Dive();

	private TextView tvTitle;

	private TextView tvDate;
	private TextView tvTime;
	private TextView tvDepth;
	private TextView tvDuration;
	
	private ImageView ivEdit;
	private ImageView ivAdd;
	
	private MapView mvMap;
	MapHelper mapHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dive_details);

		dive.set_id(getIntent().getExtras().getInt("_ID"));

		tvTitle = (TextView) findViewById(R.id.header_title);
		tvDate = (TextView) this.findViewById(R.id.dive_details_date);
		tvTime = (TextView) this.findViewById(R.id.dive_details_time);
		tvDepth = (TextView) this.findViewById(R.id.dive_details_maxDeep);
		tvDuration = (TextView) this.findViewById(R.id.dive_details_duration);
		ivEdit = (ImageView) this.findViewById(R.id.header_button_edit);
		ivAdd  = (ImageView) this.findViewById(R.id.header_button_add);
		mvMap = (MapView) findViewById(R.id.dive_map);
		mapHelper = new MapHelper(mvMap, getResources());

		// Title
		tvTitle.setText(R.string.dive_details_title);
		// Buttons
		ivAdd.setVisibility(View.GONE);
		ivEdit.setVisibility(View.VISIBLE);

		addListeners();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void addListeners() {
		// Edit Dive OnClick
		ivEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// Edit the new dive
				Intent i = new Intent(getBaseContext(), DiveEditActivity.class);
				i.putExtra("_ID", dive.get_id());
				startActivity(i);
			}
		});
	}
	
	private void populateFields() {

		// Get the data
		diveDbAdapter.open();	// Open the DB
		Cursor cursor = diveDbAdapter.fetchById(dive.get_id());		
		if (cursor.moveToFirst()) {
			diveDbAdapter.loadInstance(cursor, dive);

			tvTitle.setText(dive.getName());
			tvDate.setText(FormatterHelper.formatDate(dive.getTimeIn()));
			tvTime.setText(FormatterHelper.formatTime(dive.getTimeIn()));

			// Max Deep
			double depth = dive.getDepth();
			if (depth > 0.0f) {
				tvDepth.setText(String.valueOf(depth));				
			}
			
			// Duration
			if (dive.getDuration() > 0) {
				tvDuration.setText(FormatterHelper.formatTime(dive.getDuration()));
			}
		}
		
		// GPS
		mapHelper.setMapPosition(dive.getLatitude(), dive.getLongitude());

		// Close the DB
		cursor.close();
		diveDbAdapter.close();
	}

	@Override
	public void onResume() {
		populateFields();
		super.onResume();
	}	
}
