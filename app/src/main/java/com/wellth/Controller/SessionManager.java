package com.wellth.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "Wellth";
	
	private static final String NOTIFICATION = "NOTIFICATION";
	private static final String FIRST_TIME = "FIRST_TIME";

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void setNotificationSetting(boolean notification) {
		editor.putBoolean(NOTIFICATION, notification);
		editor.commit();
	}
	public boolean getNotificationSetting(){
		return pref.getBoolean(NOTIFICATION, true);
	}

	public void setFirstTime(boolean firstTime) {
		editor.putBoolean(FIRST_TIME, firstTime);
		editor.commit();
	}
	public boolean getFirstTime(){
		return pref.getBoolean(FIRST_TIME, false);
	}
}
