package org.cjc.mydives.divetracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "divetrackerdata";
	
	public static final int DATABASE_VERSION = 2;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		UserTable.onCreate(database);
		CertificationTable.onCreate(database);
		DiveTable.onCreate(database);
		EquipmentTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		UserTable.onUpdate(database, oldVersion, newVersion);
		CertificationTable.onUpdate(database, oldVersion, newVersion);
		DiveTable.onUpdate(database, oldVersion, newVersion);
		EquipmentTable.onUpdate(database, oldVersion, newVersion);
	}
}
