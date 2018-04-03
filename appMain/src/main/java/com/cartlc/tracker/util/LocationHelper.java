package com.cartlc.tracker.util;

import android.Manifest;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.TableCrash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Find the location the user is at and return the best street address we can find.
 * Created by dug on 3/12/18.
 */
public class LocationHelper {

    static final String TAG = LocationHelper.class.getSimpleName();
    static final Boolean LOG = true;

    public interface OnLocationCallback
    {
        void onLocationUpdate(Address address);
    }

    class GetAddressTask extends AsyncTask<Location, Void, Address> {
        static final String GOOGLE_LOC = "http://maps.googleapis.com/maps/api/geocode/json?";

        OnLocationCallback mCallback;

        GetAddressTask(OnLocationCallback callback) {
            mCallback = callback;
        }

        @Override
        protected Address doInBackground(Location... locs) {
            Location loc = locs[0];
            Address address;
            address = getAddressFromDatabase(loc);
            if (address != null) {
                return address;
            }
            address = getAddressFromGeocoder(loc);
            if (address != null) {
                return storeAddressToDatabase(loc, address);
            }
            return storeAddressToDatabase(loc, getAddressFromNetwork(loc));
        }

        /**
         * Lookup the address in internal database.
         * The reason is that there is a limit to the number of times you can hit Google's geocacher.
         */
        Address getAddressFromDatabase(Location location) {
            return null;
        }

        Address storeAddressToDatabase(Location location, Address address) {
            if (address == null) {
                return null;
            }
            return address;
        }

        Address getAddressFromGeocoder(Location location) {
            try {
                List<Address> addressList = mGeocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (addressList != null && addressList.size() > 0) {
                    return addressList.get(0);
                }
            } catch (Exception ex) {
                Log.e(TAG, "GEOCODER:" + ex.getMessage());
            }
            return null;
        }

        Address getAddressFromNetwork(Location location) {
            List<Address> list = getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list.get(0);
        }

        List<Address> getFromLocation(double lat, double lng, int maxResult) {
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
        protected void onPostExecute(Address address) {
            if (address != null) {
                if (LOG) {
                    Log.i(TAG, "Got address=" + address.toString());
                }
                if (mCallback != null) {
                    mCallback.onLocationUpdate(address);
                    infoReport(address);
                }
            }
        }
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
    FusedLocationProviderClient mFusedLocationClient;
    HashSet<Long> mReportMap = new HashSet();

    LocationHelper(TBApplication app)
    {
        sInstance = this;
        mApp = app;
        mGeocoder = new Geocoder(mApp);
        mLocationSelector = 1;
    }

    void requestAddress(Location location, OnLocationCallback callback)
    {
        GetAddressTask task = new GetAddressTask(callback);
        task.execute(location);
    }

    public void requestLocation(final Activity act, final OnLocationCallback callback) {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(act);
        }
        mApp.checkPermissions(act, new PermissionHelper.PermissionListener() {
            @Override
            public void onGranted(String permission) {
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                    getLocation(act, callback);
                }
            }

            @Override
            public void onDenied(String permission) {
            }
        });
    }

    public void onDestroy() {
        mFusedLocationClient = null;
    }

    void getLocation(Activity act, final OnLocationCallback callback) throws SecurityException {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(act, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        requestAddress(location, callback);
                    }
                });
    }

    public static String matchState(Address address, List<String> states) {
        return match(address.getAdminArea(), states);
    }

    public static String matchCity(Address address, List<String> cities) {
        return match(address.getLocality(), cities);
    }

    public static String matchStreet(Address address, List<String> streets) {
        List<String> thoroughFares = new ArrayList<>();
        for (String street : streets) {
            if (street.contains(address.getThoroughfare())) {
                thoroughFares.add(street);
            }
            if (street.contains(address.getFeatureName())) {
                return street;
            }
        }
        if (thoroughFares.size() == 1) {
            return thoroughFares.get(0);
        }
        return null;
    }

    static String match(String match, List<String> items) {
        if (hasMatch(items, match)) {
            return match;
        }
        return null;
    }

    static boolean hasMatch(List<String> items, String match) {
        for (String item : items) {
            if (item.compareToIgnoreCase(match) == 0) {
                return true;
            }
        }
        return false;
    }

    public void infoReport(Address address) {
        if (TBApplication.REPORT_LOCATION) {
            long code = address.getAdminArea().hashCode();
            code += address.getLocality().hashCode();
            if (!mReportMap.contains(code)) {
                mReportMap.add(code);
                TableCrash.getInstance().info("LOCATION: " + address.toString());
            }
            Log.i(TAG, address.toString());
        }
    }
}
