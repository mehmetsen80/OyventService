package com.oy.vent;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.oy.vent.fragment.BaseFragment;
import com.oy.vent.fragment.CommunityFragment;
import com.oy.vent.fragment.HomeFragment;
import com.oy.vent.fragment.NavigationDrawerFragment;
import com.oy.vent.fragment.SettingsFragment;


/**
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class MainActivity extends CameraActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, BaseFragment.OnFragmentInteractionListener {


    /**
     * Actions
     */
    public static final int SELECT_PHOTO_ACTION = 0;

    /**
     * Fragment Identifiers
     */
    public static final int SIMPLE_CAMERA_INTENT_FRAGMENT = 0;
    public static final int COMMUNITY_FRAGMENT = 1;
    public static final int SETTINGS_FRAGMENT = 2;
    public static final int NATIVE_CAMERA_FRAGMENT = 3;
    public static final int HORIZONTAL_GALLERY_FRAGMENT = 4;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        BaseFragment targetFragment = null;

        // Populate the fragment
        switch (position) {
            case SIMPLE_CAMERA_INTENT_FRAGMENT: {
                targetFragment = HomeFragment.newInstance(position + 1);
                break;
            }
            case COMMUNITY_FRAGMENT: {
                targetFragment = CommunityFragment.newInstance(position + 1);
                break;
            }
            case SETTINGS_FRAGMENT: {
                targetFragment = SettingsFragment.newInstance(position + 1);
                break;
            }
            /*case NATIVE_CAMERA_FRAGMENT: {

                break;
            }
            case HORIZONTAL_GALLERY_FRAGMENT:{

                break;
            }*/

            default:
                break;
        }

        // Select the fragment.
        fragmentManager.beginTransaction()
                .replace(R.id.container, targetFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "Home";// getString(R.string.title_section1);
                break;
            case 2:
                mTitle = "Community";// getString(R.string.title_section2);
                break;
            case 3:
                mTitle = "Settings";// getString(R.string.title_section3);
                break;
            /*case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;*/
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
       // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        /*actionBar.setCustomView(R.layout.actionbar_view);
        ImageView addfeed = (ImageView) actionBar.getCustomView().findViewById(R.id.addfeed);
        addfeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Add Feed triggered",
                        Toast.LENGTH_LONG).show();
            }
        });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);*/

        actionBar.setTitle(mTitle);
        //actionBar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_add_feed:
                Intent intent = new Intent(MainActivity.this,PostFeedActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Toast.makeText(MainActivity.this, "Settings triggered",
                        Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Handle Incoming messages from contained fragments.
     */

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onFragmentInteraction(int actionId) {

    }
}
