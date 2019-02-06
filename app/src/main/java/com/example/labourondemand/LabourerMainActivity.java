package com.example.labourondemand;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class LabourerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    private FloatingActionButton fab;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Labourer labourer ;
    private String tag = LabourerMainActivity.class.getName();
    private RecyclerView recyclerView;
    private DashboardAdapter dashboardAdapter;
    private TextView visibleText;
    private String currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labourer_main);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if(getIntent().getExtras() != null) {
            currentService = getIntent().getStringExtra("currentService");
            labourer = (Labourer) getIntent().getExtras().get("labourer");
        }

        toolbar = findViewById(R.id.labourer_main_tb);
        drawerLayout = findViewById(R.id.labourer_main_dl);
        fab = findViewById(R.id.labourer_main_fab);
        navigationView = findViewById(R.id.labourer_main_nav);
        recyclerView = findViewById(R.id.dashboard_labourer_rv);
        visibleText = findViewById(R.id.visible);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        dashboardAdapter = new DashboardAdapter(getApplicationContext(),new ArrayList<Services>(),0);
        recyclerView.setAdapter(dashboardAdapter);
        recyclerView.setHasFixedSize(false);

        if( currentService == null ){

            if(labourer == null) {
                fetchFromFirebase();
            }else{
                fetchServices();
            }
            visibleText.setVisibility(View.GONE);
        }else{
            visibleText.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

/*
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.customer_main_dl);
*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(0);
        navigationView.setNavigationItemSelectedListener(this);


    }

    private void fetchFromFirebase() {

        firebaseFirestore.collection("labourer").document(firebaseAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //labourer = new Labourer();
                        if (documentSnapshot.getData() != null) {
                            labourer = documentSnapshot.toObject(Labourer.class);
                            Log.d(tag, documentSnapshot.getData().toString() + "!");

                            if (labourer.getCurrentService() == null) {
                                fetchServices();
                            }else{

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
                });
    }

    private void fetchServices() {

        firebaseFirestore.collection("services").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Services services ;
                            Log.d("tag",labourer.getSkill()+"!"+documentSnapshot.get("skill")+"!"+documentSnapshot.getData().toString());
                            if(documentSnapshot.getString("skill").equals(labourer.getSkill())){
                                services = documentSnapshot.toObject(Services.class);
                                services.setServiceID(documentSnapshot.getId());
                                final Services finalServices = services;
                                firebaseFirestore.collection("customer").document(services.getCustomerUID()).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                finalServices.setCustomer(documentSnapshot.toObject(Customer.class));
                                                dashboardAdapter.added(finalServices);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(tag,"error fetchService2 : "+e.toString());
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(tag,"error fetchService1 : "+e.toString());
                    }
                });
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.labourer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {

        } else if (id == R.id.nav_history) {
            //Toast.makeText(this,"History yet to be Developed",)
            Intent intent = new Intent(this, PreviousActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_person) {
            Intent intent = new Intent(this, ProfileActivity.class);
            /*Bundle bundle = new Bundle();
            bundle.putParcelable("labourer",labourer);*/
            intent.putExtra("user", labourer);
            intent.putExtra("type","labourer");
            Log.d(tag, "labourer : " + labourer.getAddressLine1());
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            //Intent settings = new Intent(LabourerMainActivity.this,SettingsActivity.class);
            //startActivity(settings);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
