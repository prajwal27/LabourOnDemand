package com.example.labourondemand;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LabourerHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CardVIewJobs.OnFragmentInteractionListener, OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String tag = LabourerHomeActivity.class.getName();
    private BottomNavigationView navigation;
    private LabourerFinal labourerFinal;
    private ArrayList<Bundle> bundles = new ArrayList<>();
    private ArrayList<CardVIewJobs> cardViewJobs = new ArrayList<CardVIewJobs>();
    private ViewPagerAdapterLabourer viewPagerAdapterLabourer; //= new ViewPagerAdapter(getSupportFragmentManager());
    private WrapContentViewPager viewPager;
    private ArrayList<ServicesFinal> servicesFinalForLocation = new ArrayList<ServicesFinal>();
    private Location myLocation, serviceLocation;
    private TabLayout tabsImages;
    private double distance;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private View mapView;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private SessionManager sessionManager;

    private Double max = 0.0;
    private Spinner spinner;

    static LabourerHomeActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static LabourerHomeActivity getInstance() {
        return instance;
    }
    TextView textView;

    private TextView nameHeader;
    private ImageView photoHeader;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labourer_home);


        sessionManager = new SessionManager(getApplicationContext());
        instance = this;
        labourerFinal = (LabourerFinal) getIntent().getExtras().get("labourer");

        //textView = findViewById(R.id.labourer_home_no_response_tv);

       /* Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Log.d("permission",response.toString());
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(LabourerHomeActivity.this, "you nsna", Toast.LENGTH_LONG).show();
                        Log.d("permissiondenied",response.toString());
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        Log.d("permission ratio",permission.toString());
                    }


                }).check();*/

       if(runtime_permissions(getApplicationContext(),0.0))
       {
           Log.d("labour home",true+"!");
       Intent intent = new Intent(this,MyLocationService.class);
       intent.putExtra("labourer",labourerFinal);
        this.startService(intent);
        }else{

           Log.d("labour home",false+"!");
           Intent intent = new Intent(this,MyLocationService.class);
           intent.putExtra("labourer",labourerFinal);
           this.startService(intent);
       }


//        startService(new Intent(this,MyLocationService .class));

        spinner = findViewById(R.id.sp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.array_distances, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.labourer_home_map);
        mapView = mapFragment.getView();

        serviceLocation = new Location("");

        toolbar = findViewById(R.id.labourer_home_tb);
        drawerLayout = findViewById(R.id.labourer_home_dl);
        navigationView = findViewById(R.id.labourer_home_nv);
        navigation = findViewById(R.id.bottom_nav_view);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.getMenu().getItem(0).setChecked(true);
        View header = navigationView.getHeaderView(0);
        nameHeader = header.findViewById(R.id.nav_header_tv);
        photoHeader = header.findViewById(R.id.nav_header_iv);
        nameHeader.setText(labourerFinal.getName());
        Glide.with(getApplicationContext()).load(labourerFinal.getImage()).into(photoHeader);
        navigationView.setNavigationItemSelectedListener(this);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(1).setChecked(true);

        viewPager = findViewById(R.id.labourer_home_vp);
        viewPagerAdapterLabourer = new ViewPagerAdapterLabourer(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapterLabourer);
        CardVIewJobs c = new CardVIewJobs();

        //viewPagerAdapterLabourer.addFragment(c,"deddescs");
        //viewPagerAdapterLabourer.addFragment(new CardVIewJobs(),"cdc");
        viewPager.setAdapter(viewPagerAdapterLabourer);

        Log.d("labourerHome", labourerFinal.toString());

       /* slide = new Slide(this, new ArrayList<String>());
        viewPager.setAdapter(slide);*/

        tabsImages = findViewById(R.id.labourer_home_tl);
        tabsImages.setupWithViewPager(viewPager, true);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("not Successful token", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        firebaseFirestore.collection("labourer").document(firebaseAuth.getUid()).update("token", token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TOKEN", "SUCCESS");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TOKEN Failure", e.toString());
                                    }
                                });
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("dcs", token);
                        Toast.makeText(LabourerHomeActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TOKEN Failure111", e.toString());

                    }
                });


        //Log.d("MAP",context.getPackageManager().getPackageInfo("com.google.android.gms",0).versionName);
//
//        if (labourerFinal.getCurrentService() == null) {
//            Log.d("tagggg",labourerFinal.getSkill()+"!");
//            fetchServices();
//        }


//        if (labourerFinal.getName() == null) {
//            fetchFromFirebase();
//        } else {
//            fetchServices();
//        }

