package com.cartlc.tracker.server;

import android.net.Uri;

import com.cartlc.tracker.data.TableZipCode;
import com.cartlc.tracker.data.DataZipCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by dug on 8/24/17.
 */

public class DCZip extends DCPost {

    static final String AUTHORITY = "http://maps.googleapis.com";

    enum ObjectType {
        IGNORED,
        CITY,
        STATE,
        ZIPCODE;

        public static ObjectType from(JSONArray types) throws JSONException {
            for (int i = 0; i < types.length(); i++) {
                if ("locality".equals(types.getString(i))) {
                    return STATE;
                }
                if ("administrative_area_level_1".equals(types.getString(i))) {
                    return CITY;
                }
                if ("postal_code".equals(types.getString(i))) {
                    return ZIPCODE;
                }
            }
            return IGNORED;
        }
    }

    public DCZip() {
        super();
    }

    public void findZipCode(String zipcode) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(AUTHORITY)
                    .appendPath("maps")
                    .appendPath("api")
                    .appendPath("geocode")
                    .appendPath("json")
                    .appendQueryParameter("address", zipcode)
                    .appendQueryParameter("sensor", "true");
            URL url = new URL(builder.build().toString());
            String result = post(url);
            if (result == null) {
                return;
            }
            DataZipCode data = new DataZipCode();
            JSONObject root = new JSONObject(result);
            JSONObject results = root.getJSONObject("results");
            JSONArray components = results.getJSONArray("address_components");
            for (int i = 0; i < components.length(); i++) {
                JSONObject ele = components.getJSONObject(i);
                JSONArray types = ele.getJSONArray("types");
                ObjectType objType = ObjectType.from(types);
                if (objType == ObjectType.CITY) {
                    data.city = ele.getString("long_name");
                } else if (objType == ObjectType.STATE) {
                    data.stateLongName = ele.getString("long_name");
                    data.stateShortName = ele.getString("short_name");
                } else if (objType == ObjectType.ZIPCODE) {
                    data.zipCode = ele.getString("long_name");
                }
            }
            if (data.isValid()) {
                TableZipCode.getInstance().add(data);
                EventBus.getDefault().post(data);
            } else {
                Timber.e("Invalid zipcode response: " + result);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    String post(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String result = getResult(connection);
        connection.disconnect();
        return result;
    }
}
