package org.boxtree.vic.locationtracker;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.boxtree.vic.locationtracker.vo.Trip;

import java.util.ArrayList;


/**
 * Created by victoriahawkins on 5/30/17.
 *
 * For Material Design Style Navigation Drawer, used tutorial at https://github.com/codepath/android_guides/wiki/Fragment-Navigation-Drawer

 */

public class MainActivity extends AppCompatActivity implements MapFragmentRecorder.MapFragInteraction, TripItemFragment.OnListFragmentInteractionListener, SettingsFragment.OnSettingsFragmentInteractionListener, HelpFragment.OnHelpFragmentInteractionListener{

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    private MapFragmentRecorder mMapFragmentRecorder;


    // Make sure to be using android.support.v7.app.ActionBarDrawerToggle version.
    // The android.support.v4.app.ActionBarDrawerToggle has been deprecated.
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        // Find our navigation view within drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view for click events
        setupDrawerContent(nvDrawer);

        // animate hamburger icon
        // Tie DrawerLayout events to the ActionBarToggle
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);

        // Lookup navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nvView);
// Inflate the header view at runtime
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
// We can now look up items within the header if needed
//        ImageView ivHeaderPhoto = headerLayout.findViewById(R.id.imageView);



        // default start first fragment before nav drawer clicked
        displayDefaultFragment();

    }



    // animate hamburger icon code

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }




    // when the hamburget is selected? TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:

                fragmentClass = MapFragmentRecorder.class;

                // we only want to instantiate mapfragment once otherwise we will have a leak
                if (mMapFragmentRecorder == null)
                    mMapFragmentRecorder = new MapFragmentRecorder();

                fragment = mMapFragmentRecorder;

                break;

            case R.id.nav_second_fragment:

                fragmentClass = TripItemFragment.class;
                break;

            case R.id.nav_third_fragment:
                fragmentClass = MapFragmentPointsOfInterest.class;

//                Toast.makeText(this, "Implement google Points of Interest for last location visited", Toast.LENGTH_LONG).show();

                break;

            case R.id.settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;

            case R.id.help_fragment:
                fragmentClass = HelpFragment.class;
                break;


            default:
                fragmentClass = MapFragment.class;
        }

        try {
            if (fragment == null)
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }



    final private static int SAVE_TRIP_ACTIVITY = 1;

    @Override
    public int saveTrip(ArrayList<Location> route) {

        Log.d("MainActivity", "MapFragInteraction.saveTrip() called");



        Log.d("MainActivity", "routes to save " + route);

            // random picture
            // title
            // note
            // route
            // pictures


        Intent saveTripIntent = new Intent(MainActivity.this, SaveTrip.class);

        saveTripIntent.putExtra("Route", route);

        startActivityForResult(saveTripIntent, SAVE_TRIP_ACTIVITY);

        return 0;
    }

    // interaction with the Trip Item List Fragment

    @Override
    public void onListFragmentInteraction(Trip item) {
        // TODO interaction here with cardview



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
            displaySavedTripsFragment();


    }

    /* this will create a new version of the saved trips fragment which will also refresh the list of trips from the database */
    private void displaySavedTripsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new TripItemFragment()).commit();
    }

    private void displayDefaultFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        // we only want to instantiate mapfragment once otherwise we will have a leak
        if (mMapFragmentRecorder == null)
            mMapFragmentRecorder = new MapFragmentRecorder();

        fragmentManager.beginTransaction().replace(R.id.flContent, mMapFragmentRecorder).commit();


    }


    @Override
    public void onHelpFragmentInteraction(Uri uri) {
        Toast.makeText(this, "Thanks for visiting Help", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSettingsFragmentInteraction(Uri uri) {

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_LONG).show();


    }
}
