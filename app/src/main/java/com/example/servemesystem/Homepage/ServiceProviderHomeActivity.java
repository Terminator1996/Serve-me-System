package com.example.servemesystem.Homepage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.servemesystem.LoginActivity;
import com.example.servemesystem.R;

import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.servemesystem.domain.ConstantResources;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;

import java.util.ArrayList;

public class ServiceProviderHomeActivity extends AppCompatActivity {

    private static final String TAG = "ServiceProviderHome";
    private AppBarConfiguration mAppBarConfiguration;
    private ArrayList<String> mUserNames = new ArrayList<>();
    private ArrayList<String> mUserMessage = new ArrayList<>();
    private ArrayList<String> mRequestTypes = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String userName,services;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_home2);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_past_orders,
                R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        SharedPreferences prefs = getSharedPreferences("currUser", MODE_PRIVATE);
        userName = prefs.getString("userName", null);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.fragment_prev_orders:
                        Intent myint = new Intent(ServiceProviderHomeActivity.this,PastUserOrders.class);
                        startActivity(myint);
                        break;
                    case R.id.fragment_logout:
                        SharedPreferences prefs = getSharedPreferences("currUser", MODE_PRIVATE);
                        prefs.edit().clear();
                        prefs.edit().apply();
                        Intent myInt = new Intent(ServiceProviderHomeActivity.this, LoginActivity.class);
                        startActivity(myInt);
                        break;
                }
                drawer.closeDrawers();
                return false;
            }
        });
        myRef.child(ConstantResources.SERVICE_PROVIDER).child(userName).child(ConstantResources.SERVICE_PROVIDER_VERIFIED).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String verified = dataSnapshot.getValue(String.class);
                if(verified.equals("true")||verified.equals("True")){
                    getServiceProviderTypes();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void getServiceProviderTypes() {
        myRef.child(ConstantResources.SERVICE_PROVIDER).child(userName).child(ConstantResources.SERVICE_TYPES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               services = snapshot.getValue(String.class);
                final String[] mServiceTypes = services.split(",");
                for (final String service : mServiceTypes){
                    myRef.child(ConstantResources.SERVICE_REQUESTS).child(service).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Log.e("Count ", "" + snapshot.getChildrenCount());
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                String uname = postSnapshot.getKey();
                                String message = postSnapshot.getValue(String.class);
                                mUserNames.add(uname);
                                mUserMessage.add(message);
                                mRequestTypes.add(service);
                            }
                            initRecyclerView();
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            Log.e("The read failed: ", firebaseError.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ServiceProviderAdapter adapter = new ServiceProviderAdapter(this, mUserNames, mRequestTypes,mUserMessage);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.service_provider_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logoutButton:
                SharedPreferences prefs = getSharedPreferences("currUser", MODE_PRIVATE);
                prefs.edit().clear();
                prefs.edit().apply();
                Intent myInt = new Intent(ServiceProviderHomeActivity.this, LoginActivity.class);
                startActivity(myInt);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