/*      //dummy comment
        for (int i = 0; i < 5; i++) {
            bundles.add(new Bundle());
            bundles.get(i).putString("key", Integer.toString(i));
        }


        for (int i = 0; i < 5; i++) {
            cardViewJobs.add(new CardVIewJobs());
            cardViewJobs.get(i).setArguments(bundles.get(i));
        }


        //viewPagerAdapter.addFragment(hello1, "Hello1");
        for (int i = 0; i < 5; i++) {
            viewPagerAdapter.addFragment(cardViewJobs.get(i), "hello" + i);
        }
        //viewPagerAdapter.addFragment(hello2, "Hello2");*/

        //viewPager.setAdapter(viewPagerAdapterLabourer);




        //Spinner spinner = (Spinner) findViewById(R.id.labourer_home_sp);
        spinner.setOnItemSelectedListener(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mMap.clear();
                LatLng sydney = new LatLng(servicesFinalForLocation.get(position).getDestinationLatitude(), servicesFinalForLocation.get(position).getDestinationLongitude());
                mMap.addMarker(new MarkerOptions().position(sydney).title("Job location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                Log.d("Location " + position, sydney.toString());



//                Location l1 = new Location("");
//                l1.setLatitude(myLocation.getLatitude());
//                l1.setLongitude(myLocation.getLongitude());
//
//                Location l2 = new Location("");
//                l2.setLatitude(servicesFinalForLocation.get(position).getDestinationLatitude());
//                l2.setLongitude(servicesFinalForLocation.get(position).getDestinationLongitude());
//
//                distance = (l1.distanceTo(l2)) / 1000;
//
//                Bundle bundle = new Bundle();
//                Log.d("distance1",String.valueOf(distance));
//                bundle.putDouble("distance1", distance);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void updateLocation() {
        buildLocationRequest();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());

    }

    @Override
    protected void onRestart() {
        Log.d("onrestart","1");
        super.onRestart();
        Log.d("onrestart","2");
        labourerFinal = sessionManager.getLabourer(labourerFinal.getId());
        navigationView.getMenu().getItem(1).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        Log.d("ondestroy","1");
        super.onDestroy();
        Log.d("ondestroy","2");
    }

    public void update(final String value)
    {

        LabourerHomeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("location brodcast ",value+"!");
                //textView.(value);
            }
        });
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this,MyLocation.class);
        intent.setAction(MyLocation.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private Boolean runtime_permissions(Context context, Double max) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }

        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        if (mMap != null) {
            //fetchServices(max);
            mMap.setMyLocationEnabled(true);
            //  mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
            //mMap.setOnMyLocationClickListener(onMyLocationClickListener);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {

            Log.d("results", grantResults + "!");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onReqPermissionResult", String.valueOf(requestCode));

                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
                if (mMap != null) {
                    //fetchFromFirebase();
                    mMap.setMyLocationEnabled(true);
                    // mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
                    //mMap.setOnMyLocationClickListener(onMyLocationClickListener);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                }
            } else {
                runtime_permissions(getApplicationContext(), max);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(17);
        Log.d("tag", "customer Home Activity onMapReady");

//        LatLng sydney = new LatLng(servicesFinalForLocation.get(0).getDestinationLatitude(), servicesFinalForLocation.get(0).getDestinationLongitude());
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Job location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (!runtime_permissions(getApplicationContext(), max)) {
            Log.d("service onMapReady", "yessss");
        }

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 220);

            mapView.setBackgroundColor(getResources().getColor(R.color.lightRed));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.getMapAsync(this);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_navigation_home:
                    return true;
                case R.id.bottom_navigation_history:
                    Intent intent = new Intent(LabourerHomeActivity.this, LabourerHistoryActivity.class);
                    intent.putExtra("labourer", labourerFinal);
                    startActivity(intent);
                    return true;
                case R.id.bottom_navigation_jobs:
                    Intent intent1 = new Intent(LabourerHomeActivity.this, LabourerMainActivity.class);
                    intent1.putExtra("labourer", labourerFinal);
                    startActivity(intent1);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle menu_bottom_navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_history) {

            Intent intent = new Intent(LabourerHomeActivity.this, LabourerHistoryActivity.class);
            intent.putExtra("labourer", labourerFinal);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_jobs) {
            Intent intent = new Intent(this, LabourerMainActivity.class);
            intent.putExtra("labourer", labourerFinal);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("labourer",labourerFinal);
            intent.putExtra("type","labourer");
            Log.d(tag, "labourer : " + labourerFinal.getAddressLine1());
            startActivity(intent);
        }  else if (id == R.id.nav_wallet) {
            Intent intent = new Intent(this, WalletActivity.class);
            Log.d("wallet",labourerFinal.toString());
            intent.putExtra("labourer",labourerFinal);
            intent.putExtra("type","labourer");
            Log.d(tag, "labourer : " + labourerFinal.getAddressLine1());
            startActivity(intent);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            firebaseAuth.signOut();
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Log.d("labourer home",id+"!");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifications) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void fetchServices(Double max_distance) {

        for (String skill : labourerFinal.getSkill()) {
            firebaseFirestore.collection("services").whereEqualTo("skill", skill).whereEqualTo("isApplyable", true).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                Log.d("tag", labourerFinal.getSkill() + "!" + documentSnapshot.get("skill") + "!" + documentSnapshot.getData().toString());
                                // Log.d("service fetched", documentSnapshot.getString("serviceId"));
                                ServicesFinal servicesFinal = documentSnapshot.toObject(ServicesFinal.class);
                                double distance = 0;
                                servicesFinal.setServiceId(documentSnapshot.getId());
                                //servicesFinal.setCustomerUID(documentSnapshot.getString("customerUID"));
                                Log.d("I don't know", "+" + servicesFinal.toString() + "!" + servicesFinal.getDestinationLongitude() + "!");
                                //final ServicesFinal finalServices = servicesFinal;
                                //ServicesFinal finalServicesFinal = servicesFinal;
                                //firebaseFirestore.collection("customer").document(servicesFinal.getCustomerUID())
                                //ActivityCompat.requestPermissions(LabourerHomeActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

                                mMap.setMyLocationEnabled(true);
                                myLocation = mMap.getMyLocation();

                                Location l1 = new Location("");
                                l1.setLatitude(myLocation.getLatitude());
                                l1.setLongitude(myLocation.getLongitude());

                                Location l2 = new Location("");
                                l2.setLatitude(servicesFinal.getDestinationLatitude());
                                l2.setLongitude(servicesFinal.getDestinationLongitude());


                                Log.d("My location", myLocation.toString() + "!");
//                                Log.d("ser", servicesFinal.getDestinationLatitude() + "!" + servicesFinal.getDestinationLongitude()+"+"+(l1.distanceTo(l2)));
                                Log.d("Service location", servicesFinal.getDestinationLatitude().toString() + " + " + servicesFinal.getDestinationLongitude().toString());
                                serviceLocation.setLatitude(servicesFinal.getDestinationLatitude());
                                serviceLocation.setLatitude(servicesFinal.getDestinationLatitude());
                                distance = (l1.distanceTo(l2)) / 1000;
                                Log.d("distance", String.valueOf(distance));
                                if (distance < max_distance) {
                                    double finalDistance = distance;
                                    servicesFinalForLocation.add(servicesFinal);
                                    Log.d("Added to location", servicesFinal.toString() + "!!!" + servicesFinalForLocation.size());
                                    if (servicesFinalForLocation.size() == 1) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("services", servicesFinal);
                                        bundle.putSerializable("labourer", labourerFinal);
                                        bundle.putDouble("distance", finalDistance);
                                        CardVIewJobs cv = new CardVIewJobs();
                                        cv.setArguments(bundle);
                                        viewPagerAdapterLabourer.addFragment(cv, "cc");
                                        mMap.clear();

                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                        LatLng latLng = new LatLng(l1.getLatitude(),l1.getLongitude());
                                        LatLng latLng2 = new LatLng(l2.getLatitude(),l2.getLongitude());
                                        builder.include(latLng);
                                        builder.include(latLng2);

                                        LatLngBounds bounds = builder.build();
                                        int padding = 50; // offset from edges of the map in pixels
                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                        LatLng sydney = new LatLng(servicesFinalForLocation.get(0).getDestinationLatitude(), servicesFinalForLocation.get(0).getDestinationLongitude());
                                        LatLng myLocationLatLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                                        Log.d("First location", sydney.toString());
                                        mMap.addMarker(new MarkerOptions().position(latLng2).title("Job location"));
                                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                                        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                            @Override
                                            public void onMapLoaded() {
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                                            }
                                        });
                                        //mMap.animateCamera(cu);
                                        TextView textView;
                                        textView = (TextView)findViewById(R.id.labourer_home_tv_error);
                                        textView.setVisibility(View.INVISIBLE);
                                    }
                                    else {
//                                        Log.d("doc00", documentSnapshot1.getData() + "!00");
                                        //CustomerFinal customerFinal = documentSnapshot1.toObject(CustomerFinal.class);
                                        //Log.d("cus", customerFinal.toString() + "!");
                                        Log.d("service", servicesFinal.toString() + " hi");
                                        //servicesFinal.setCustomer(customerFinal);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("services", servicesFinal);
                                        bundle.putSerializable("labourer", labourerFinal);
                                        bundle.putDouble("distance", finalDistance);
                                        CardVIewJobs cv = new CardVIewJobs();
                                        cv.setArguments(bundle);
                                        viewPagerAdapterLabourer.addFragment(cv, "cc");



//                                                        Location l1 = new Location("");
//                                                        l1.setLatitude(myLocation.getLatitude());
//                                                        l1.setLongitude(myLocation.getLongitude());
//
//                                                        Location l2 = new Location("");
//                                                        l2.setLatitude(servicesFinalForLocation.get(0).getDestinationLatitude());
//                                                        l2.setLongitude(servicesFinalForLocation.get(0).getDestinationLongitude());
//
//                                                        distance = (l1.distanceTo(l2))/1000;
//                                                        Log.d("distance",String.valueOf(distance));
//                                                        bundle.putDouble("distance",distance);
                                        //viewPager.setAdapter(viewPagerAdapterLabourer);

                                        // To add code to add to viewPager
                                               /* bundles.add(new Bundle());
                                                bundles.get(j).putString("key", servicesFinal.getCustomerUID());
                                                cardViewJobs.add(new CardVIewJobs());
                                                cardViewJobs.get(j).setArguments(bundles.get(j);
                                                viewPagerAdapter.addFragment(cardViewJobs.get(j), "hello" + j);
                                                j = j +1;*/
                                        //
                                        //finalServices.setCustomer(documentSnapshot.toObject(Customer.class));
                                        //dashboardAdapter.added(finalServices);
                                    }
                                    viewPagerAdapterLabourer.notifyDataSetChanged();

                                    viewPager.setOffscreenPageLimit(viewPagerAdapterLabourer.getCount());

                                }

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(tag, "error fetchService1 : " + e.toString());
                        }
                    });

        }
        //add isApplyable later


    }

    /*mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
        @Override
        public void onMapLoaded() {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
        }
    });*/

    //Later to be deleted

    private void fetchFromFirebase() {

        fetchServices(Double.POSITIVE_INFINITY);

       /* firebaseFirestore.collection("labourer").document(firebaseAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //labourer = new Labourer();
                        if (documentSnapshot.getData() != null) {
                            labourerFinal = documentSnapshot.toObject(LabourerFinal.class);
                            Log.d("Labourer info fetched", firebaseAuth.getUid() + "!");

                            if (labourerFinal.getCurrentService() == null) {
                                Log.d("tagggg", labourerFinal.getSkill() + "!");
                                fetchServices();
                            } else {

                            }

                        } else {
                            Log.d(tag, "null");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });*/
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewPagerAdapterLabourer = new ViewPagerAdapterLabourer(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapterLabourer);
        servicesFinalForLocation = new ArrayList<ServicesFinal>();
        String list[] = getResources().getStringArray(R.array.array_distances);
        Log.d("item", "dccv");

        if (position == 0) {
            Log.d("item", "0");
//            max = Double.POSITIVE_INFINITY;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            //fetchServices(Double.POSITIVE_INFINITY);
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
        } else if (position == 1) {
            Log.d("item", "cdsfcdcdscfdvdv" + position);
            max = 1.0;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            Log.d("value of distance", max.toString());
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        } else if (position == 2) {
            Log.d("item", "cdsfcdcdscfdvdv" + position);
            max = 2.0;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            Log.d("value of distance", max.toString());
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        } else if (position == 3) {
            Log.d("item", "cdsfcdcdscfdvdv" + position);
            max = 3.0;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            Log.d("value of distance", max.toString());
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        } else if (position == 4) {
            Log.d("item", "cdsfcdcdscfdvdv" + position);
            max = 5.0;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            Log.d("value of distance", max.toString());
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        } else if (position == 5) {
            Log.d("item", "cdsfcdcdscfdvdv" + position);
            max = 10.0;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            Log.d("value of distance", max.toString());
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        } else if (position == 6) {
            Log.d("item", "vvfdvdv");
            max = Double.POSITIVE_INFINITY;
//            if (!runtime_permissions(getApplicationContext(),max)) {
//                Log.d("service onMapReady", "yessss");
//            }
            TextView textView = findViewById(R.id.labourer_home_tv_error);
            textView.setVisibility(View.VISIBLE);
            fetchServices(max);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("nothing", "dccv");
        //fetchServices(Double.POSITIVE_INFINITY);
    }



}