package com.jimi.smartbrt;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDirectionActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {

    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private LatLng camera = new LatLng(6.443241, 3.403703);
    private LatLng origin = new LatLng(6.443241, 3.403723);
    private LatLng destination = new LatLng(6.620475, 3.503311);
    private LatLng bus = new LatLng(6.463994, 3.380320);
    private static Map<String, LatLng> busStops;
    String reponse = "";
    JSONObject responseObject = null;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);
        busStops = new HashMap<>();

        busStops.put("Mile 12", new LatLng(6.605991, 3.399722));
        busStops.put("Maryland", new LatLng(6.571929, 3.367439));
        busStops.put("Palmgrove", new LatLng(6.541583, 3.366970));
        busStops.put("Stadium", new LatLng(6.501366, 3.362703));
        busStops.put("TBS", new LatLng(6.445863, 3.400579));


        btnRequestDirection = (Button) findViewById(R.id.btn_request_direction);
        assert btnRequestDirection != null;
        btnRequestDirection.setOnClickListener(this);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    private void addMarkers() {
        for (HashMap.Entry<String, LatLng> map : busStops.entrySet()) {
            String busStopName = map.getKey();
            LatLng coord = map.getValue();
            Log.e("TAG", "coord is " + coord + "busStopName is " + busStopName);
            googleMap.addMarker(new MarkerOptions().position(new LatLng(coord.latitude, coord.longitude)).title(busStopName));
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(camera, 12));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(6.463994, 3.380320)).title("Bus 1 " + count +" Passengers. Arrives in 15min")).showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bus, 15));

    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            Log.e("TAG", "route size is " + direction.getRouteList().size());
            List<Step> stepList = direction.getRouteList().get(1).getLegList().get(0).getStepList();


            for (Step step : stepList) {
                Log.e("TAG", "distance is " + step.getDistance().getValue());
                Log.e("TAG", "duration is " + step.getDuration().getValue());
            }
            ArrayList<LatLng> sectionPositionList = direction.getRouteList().get(1).getLegList().get(0).getSectionPoint();
            for (LatLng position : sectionPositionList) {
                googleMap.addMarker(new MarkerOptions().position(position));
            }
            ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(this, stepList, 5, Color.RED, 3, Color.BLUE);
            for (PolylineOptions polylineOption : polylineOptionList) {
                googleMap.addPolyline(polylineOption);
            }

//            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(1).getLegList().get(0).getDirectionPoint();
//            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));


            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 18));
            btnRequestDirection.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_request_direction) {
//            requestDirection();
            updateCount();

        }

    }

    private void updateCount() {
        new MapAsyncTask().execute("http://192.168.1.1/json");
    }

    public void requestDirection() {
        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(getString(R.string.google_maps_key))
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 13));
    }


    public class MapAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            int resCode = -1;
            String response = "";

            for (String aUrl : params){
                try {
                    URL url = new URL(aUrl);
                    URLConnection urlConn = url.openConnection();
                    if (!(urlConn instanceof HttpURLConnection)) {
                        throw new IOException("URL is not an Http URL");
                    }

                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConn;
                    httpURLConnection.setAllowUserInteraction(false);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    resCode = httpURLConnection.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = httpURLConnection.getInputStream();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (in != null) {
                    try {
                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        String inputStr;
                        while ((inputStr = streamReader.readLine()) != null) {
                            response += inputStr;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String stringOuput) {
            Log.e("TAG", "output " + stringOuput);
            reponse = stringOuput;

            try {
                responseObject = new JSONObject(reponse);
                count = responseObject.getInt("count");
                Log.e("TAG", "count is " + count);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            addMarkers();
        }
    }
}
