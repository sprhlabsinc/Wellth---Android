package com.wellth.Controller;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Activity.MainActivity;

import org.json.JSONObject;

import tgio.parselivequery.LiveQueryClient;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;

	private static AppController mInstance;
	public static boolean isBackground = false;

	private static GoogleAnalytics sAnalytics;
	private static Tracker sTracker;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Montserrat-Regular.otf")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);

		Parse.initialize(new Parse.Configuration.Builder(this)
				.applicationId(getResources().getString(R.string.parse_app_id))
				.clientKey(getResources().getString(R.string.parse_client_key))
				.server(getResources().getString(R.string.parse_server_url)).build()
		);
		LiveQueryClient.init(getResources().getString(R.string.parse_server_live_url), getResources().getString(R.string.parse_app_id), true);
		LiveQueryClient.connect();

		OneSignal.startInit(this)
				.setNotificationOpenedHandler(new AppNotificationOpenedHandler())
				.init();
		OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
			@Override
			public void idsAvailable(String userId, String registrationId) {
				Log.d("debug", "User:" + userId);
				if (registrationId != null) {
					Log.d("debug", "registrationId:" + registrationId);
					AppConfig.token = userId;
				}

			}
		});

		sAnalytics = GoogleAnalytics.getInstance(this);
	}

	synchronized public Tracker getDefaultTracker() {
		// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
		if (sTracker == null) {
			sTracker = sAnalytics.newTracker(R.xml.global_tracker);
		}

		return sTracker;
	}

	private class AppNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
		@Override
		public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
			try {
				if (additionalData != null) {
					if (additionalData.has("actionSelected"))
						Log.d("OneSignalExample", "OneSignal notification button with id " + additionalData.getString("actionSelected") + " pressed");

					Log.d("OneSignalExample", "Full additionalData:\n" + additionalData.toString());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

			// The following can be used to open an Activity of your choose.
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("message", message);
			startActivity(intent);

			LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
			Intent intent1 = new Intent();
			intent1.setAction(getResources().getString(R.string.click_notification));
			intent1.putExtra("message", message);
			mBroadcaster.sendBroadcast(intent1);
			// Follow the insturctions in the link below to prevent the launcher Activity from starting.
			// https://documentation.onesignal.com/docs/android-notification-customizations#changing-the-open-action-of-a-notification
		}
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
}