package org.cjc.mydives.divetracker;

import java.util.Calendar;

import org.cjc.mydives.divetracker.db.DiveDbAdapter;
import org.cjc.mydives.divetracker.db.FormatterHelper;
import org.cjc.mydives.divetracker.entity.Dive;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class DiveEditActivity extends MapActivity {
	private static final int DATE_DIALOG_ID  = 0;
	private static final int TIME_IN_DIALOG_ID  = 1;
	private static final int TIME_OUT_DIALOG_ID = 2;
	
	TimePicker timePicker;
	DatePicker datePicker;
	
	private DiveDbAdapter diveDbAdapter = new DiveDbAdapter(this);

	// CONTROLS
	private EditText etName;
	private Button btnDate;
	private Button btnTimeIn;
	private Button btnTimeOut;
	private EditText etDepth;
	private Button btnSave;
	private MapView mvMap;
	private MapHelper mapHelper;

	private Dive dive = new Dive(); // The dive being edited;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dive_edit);
		
		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("_ID")) {
			dive.set_id(getIntent().getExtras().getInt("_ID"));
		} else {
			dive.set_id(-1);
		}

		// Title
		((TextView) findViewById(R.id.header_title)).setText(R.string.dive_edit_title);
		
		// Get the controls
		etName = (EditText) findViewById(R.id.dive_edit_field_name);
		btnDate = (Button) findViewById(R.id.dive_edit_date);
		btnTimeIn = (Button) findViewById(R.id.dive_edit_timeIn);
		btnTimeOut = (Button) findViewById(R.id.dive_edit_timeOut);
		etDepth   = (EditText) findViewById(R.id.dive_edit_field_deep);
		btnSave = (Button) findViewById(R.id.btn_save);
		mvMap = (MapView) findViewById(R.id.dive_map);
		mapHelper = new MapHelper(mvMap, getResources());
		
		mvMap.setBuiltInZoomControls(true);
		mvMap.setClickable(true);
		
		// GPS if we are creating...
		if (dive.get_id() == -1) {
			enableGPS();
		}

		addListeners();
		populateFields();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar date = Calendar.getInstance();
		switch (id) {
		case TIME_IN_DIALOG_ID:
			date.setTimeInMillis(dive.getTimeIn());
			return new TimePickerDialog(this, timeInSetListener, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true);
		case TIME_OUT_DIALOG_ID:
			date.setTimeInMillis(dive.getTimeOut());
			return new TimePickerDialog(this, timeOutSetListener, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true);
		case DATE_DIALOG_ID:
			date.setTimeInMillis(dive.getTimeIn());
			return new DatePickerDialog(
					this, dateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	private TimePickerDialog.OnTimeSetListener timeInSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			Calendar dateIn = Calendar.getInstance();
			dateIn.setTimeInMillis(dive.getTimeIn());
			dateIn.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dateIn.set(Calendar.MINUTE, minuteOfHour);
			dive.setTimeIn(dateIn.getTimeInMillis());
			btnTimeIn.setText(FormatterHelper.formatTime(dive.getTimeIn()));
		}
	};
	
	private TimePickerDialog.OnTimeSetListener timeOutSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			Calendar dateOut = Calendar.getInstance();
			dateOut.setTimeInMillis(dive.getTimeOut());
			dateOut.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dateOut.set(Calendar.MINUTE, minuteOfHour);
			dive.setTimeOut(dateOut.getTimeInMillis());
			btnTimeOut.setText(FormatterHelper.formatTime(dive.getTimeOut()));
		}
	};

	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar dateIn = Calendar.getInstance();
			dateIn.setTimeInMillis(dive.getTimeIn());
			dateIn.set(year, monthOfYear, dayOfMonth);
			dive.setTimeIn(dateIn.getTimeInMillis());
			btnDate.setText(FormatterHelper.formatDate(dive.getTimeIn()));
		}
	};
	
	/**
	 * Method used to subscribe to the listeners of the scenario.
	 */
	public void addListeners() {
		// DATE Button
		btnDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		
		// TIME IN Button
		btnTimeIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_IN_DIALOG_ID);
			}
		});

		// TIME OUT Button
		btnTimeOut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dive.getTimeOut() == 0)
					dive.setTimeOut(System.currentTimeMillis());
				showDialog(TIME_OUT_DIALOG_ID);
			}
		});

		// SAVE Button
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
	}

	/**
	 * Method enable and get the GPS position.
	 */
    private void enableGPS() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				dive.setLongitude(location.getLongitude());
				dive.setLatitude(location.getLatitude());
				mapHelper.setMapPosition(dive.getLatitude(), dive.getLongitude());
			}
			
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			
			public void onProviderEnabled(String provider) {}
			
			public void onProviderDisabled(String provider) {}
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);    
   	}
    
	/**
	 * Method used to populate the scenario fields with data.
	 */
    private void populateFields() {
    	if (dive.get_id() == -1) {
    		// Instance creation
    		dive.setTimeIn(System.currentTimeMillis());
    		btnDate.setText(FormatterHelper.formatDate(dive.getTimeIn()));
    		btnTimeIn.setText(FormatterHelper.formatTime(dive.getTimeIn()));
    	}

		// Get the data
		diveDbAdapter.open();	// Open the DB
		Cursor cursor = diveDbAdapter.fetchById(dive.get_id());
		if (cursor.moveToFirst()) {
			diveDbAdapter.loadInstance(cursor, dive);
			if (dive.getName() != null) {
				etName.setText(dive.getName());
			}
			btnDate.setText(FormatterHelper.formatDate(dive.getTimeIn()));
			btnTimeIn.setText(FormatterHelper.formatTime(dive.getTimeIn()));

			if (dive.getTimeOut() > 0) {
				btnTimeOut.setText(FormatterHelper.formatTime(dive.getTimeOut()));
			}

			etDepth.setText(String.valueOf(dive.getDepth()));

			// GPS
			if (dive.getLatitude() != 0.0f && dive.getLongitude() != 0.0f) {
				mapHelper.setMapPosition(dive.getLatitude(), dive.getLongitude());
			}
		}
		
		// Close the DB
		cursor.close();
		diveDbAdapter.close();
	}
    
	/**
	 * This will store the data to the database.
	 */
	private void saveData() {
		dive.setName(etName.getText().toString());

		try {
			dive.setDepth(Integer.valueOf(etDepth.getText().toString()).intValue());
		} catch (NumberFormatException e) {
		}
				
		diveDbAdapter.open();
		if (dive.get_id() != -1) {
			diveDbAdapter.update(dive.get_id(), dive.getName(), dive.getTimeIn(), dive.getTimeOut(), dive.getDepth(), null, null, null, null, dive.getLatitude(), dive.getLongitude());
		} else {
			diveDbAdapter.insert(dive.getName(), dive.getTimeIn(), dive.getTimeOut(), dive.getDepth(), null, null, null, null, dive.getLatitude(), dive.getLongitude());
		}
		diveDbAdapter.close();
	}
}
