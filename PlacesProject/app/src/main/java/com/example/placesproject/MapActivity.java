package com.example.placesproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; //map object
    private FusedLocationProviderClient mfusedLocationProviderClient; //fetch current location
    private PlacesClient placesClient; //search suggestions
    private List<AutocompletePrediction> predictionList; //save suggested places
    private Location mLastKnownLocation; //save last known location
    private LocationCallback locationCallback; //updating user's request if last known location is null

    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private Button hidden_button;
    private RippleBackground rippleBackground;

    private final float DEFAULT_ZOOM=18;

    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        name=findViewById(R.id.name);
        materialSearchBar=findViewById(R.id.searchBar);
        btnFind=findViewById(R.id.btn_find);
        hidden_button=findViewById(R.id.hidden_button);
        rippleBackground=findViewById(R.id.ripple_bg);

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView=mapFragment.getView();

        mfusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this,"/*insert your own api-key here*/");
        placesClient=Places.createClient(this);
        final AutocompleteSessionToken token=AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(),true,null,true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if(buttonCode==MaterialSearchBar.BUTTON_NAVIGATION){
                    //open or close a navigation drawer
                }
                else if(buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder().
                        setCountry("bd").
                        setTypeFilter(TypeFilter.ADDRESS).
                        setSessionToken(token).
                        setQuery(charSequence.toString()).
                        build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if(predictionsResponse!=null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionList = new ArrayList<>();

                                //fetch the suggested locations and convert them to string
                                for(int i =0 ; i<predictionList.size() ; i++){
                                    AutocompletePrediction prediction  = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionList);
                                if(!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }
                        else{
                            Log.i("key_prediction","prediction fetching task unsuccessful.");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //we don't have the location's latitude and longitude so we request google server to send the places's latitude and longitude
        //then we move the camera to desired location
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if(position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);
                //closing keyboard after clicking suggestion
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm!=null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);

                final String placeId=selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG); // we are interested only to the places's latitude and longitude

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId,placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("new key","place found "+place.getName());
                        LatLng latLngofplace = place.getLatLng();
                        if(latLngofplace!=null){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngofplace,DEFAULT_ZOOM));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e instanceof ApiException){
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statuscode = apiException.getStatusCode();
                            Log.i("never key","place not found: "+e.getMessage());
                            Log.i("hybrid key","status code: "+statuscode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currentMapLocation = mMap.getCameraPosition().target;
                rippleBackground.startRippleAnimation();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4417,91.9090),DEFAULT_ZOOM));
                name.setText("Arnob Sharee Collection");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4416,91.9094),DEFAULT_ZOOM));
                        name.setText("Noor-M-Fashion");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3668,91.8226),DEFAULT_ZOOM));
                                name.setText("Fashion King");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4416,91.9084),DEFAULT_ZOOM));
                                        name.setText("Style Park Gents & Kids");
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3469,91.8341),DEFAULT_ZOOM));
                                                name.setText("Baatighar");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3339,91.8299),DEFAULT_ZOOM));
                                                        name.setText("Peoples Book Agency");
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3410,91.8372),DEFAULT_ZOOM));
                                                                name.setText("Genuine Library");
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        getDeviceLocation();
                                                                        Intent ob=new Intent(MapActivity.this,ProductActivity.class);
                                                                        ob.putExtra("tag",1);
                                                                        startActivity(ob);
                                                                        finish();
                                                                        rippleBackground.stopRippleAnimation();
                                                                    }
                                                                },1500);
                                                            }
                                                        },1500);
                                                    }
                                                },1500);
                                            }
                                        }, 1500);
                                    }
                                }, 1500);
                            }
                        }, 1500);
                    }
                }, 1500);
            }
        });
        hidden_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currentMapLocation = mMap.getCameraPosition().target;
                rippleBackground.startRippleAnimation();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4596,91.9657),DEFAULT_ZOOM));
                name.setText("Rafsan Enterprise");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4603,91.9665),DEFAULT_ZOOM));
                        name.setText("হাবিবীয়া ইলেকট্রনিক্স");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4416,91.9085),DEFAULT_ZOOM));
                                name.setText("Singer Plus");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.4605,92.0055),DEFAULT_ZOOM));
                                        name.setText("Lokman Electronics Shop");
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3558,91.8391),DEFAULT_ZOOM));
                                                name.setText("DogPatch Music");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3486,91.8343),DEFAULT_ZOOM));
                                                        name.setText("Priyanka Music");
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3760,91.8447),DEFAULT_ZOOM));
                                                                name.setText("Guitar Land Chittagong");
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        getDeviceLocation();
                                                                        Intent ob=new Intent(MapActivity.this,ProductActivity.class);
                                                                        ob.putExtra("tag",2);
                                                                        startActivity(ob);
                                                                        finish();
                                                                        rippleBackground.stopRippleAnimation();
                                                                    }
                                                                },1500);
                                                            }
                                                        },1500);
                                                    }
                                                },1500);
                                            }
                                        }, 1500);
                                    }
                                }, 1500);
                            }
                        }, 1500);
                    }
                }, 1500);
            }
        });
    }


    //this method is called when the map is ready and loaded
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true); // this method checks whether the permission is taken but it is sure that the permission is taken in the previous activity
        mMap.getUiSettings().setMyLocationButtonEnabled(true); //current location round button is enabled

        if(mapView!=null && mapView.findViewById(Integer.parseInt("1")) != null){
            View locationButton=((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,40,180);
        }

        //check if GPS is enabled or not and then request user to enable it
        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);

        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //called if location toggle is already enabled
        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException resolvable= (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this,51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(materialSearchBar.isSuggestionsVisible()){
                    materialSearchBar.clearSuggestions();
                }
                if(materialSearchBar.isSearchEnabled()){
                    materialSearchBar.disableSearch();
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==51){
            if(resultCode==RESULT_OK){ //user accepted the request and enabled the GPS option in their phone
                getDeviceLocation();
            }
        }
    }

    //it is sure that permission is granted
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mfusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            mLastKnownLocation=task.getResult();
                            if(mLastKnownLocation!=null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                            }
                            else{
                                final LocationRequest locationRequest=LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                locationCallback=new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if(locationResult == null){
                                            return;
                                        }
                                        mLastKnownLocation=locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                                        mfusedLocationProviderClient.removeLocationUpdates(locationCallback); //without this line user will repeatedly get location updates
                                    }
                                };
                                mfusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
                            }
                        }
                        else{
                            Toast.makeText(MapActivity.this,"Unable to get last location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
