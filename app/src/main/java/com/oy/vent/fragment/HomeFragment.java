package com.oy.vent.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oy.vent.R;
import com.oy.vent.utilities.GPSTracker;


/**
 * This is an example of a fragment which can use the external Android camera to take
 * a picture. It is important to remember to save the file URI where we want to save
 * our picture into the bundle because this data will be cleared when the camera is loaded.
 * The appropriate place to do this is in the Fragment's parent activity because there isn't a
 * good "entry" point when the fragment returns to the foreground to retrieve the bundle info.
 *
 * Reference: http://developer.android.com/training/camera/photobasics.html
 *
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class HomeFragment extends BaseFragment  {

    private static String TAG =  HomeFragment.class.getName();


    // Progress Dialog
    private ProgressDialog pDialog = null;

    //GPS Tracker to get Geo coordinates
    private GPSTracker gpsTracker;

    /**
     * Default empty constructor.
     */
    public HomeFragment(){
        super();
    }

    /**
     * Static factory method
     * @param sectionNumber
     * @return
     */
    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * OnCreateView fragment override
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //let's first set the layout container
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setHasOptionsMenu(true);

            //setup GPS Tracker to get the geo coordinates
            if(gpsTracker == null) {
                gpsTracker = new GPSTracker(getActivity());
                if (!gpsTracker.canGetLocation()) {
                    gpsTracker.showSettingsAlert();
                }
            }

            if(pDialog == null)
                pDialog = new ProgressDialog(getActivity());

        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }

        setRetainInstance(true);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( pDialog!=null && pDialog.isShowing() ){
            pDialog.dismiss();
        }
    }

    @Override
    public void onPause(){
        super.onDestroy();
        if ( pDialog!=null && pDialog.isShowing() ){
            pDialog.dismiss();
        }
    }



    /*public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }*/




    /*public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }



    /**
     * Use for decoding camera response data.
     *
     * @param data
      * @return
     */
    /*public static Bitmap getBitmapFromCameraData(Intent data, Context context){

        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return BitmapFactory.decodeFile(picturePath);
    }*/




}
