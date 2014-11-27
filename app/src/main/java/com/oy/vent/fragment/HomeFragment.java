package com.oy.vent.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.oy.vent.BuildConfig;
import com.oy.vent.R;
import com.oy.vent.model.RowItem;
import com.oy.vent.model.UserInfo;
import com.oy.vent.model.ViewHolder;
import com.oy.vent.utilities.AlertDialogManager;
import com.oy.vent.utilities.ConnectionDetector;
import com.oy.vent.utilities.GPSTracker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
public class HomeFragment extends BaseFragment  implements AdapterView.OnItemClickListener{

    private static String TAG =  HomeFragment.class.getName();
    private static final String JSON_URL = "http://oyvent.com/ajax/Feeds.php";
    private int mImageThumbSize;
    private int mImageThumbWidth;
    private int mImageThumbHeight;
    private int mImageThumbSpacing;
    private int currentPage = 0;

    // Connection detector
    private ConnectionDetector cd = null;
    // Progress Dialog
    private ProgressDialog pDialog = null;
    // Alert dialog manager
    private AlertDialogManager alert = null;
    // GPS Tracker to get Geo coordinates
    private GPSTracker gpsTracker;
    // custom gridview adapter
    private CustomListViewAdapter customAdapter = null;
    // flag if loading
    private boolean flag_loading = true;
    public GridView gridView = null;
    private ArrayList<RowItem> listValue = null;
    private LayoutInflater layoutx;

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
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        final GridView mGridView = (GridView) view.findViewById(R.id.gridView);
        mGridView.setAdapter(customAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                   // mImageFetcher.setPauseWork(true);
                } else {
                   //mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                boolean loadMore = // maybe add a padding
                        (firstVisibleItem + visibleItemCount >= totalItemCount);

                if(loadMore && !flag_loading)
                {
                    flag_loading = true;
                    if(customAdapter != null){
                        Log.d(TAG,"custom adapter not null");
                        addMoreData(); //to do: remove comment
                    }
                }

            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (customAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));

                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                customAdapter.setNumColumns(numColumns);
                                customAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                            }
                        }
                    }
              });


        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setHasOptionsMenu(true);

            cd = new ConnectionDetector(getActivity().getApplicationContext());
            // Check for internet connection
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialog(getActivity(), "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                // stop executing code by return
                return;
            }

            if(listValue == null)
                listValue = new ArrayList<RowItem>();

            if(customAdapter == null)
                customAdapter = new CustomListViewAdapter(
                    getActivity(),R.layout.fragment_home_list_item,listValue);

            if(layoutx == null)
                layoutx = getActivity().getLayoutInflater();

            if(alert == null)
                alert = new AlertDialogManager();

            //setup GPS Tracker to get the geo coordinates
            if(gpsTracker == null) {
                gpsTracker = new GPSTracker(getActivity());
                if (!gpsTracker.canGetLocation()) {
                    gpsTracker.showSettingsAlert();
                }
            }

            if(pDialog == null)
                pDialog = new ProgressDialog(getActivity());

            mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
            mImageThumbWidth = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_width);
            mImageThumbHeight = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_height);
            mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

            setRetainInstance(true);

            new LoadFeeds().execute();

        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
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

    public void addMoreData()
    {
        new LoadFeeds().execute();
        customAdapter.notifyDataSetChanged();
    }

    public void reloadPage(int currPage,String currEventID)
    {
        currentPage = currPage;
        reloadList();
    }


    protected void reloadList() {
        currentPage = 0;
        listValue.clear();
        customAdapter.notifyDataSetChanged();
    }

    //to do: remove this later
    private Random random = new Random();
    final float MIN = 10.0f;
    final float MAX = 100.0f;
    public float getRandom(float min, float max){
        return (random.nextFloat() * max) + min;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //openDetail(parent, view, position, id);
    }

    /**
     * Background Async Task to Load all Albums by making http request
     * */
    class LoadFeeds extends AsyncTask<Void, Void,  Boolean> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting Feeds JSON
         */
        @Override
        protected  Boolean doInBackground(Void... args) {

            try {
                HttpPost httpPost = new HttpPost(JSON_URL);
                httpPost.addHeader("Accept", "application/json");
                HttpClient httpClient = new DefaultHttpClient();

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setCharset(MIME.UTF8_CHARSET);
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                builder.addTextBody("processType", "GETFEEDLIST", ContentType.create("text/plain", MIME.UTF8_CHARSET));
                builder.addTextBody("currentPage", Integer.toString(currentPage), ContentType.create("text/plain", MIME.UTF8_CHARSET));
                httpPost.setEntity(builder.build());
                Log.d(TAG, "Executing feed request: " + httpPost.getRequestLine());
                httpClient.execute(httpPost, new DataPostResponseHandler());

            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

            return true;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(Boolean args) {

            // dismiss the dialog after getting all albums
            pDialog.dismiss();
            // updating UI from Background Thread
            if(getActivity() != null){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                         customAdapter.notifyDataSetChanged();
                         flag_loading = false;
                         currentPage++;
                    }
                });
            }

        }

    }


    private class DataPostResponseHandler implements ResponseHandler<Object> {

        private InputStream is = null;
        private String json = null;

        @Override
        public Boolean handleResponse(HttpResponse response)
                throws IOException {

            HttpEntity r_entity = response.getEntity();
            is = r_entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();

            Log.d("Login Data JSON Result: ", "> " + json);

            try {
                JSONArray results = new JSONArray(json);

                if (results != null) {
                    // looping through All feeds
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);
                        String photoId = c.getString("PKPHOTOID");
                        String urllarge = c.getString("URLLARGE");
                        String urlmedium = c.getString("URLMEDIUM");
                        String urlsmall = c.getString("URLSMALL");
                        String urlthumb = c.getString("URLTHUMB");
                        //String username = c.getString("OWNEDBY");
                        //String ownedby = c.getString("OWNEDBY");
                        String username = "mehmetsen80";
                        String ownedby = "memosen80";
                        String postdate = c.getString("POSTDATE");
                        String fullname = "";

                        Log.d(TAG, "photoId:"+photoId+" postdate:"+postdate);

                        float points = getRandom(MIN, MAX);
                        Log.d(TAG,"points: "+points);

                        //to do: get this later from db
                        double geo =  0.0;

                        try
                        {
                            final RowItem rdItem = new RowItem(i,photoId,postdate,urllarge,urlmedium,urlsmall,urlthumb, username, ownedby, false, points, geo);
                            listValue.add(rdItem);
                        }
                        catch (ParseException e)
                        {
                            Log.e(TAG,e.getMessage());
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG,e.getMessage());
            }

            return true;
        }
    }


            public class CustomListViewAdapter extends ArrayAdapter<RowItem> {
        private ViewHolder holder = null;
        private TextView photoId = null;
        private TextView postDate = null;
        private ImageView thumb = null;
        private TextView userName = null;
        private TextView points = null;
        private TextView geo = null;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;
        private int mActionBarHeight = 0;
        private Context mContext;
        DecimalFormat formatDecimal = new DecimalFormat("0.0");


        public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
            super(context, resourceId, items);
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, context.getResources().getDisplayMetrics());
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final RowItem rowItem = getItem(position);

            if (convertView == null) {
                convertView = layoutx.inflate(R.layout.fragment_home_list_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
                holder = (ViewHolder) convertView.getTag();

                if (holder == null) {
                    Log.e(TAG, "holder is null");
                    return null;
                }

                postDate = holder.getPostDate();
                postDate.setText(rowItem.postDate);

                userName = holder.getUserName();
                userName.setText(rowItem.username);

                photoId = holder.getPhotoId();
                photoId.setText(rowItem.photoId);

                points = holder.getPoints();
                if (points != null)
                    points.setText(String.format("%.2f", rowItem.points));

                geo = holder.getGeo();
                if (geo != null && gpsTracker.canGetLocation()) {
                    double distance = gpsTracker.getDistance(gpsTracker.getLatitude(), gpsTracker.getLongitude(), 34.770091, -92.336152);
                    geo.setText(String.valueOf(formatDecimal.format(distance)) + " mi");
                }
                // Now handle the ImageView thumbnails
                thumb = holder.getThumb();
                thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                thumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "position [" + position + "]");
                    }
                });

                if (!rowItem.urlthumb.equals("") && rowItem.urlthumb != null) {
                    Log.d(TAG, "urlthumb: " + rowItem.urlthumb);
                    try {
                        //To Do: use the advanced load image library
                        //imageLoader.DisplayImage(rowItem.thumb , thumb2, Utils.SIZE_THUMB2, false);
                        //mImageFetcher.loadImage(rowItem.urlsmall, thumb);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }

                    thumb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //openDetail(null, v, position, position);
                        }
                    });
                }

                convertView.setTag(holder);
                //thumb2.setImageURI(Uri.parse(rowItem.thumb2));

                return convertView;
        }

        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            //mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }

    }

}
