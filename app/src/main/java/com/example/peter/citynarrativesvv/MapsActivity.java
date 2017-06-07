package com.example.peter.citynarrativesvv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.peter.citynarrativesvv.POJO.Example;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener {

    private LatLng here;
    private ArrayList<NarrativeLocation> narrativeLocations;
    private  NarrativeLocation visitedLocation;
    private String narrativeTableName;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;

    ArrayList<LatLng> MarkerPoints;
    TextView locationMapsDistance;
    Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        visitedLocation = null;

        //// Used for volley method
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();
        //X//

        here = (LatLng) getIntent().getExtras().get("here");
        narrativeLocations = (ArrayList<NarrativeLocation>) getIntent().getSerializableExtra("narrativeLocations");
        narrativeTableName = (String) getIntent().getExtras().get("narrativeTableName");
        visitedLocation = (NarrativeLocation) getIntent().getExtras().get("visitedLocation");

        MarkerPoints = new ArrayList<>();
        locationMapsDistance = (TextView) findViewById(R.id.locationMapsDistance);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);


        if(visitedLocation != null)// if the user is coming from the visited location activity
        {
            LatLng visitedLatLng = new LatLng(Double.parseDouble(visitedLocation.getLatitude()),Double.parseDouble(visitedLocation.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(visitedLatLng).title(visitedLocation.getTitle()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(visitedLatLng, 16));
            locationMapsDistance.setVisibility(View.GONE);
        }
        else// if the user is coming from the location display activity
        {
            for (NarrativeLocation location : narrativeLocations)
            {
                LatLng tempLatLng = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()));
                mMap.addMarker(new MarkerOptions().position(tempLatLng).title(location.getTitle()));
            }

            mMap.setOnInfoWindowClickListener(this);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 14));

            build_retrofit_and_get_response();
            //getDirection(); //Drawing a path using volley methods, not fullt implemented
        }
    }

    private void build_retrofit_and_get_response()
    {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        for(int i = 1; i < narrativeLocations.size(); i++)
        {
            NarrativeLocation location = narrativeLocations.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()));
            Call<Example> calls = service.getDistanceDuration("metric", here.latitude + "," + here.longitude, latLng.latitude + "," + latLng.longitude, "walking");
            calls.enqueue(new Callback<Example>()
            {
                @Override
                public void onResponse(Response<Example> response, Retrofit retrofit)
                {
                    try {
                        for (int i = 0; i < response.body().getRoutes().size(); i++)
                        {

                            String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                            String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                            locationMapsDistance.setText(narrativeLocations.get(0).getTitle() + "\n" +"Distance: " + distance + "\n"+"Duration: " + time);
                            String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                            List<LatLng> list = decodePoly(encodedString);
                            line = mMap.addPolyline(new PolylineOptions()
                                    .addAll(list)
                                    .width(10)
                                    .color(Color.parseColor("#004c00"))
                                    .geodesic(true)
                            );
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d("onFailure", t.toString());
                }
            });
        }
        // Method repeated for primary destination to add with different colour polyline, could be reduced to an if/else check in the previous for loop
        NarrativeLocation firstLocation = narrativeLocations.get(0);
        LatLng firstLatLng = new LatLng(Double.parseDouble(firstLocation.getLatitude()), Double.parseDouble(firstLocation.getLongitude()));
        Call<Example> call = service.getDistanceDuration("metric", here.latitude + "," + here.longitude, firstLatLng.latitude + "," + firstLatLng.longitude, "walking");
        call.enqueue(new Callback<Example>()
        {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit)
            {
                try {
                    for (int i = 0; i < response.body().getRoutes().size(); i++)
                    {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        locationMapsDistance.setText("Distance:" + distance + ", Duration:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(10)
                                .color(Color.parseColor("#b29911"))
                                .geodesic(true)
                        );
                    }
                }
                catch (Exception e)
                {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    //The user may change location by clicking the info windo and returning to Location Display activity
    @Override
    public void onInfoWindowClick(Marker marker) {
        for(int i = 0; i < narrativeLocations.size(); i++)
        {
            NarrativeLocation location = narrativeLocations.get(i);
            if (marker.getTitle().contentEquals(location.getTitle()))
            {
                rearrangeNarrative(location);
                Intent intent = new Intent(this, LocationDisplayActivity.class);
                intent.putExtra("narrativeTableName", narrativeTableName);
                intent.putExtra("narrativeLocations", narrativeLocations);
                intent.putExtra("here", here);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //This prevents activities being duplicated by removing previous activities, therefore if a user consistently switches directions on the map mulitple instances of the activity aren't created
                startActivity(intent);
            }
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }
        return poly;
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    public void rearrangeNarrative(NarrativeLocation location)
    {
        NarrativeLocation temp = location;
        narrativeLocations.remove(temp);
        narrativeLocations.add(0, temp);
    }
////
@Override
protected void onStart() {
    googleApiClient.connect();
    super.onStart();
}

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

 ///Methods used for Volley
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyBwTBB7kiCOGKfIU-xopnTV6n0z4W2Vbr0");
        return urlString.toString();
    }

    private void getDirection(double sourcelat, double sourcelog, double destlat, double destlog){
        //Getting the URL
        String url = makeURL(sourcelat, sourcelog, destlat, destlog);

        //Showing a dialog till we get the route
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Route", "Please wait...", false, false);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //Calling the method drawPath to draw the path
                        drawPath(response);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                    }
                });

        //Adding the request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //The parameter is the server response
    public void drawPath(String  result) {
        try {
            //Parsing json
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(20)
                    .color(Color.RED)
                    .geodesic(true)
            );
        }
        catch (JSONException e)
        {

        }
    }
///
}
