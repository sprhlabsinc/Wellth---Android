package com.wellth.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.onesignal.OneSignal;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wellth.Controller.MySpannable;
import com.wellth.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class AppConfig {
	public static String API_URL = "https://www.back4app.com/";

	public static int tabbar_height = 100;

	public static String namePattern = "^[\\p{L} .'-]+$";
	public static String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
	public static String phonePattern = "^[+]?[0-9]{8,20}$";
	public static String passwordPattern = "^([a-zA-Z0-9!@#$%&*]{6,20})$";

	public static String EMAIL_ERROR_MESSAGE = "Please input correct email address!";
	public static String PASSWORD_ERROR_MESSAGE = "Password should have 6 characters at least!";
	public static String PASSWORD_MATCH_ERROR_MESSAGE = "Password doesn't match.";
	public static String USERNAME_ERROR_MESSAGE = "Please input full name!";
	public static String AGE_ERROR_MESSAGE = "Age should less than 100.";

	public static String PARSE_URL_PATH = "https://parsefiles.back4app.com/Lgk1swICG2ohVVoXJilOskFPTrLlLHe4rOrQQ9KN";

	public static int GET_MAX_POST_COUNT = 10;
	public static int MAX_PAGE_COUNT = 100;
	public static int SCROLLING_DELAY_TIME = 100;
	public static UserInfo userInfo = new UserInfo();
	public static String token = "";
	public static ParseObject selectPost = null;
	public static ParseObject updateObject = null;
	public static ParseObject createObject = null;

	public static String parseDateToddMMyyyy(String time) {
		String inputPattern = "yyyy-MM-dd";
		String outputPattern = "MMM dd, yyyy";
		SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
		SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

		Date date = null;
		String str = null;

		try {
			date = inputFormat.parse(time);
			str = outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
	}

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

	public static boolean hasString(List<String> list, String search) {
		if (list == null) { return false; }
		for (int i = 0; i < list.size(); i ++) {
			String temp = list.get(i);
			if (temp.equals(search)) {
				return true;
			}
		}
		return false;
	}

	// time ago
	public static Date getConvertedLocalTime(String time) {
		SimpleDateFormat inputFormat = new SimpleDateFormat
				("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		SimpleDateFormat outputFormat =
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Adjust locale and zone appropriately
		Date date = null;
		try {
			date = inputFormat.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String outputText = outputFormat.format(date);
		System.out.println(outputText);
		return date;
	}

	public static Date currentDate() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

	public static String getTimeAgo(Date date, Context ctx) {

		if(date == null) {
			return null;
		}

		long time = date.getTime();

		Date curDate = currentDate();
		long now = curDate.getTime();
		if (time > now || time <= 0) {
			return null;
		}

		int dim = getTimeDistanceInMinutes(time);

		String timeAgo = null;

		if (dim == 0) {
			timeAgo = ctx.getResources().getString(R.string.date_util_term_less) + " " +  ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_minute);
		} else if (dim == 1) {
			return "1 " + ctx.getResources().getString(R.string.date_util_unit_minute);
		} else if (dim >= 2 && dim <= 44) {
			timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
		} else if (dim >= 45 && dim <= 89) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " "+ctx.getResources().getString(R.string.date_util_term_an)+ " " + ctx.getResources().getString(R.string.date_util_unit_hour);
		} else if (dim >= 90 && dim <= 1439) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 60)) + " " + ctx.getResources().getString(R.string.date_util_unit_hours);
		} else if (dim >= 1440 && dim <= 2519) {
			timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_day);
		} else if (dim >= 2520 && dim <= 43199) {
			timeAgo = (Math.round(dim / 1440)) + " " + ctx.getResources().getString(R.string.date_util_unit_days);
		} else if (dim >= 43200 && dim <= 86399) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " "+ctx.getResources().getString(R.string.date_util_term_a)+ " " + ctx.getResources().getString(R.string.date_util_unit_month);
		} else if (dim >= 86400 && dim <= 525599) {
			timeAgo = (Math.round(dim / 43200)) + " " + ctx.getResources().getString(R.string.date_util_unit_months);
		} else if (dim >= 525600 && dim <= 655199) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " "+ctx.getResources().getString(R.string.date_util_term_a)+ " " + ctx.getResources().getString(R.string.date_util_unit_year);
		} else if (dim >= 655200 && dim <= 914399) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_over) + " "+ctx.getResources().getString(R.string.date_util_term_a)+ " " + ctx.getResources().getString(R.string.date_util_unit_year);
		} else if (dim >= 914400 && dim <= 1051199) {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_almost) + " 2 " + ctx.getResources().getString(R.string.date_util_unit_years);
		} else {
			timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 525600)) + " " + ctx.getResources().getString(R.string.date_util_unit_years);
		}

		return timeAgo + " " + ctx.getResources().getString(R.string.date_util_suffix);
	}

	private static int getTimeDistanceInMinutes(long time) {
		long timeDistance = currentDate().getTime() - time;
		return Math.round((Math.abs(timeDistance) / 1000) / 60);
	}

	public static void sendPush(String toUser, String message) {
		List<String> toUsers = new ArrayList<String>();
		toUsers.add(toUser);
		JSONObject payload = new JSONObject();
		ParseUser user = ParseUser.getCurrentUser();
		try {
			JSONArray jsArray = new JSONArray(toUsers);
			payload.put("include_player_ids", jsArray);
			JSONObject data = new JSONObject();
			data.put("name", user.getString("fullname"));
			data.put("uid", AppConfig.token);
			data.put("type", "notification");

			JSONObject contents = new JSONObject();
			contents.put("en", message);
			payload.put("contents", contents);
			payload.put("content-available", 1);
			payload.put("data", data);
			payload.put("ios_badgeType", "Increase");
			payload.put("ios_badgeCount", 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OneSignal.postNotification(payload, new OneSignal.PostNotificationResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				Log.d("Post Push", "success");
			}

			@Override
			public void onFailure(JSONObject response) {
				Log.d("Post Push", "fail");
			}
		});
	}

	public static void sendPushFriends(ArrayList<String> toUsers, String message) {
		JSONObject payload = new JSONObject();
		ParseUser user = ParseUser.getCurrentUser();
		try {
			JSONArray jsArray = new JSONArray(toUsers);
			payload.put("include_player_ids", jsArray);
			JSONObject data = new JSONObject();
			data.put("name", user.getString("fullname"));
			data.put("uid", AppConfig.token);
			data.put("type", "notification");

			JSONObject contents = new JSONObject();
			contents.put("en", message);
			payload.put("contents", contents);
			payload.put("content-available", 1);
			payload.put("data", data);
			payload.put("ios_badgeType", "Increase");
			payload.put("ios_badgeCount", 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OneSignal.postNotification(payload, new OneSignal.PostNotificationResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				Log.d("Post Push", "success");
			}

			@Override
			public void onFailure(JSONObject response) {
				Log.d("Post Push", "fail");
			}
		});
	}

	public static void getProfileInfo() {
		ParseUser user = ParseUser.getCurrentUser();

		userInfo.username = user.getString("fullname");
		ParseFile photoFile = user.getParseFile("photo");
		userInfo.country = user.getString("country");
		userInfo.age = user.getInt("age");
		userInfo.photoUrl = user.getString("photoUrl");
		userInfo.type = user.getString("type");
		userInfo.token = token;
		userInfo.email = user.getEmail();

		if (user.getList("healthissue") == null) {
			userInfo.issueList = new ArrayList<String>();
		}
		else {
			userInfo.issueList = user.getList("healthissue");
		}

		userInfo.photo = null;
		if (photoFile != null) {
			try {
				userInfo.photo = photoFile.getFile();
			} catch (com.parse.ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

		if (tv.getTag() == null) {
			tv.setTag(tv.getText());
		}
		ViewTreeObserver vto = tv.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				ViewTreeObserver obs = tv.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
				if (maxLine == 0) {
					int lineEndIndex = tv.getLayout().getLineEnd(0);
					String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
					tv.setText(text);
					tv.setMovementMethod(LinkMovementMethod.getInstance());
					tv.setText(
							addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
									viewMore), TextView.BufferType.SPANNABLE);
				} else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
					int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
					String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
					tv.setText(text);
					tv.setMovementMethod(LinkMovementMethod.getInstance());
					tv.setText(
							addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
									viewMore), TextView.BufferType.SPANNABLE);
				} else {
					int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
					String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
					tv.setText(text);
					tv.setMovementMethod(LinkMovementMethod.getInstance());
					tv.setText(
							addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
									viewMore), TextView.BufferType.SPANNABLE);
				}
			}
		});

	}

	private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
																			final int maxLine, final String spanableText, final boolean viewMore) {
		String str = strSpanned.toString();
		SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

		if (str.contains(spanableText)) {

			ssb.setSpan(new MySpannable(false){
				@Override
				public void onClick(View widget) {
					if (viewMore) {
						tv.setLayoutParams(tv.getLayoutParams());
						tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
						tv.invalidate();
						makeTextViewResizable(tv, -1, "See Less", false);
					} else {
						tv.setLayoutParams(tv.getLayoutParams());
						tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
						tv.invalidate();
						makeTextViewResizable(tv, 3, ".. See More", true);
					}
				}
			}, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

		}
		return ssb;

	}
}
