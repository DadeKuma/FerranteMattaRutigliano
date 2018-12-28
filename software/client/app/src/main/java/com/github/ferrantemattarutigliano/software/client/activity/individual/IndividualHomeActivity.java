package com.github.ferrantemattarutigliano.software.client.activity.individual;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.ferrantemattarutigliano.software.client.R;
import com.github.ferrantemattarutigliano.software.client.fragment.LogoutFragment;
import com.github.ferrantemattarutigliano.software.client.fragment.NotImplementedFragment;
import com.github.ferrantemattarutigliano.software.client.fragment.individual.IndividualAccountFragment;
import com.github.ferrantemattarutigliano.software.client.fragment.individual.IndividualIndividualManageRequestsFragment;
import com.github.ferrantemattarutigliano.software.client.fragment.individual.IndividualTrack4RunFragment;
import com.github.ferrantemattarutigliano.software.client.model.IndividualDTO;
import com.github.ferrantemattarutigliano.software.client.presenter.individual.IndividualHomePresenter;
import com.github.ferrantemattarutigliano.software.client.session.Profile;
import com.github.ferrantemattarutigliano.software.client.session.SessionDirector;
import com.github.ferrantemattarutigliano.software.client.view.individual.IndividualHomeView;
import com.github.ferrantemattarutigliano.software.client.websocket.connection.StompCallback;
import com.github.ferrantemattarutigliano.software.client.websocket.connection.StompClient;
import com.github.ferrantemattarutigliano.software.client.websocket.payload.StompFrame;

public class IndividualHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IndividualHomeView {

    private IndividualHomePresenter individualHomePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_individual);
        individualHomePresenter = new IndividualHomePresenter(this);
        individualHomePresenter.doFetchProfile();
    }

    @Override
    public void onProfileFetch(IndividualDTO individualDTO) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create left navigation menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //populate with profile info
        String profileName = individualDTO.getFirstname() + "\n" + individualDTO.getLastname();
        String profileInfo = individualDTO.getSsn();
        Profile profile = new Profile(profileName, profileInfo);
        SessionDirector.setProfile(profile);

        //on start select the page my account
        changeShowedFragment(IndividualAccountFragment.class);
        MenuItem myAccount = navigationView.getMenu().findItem(R.id.nav_my_account);
        selectItem(myAccount);

        SessionDirector.getStompClient().subscribe("/request/" + SessionDirector.USERNAME);
    }

    @Override
    public void startStompClient() {
        Intent intent = new Intent(this, IndividualHomeActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        StompClient stompClient = new StompClient(new StompCallback() {
            @Override
            public void onResponseReceived(StompFrame response) {
                if (!response.getStompBody().isEmpty()) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "trackme")
                            .setSmallIcon(R.drawable.trackme_small_logo)
                            .setContentTitle("Incoming Requests")
                            .setContentText(response.getStompBody())
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

                    if (Build.VERSION.SDK_INT < 26) {
                        notificationBuilder.setChannelId("trackme");
                    }
                    if (Build.VERSION.SDK_INT >= 26) { //push notifications require API level 26
                        NotificationChannel channel = new NotificationChannel(
                                "trackme",
                                "Incoming Requests",
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(channel);
                        }
                    }
                    notificationManager.notify(0, notificationBuilder.build());
                }
            }
        });
        SessionDirector.setStompClient(stompClient);
        SessionDirector.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_individual, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Class fragmentClass;
        switch (id){
            case R.id.nav_my_account:
                fragmentClass = IndividualAccountFragment.class;
                break;
            case R.id.nav_external_device:
                fragmentClass = NotImplementedFragment.class;
                break;
            case R.id.nav_manage_request:
                fragmentClass = IndividualIndividualManageRequestsFragment.class;
                break;
            case R.id.nav_automatedsos:
                fragmentClass = NotImplementedFragment.class;
                break;
            case R.id.nav_track4run:
                fragmentClass = IndividualTrack4RunFragment.class;
                break;
            case R.id.nav_logout:
                fragmentClass = LogoutFragment.class;
                break;
            default:
                fragmentClass = NotImplementedFragment.class;
        }
        changeShowedFragment(fragmentClass);
        selectItem(item);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectItem(MenuItem item){
        item.setChecked(true);
        setTitle(item.getTitle());
    }

    private void changeShowedFragment(Class fragmentClass){
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            int fragmentContainerId = R.id.frame_home_individual_frag_container;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(fragmentContainerId, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
