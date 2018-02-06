package com.wellth.Controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wellth.Model.AppConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class NetworkManager {

    public interface NetworkListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onResult(JSONObject result);
        public void onFail(String error);
    }

    private NetworkListener listener;
    private static final String TAG = "NETWORK";
    private Context context;
    private SessionManager session;

    // Constructor where listener events are ignored
    public NetworkManager(Context context, int requestType, String tag, final Map<String, String> params) {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
        this.context = context;
        this.session = new SessionManager(context);
        sendRequest(requestType, tag, params);
    }
    // Assign the listener implementing events interface that will receive the events
    public void setNetworkListener(NetworkListener listener) {
        this.listener = listener;
    }

    private void sendRequest(int requestType, String tag, final Map<String, String> params){
        // Tag used to cancel the request
        String url = AppConfig.API_URL + tag;
        if(requestType == Request.Method.GET && params != null){
            url = url + "?";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url = url + entry.getKey() + "=" + entry.getValue() + "&";
            }
        }
        AppController.getInstance().getRequestQueue().getCache().clear();
        StringRequest strReq = new StringRequest(requestType,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);

                try {
                    Object json = new JSONTokener(response).nextValue();
                    if (json instanceof JSONObject){
                        JSONObject jObj = new JSONObject(response);
                        listener.onResult(jObj);
                    }
                    else if (json instanceof JSONArray){
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jObj = new JSONObject();
                        jObj.put("result", jsonArray);
                        listener.onResult(jObj);
                    }else{
                        listener.onFail("Fail to call server!");
                    }

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.e(TAG,  e.getMessage());
                    Toast.makeText(context, "Fail, Please retry. ", Toast.LENGTH_LONG).show();
                    listener.onFail(e.getMessage());
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                if (errorMessage != null) {
                    Log.e(TAG,  error.getMessage());
                    Toast.makeText(context,
                            error.getMessage(), Toast.LENGTH_LONG).show();
                } else{
                    errorMessage = "Error 404";
                }
                listener.onFail(errorMessage);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders(){
                HashMap<String, String> map = new HashMap<String, String>();
                //map.put("X-Device-Info","Android FOO BAR");
                //map.put("Content-Type", "application/json; charset=UTF-8");
                //map.put("Authorization1", session.getAPIKey());
                return map;
            }

        };

        strReq.setShouldCache(false);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag);
    }
}
