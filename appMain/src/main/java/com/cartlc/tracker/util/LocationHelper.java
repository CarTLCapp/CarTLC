package com.cartlc.tracker.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.cartlc.tracker.app.TBApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by dug on 3/12/18.
 */

public class LocationHelper {

    static final String TAG = LocationHelper.class.getSimpleName();
    static final Boolean LOG = true;
    static final String DEFAULT_LOCATION_HACK = "Chicago, IL";

    class GetAddressTask extends AsyncTask<Location, Void, Address>
    {
        static final String GOOGLE_LOC = "http://maps.googleapis.com/maps/api/geocode/json?";

        OnLocationCallback mCallback;
        Address mAddress;

        GetAddressTask(OnLocationCallback callback) {
            mCallback = callback;
        }

        @Override
        protected Address doInBackground(Location... locs)
        {
            Location loc = locs[0];
            Address address = getAddress1(loc);
            if (address == null) {
                address = getAddress2(loc);
            }
            return address;
        }

        Address getAddress1(Location location)
        {
            try {
                List<Address> addressList = mGeocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                if (addressList != null && addressList.size() > 0) {
                    return addressList.get(0);
                }
            } catch (Exception ex) {
                Log.e(TAG, "GEOCODER:" + ex.getMessage());
            }
            return null;
        }

        Address getAddress2(Location location)
        {
            List<Address> list = getFromLocation(location.getLatitude(), location.getLongitude(),
                    1);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list.get(0);
        }

        List<Address> getFromLocation(double lat, double lng, int maxResult)
        {
            Uri buildUri = Uri.parse(GOOGLE_LOC).buildUpon().appendQueryParameter("latlng",
                    String.valueOf(lat) + "," + String.valueOf(lng)).appendQueryParameter
                    ("sensor", "true").appendQueryParameter("language", Locale.ENGLISH
                    .getLanguage()).build();
            HttpURLConnection urlConnection = null;
            StringBuilder stringBuilder = new StringBuilder();
            List<Address> retList = null;

            if (LOG) {
                Log.i(TAG, "doing query from google");
            }
            try {
                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
                JSONObject jsonObject = new JSONObject();
                /*
                 * TODO: STORE the returned string in our database, with a lookup key of the
				 * latitude and
				 * longitude. We can do a lookup if we hit this multiple times. The reason is that
				  * there
				 * is a limit to the number of times you can hit Google's geocacher.
				 */
                jsonObject = new JSONObject(stringBuilder.toString());

                retList = new ArrayList<Address>();
                String status = jsonObject.getString("status");
                if ("OK".equalsIgnoreCase(status)) {
                    JSONArray results = jsonObject.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String indiStr = result.getString("formatted_address");
                        Address addr = new Address(Locale.getDefault());
                        addr.setAddressLine(0, indiStr);
                        JSONArray components = result.getJSONArray("address_components");
                        for (int j = 0; j < components.length(); j++) {
                            JSONObject component = components.getJSONObject(j);
                            JSONArray types = component.getJSONArray("types");

                            if (types.length() == 0) {
                                continue;
                            }
                            String type = types.getString(0);
                            // String long_name = component.getString("long_name");
                            String short_name = component.getString("short_name");

                            if ("street_number".equals(type)) {
                                addr.setThoroughfare(short_name);
                            } else if ("route".equals(type)) {
                                addr.setFeatureName(short_name);
                            } else if ("locality".equals(type)) {
                                addr.setLocality(short_name);
                            } else if ("administrative_area_level_2".equals(type)) {
                                addr.setSubAdminArea(short_name);
                            } else if ("administrative_area_level_1".equals(type)) {
                                addr.setAdminArea(short_name);
                            } else if ("country".equals(type)) {
                                addr.setCountryName(short_name);
                            } else if ("postal_code".equals(type)) {
                                addr.setPostalCode(short_name);
                            } else if ("neighborhood".equals(type)) {
                                addr.setSubThoroughfare(short_name);
                            }
                        }
                        retList.add(addr);
                    }
                } else {
                    Log.e(TAG, "google geocode returned: " + status);
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocode parsing error: IOException: ", e);
            } catch (JSONException e) {
                Log.e(TAG, "TAG,Geocode parsing error: JSONException: ", e);
            } catch (Exception e) {
                Log.e(TAG, "Geocode parsing error for address " + buildUri + ", ERROR: ", e);
            }
            return retList;
        }

        @Override
        protected void onPostExecute(Address result)
        {
            mAddress = result;

            if (mAddress != null) {
                if (LOG) {
                    Log.i(TAG, "Address=" + mAddress.toString());
                }
                invokeCallback();
            } else if (LOG) {
                Log.e(TAG, "NO ADDRESS");
            }
        }

        void invokeCallback()
        {
            if (mAddress != null && mCallback != null) {
                mCallback.onLocationUpdate(getLocationString());
            }
        }

        public String getLocationString()
        {
            if (mAddress != null) {
                String result = getLocation(mLocationSelector);

                if (LOG) {
                    Log.i(TAG, "LOCATION " + mLocationSelector + "=" + mLocationSelector);
                }
                return result;
            }
            return DEFAULT_LOCATION_HACK;
        }


        String getLocation(int selector)
        {
            if (mAddress == null) {
                return null;
            }
            switch (selector) {
                case 1:
                    return mAddress.getAddressLine(1);
                case 2:
                    return mAddress.getSubThoroughfare();
                case 3:
                    return mAddress.getFeatureName();
                case 4:
                    return mAddress.getSubAdminArea();
                case 5:
                    return mAddress.getSubLocality();
                case 6:
                    return mAddress.getPremises();
                case 7:
                    return mAddress.getLocality();
                case 8:
                    return mAddress.getThoroughfare();
                case 9:
                    return mAddress.getAdminArea();
                case 10:
                    return mAddress.getCountryName();
            }
            return null;
        }


    }

    public interface OnLocationCallback
    {
        void onLocationUpdate(String location);
    }

    static LocationHelper sInstance;

    public static LocationHelper getInstance() {
        return sInstance;
    }

    public static void Init(TBApplication app) {
        new LocationHelper(app);
    }

    TBApplication mApp;
    final Geocoder mGeocoder;
    int mLocationSelector;

    LocationHelper(TBApplication app)
    {
        sInstance = this;
        mApp = app;
        mGeocoder = new Geocoder(mApp);
        mLocationSelector = 1;
    }

    public void requestAddress(Location location, OnLocationCallback callback)
    {
        GetAddressTask task = new GetAddressTask(callback);
        task.execute(location);
    }

}
