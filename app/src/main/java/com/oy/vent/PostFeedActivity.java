package com.oy.vent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.oy.vent.utilities.GPSTracker;
import com.oy.vent.utilities.ImageUtil;
import com.oy.vent.utilities.PhotoActionType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostFeedActivity extends CameraActivity {

    private static String TAG = PostFeedActivity.class.getName();
    // Activity result key for camera
    private static final int REQUEST_TAKE_PHOTO = 11111;
    // Code for our image picker select action.
    private static final int IMAGE_PICKER_SELECT = 22222;
    private ImageView mImageView;
    // Progress Dialog
    private ProgressDialog pDialog = null;
    //GPS Tracker to get Geo coordinates
    private GPSTracker gpsTracker;
    private static final String URL_AJAX = "http://oyvent.com/ajax/PhotoHandlerMobile.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        try {

            // Set the image view
            mImageView = (ImageView)findViewById(R.id.mImageView);

            //setup GPS Tracker to get the geo coordinates
            if(gpsTracker == null) {
                gpsTracker = new GPSTracker(this);
                if (!gpsTracker.canGetLocation()) {
                    gpsTracker.showSettingsAlert();
                }
            }
            //dialog
            if(pDialog == null)
                pDialog = new ProgressDialog(this);

            //take picture image button
            final ImageButton mTakePictureButton = (ImageButton)findViewById(R.id.btnAddPhoto);
            mTakePictureButton.setClickable(true);
            mTakePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });


            // Reference to picker button.
            final ImageButton mPickPhotoButton = (ImageButton)findViewById(R.id.btnBrowsePhoto);
            mPickPhotoButton.setClickable(true);
            mPickPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, IMAGE_PICKER_SELECT);
                }
            });


            //Uri selectedImage = getIntent().getData();
            /*Bundle extras = getIntent().getExtras();
            Uri selectedImage = (Uri) extras.get("output");
            String posttype = (String) extras.get("POSTTYPE");
            File file = null;
            if(posttype.equals(PhotoActionType.CAPTURE.toString())) {
                Bitmap bitmap = setFullImageFromFilePath(selectedImage.getPath(), mImageView);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                file = new File(selectedImage.getPath());
                try {
                    file.createNewFile();
                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }else if(posttype.equals(PhotoActionType.PICK.toString())){
                setFullImageFromFilePath(selectedImage.getPath(), mImageThumb);
                file = new File(getImagePathUri(selectedImage));

            }else{
                Toast.makeText(PostFeedActivity.this, "Invalid picture, please try again!", Toast.LENGTH_LONG)
                        .show();
                finish();
                return;
            }


            new LoadData().execute(file);//TODO: call this when button pushed*/

        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }

    }

    /**
     * Start the camera by dispatching a camera intent.
     */
    protected void dispatchTakePictureIntent() {

        // Check if there is a camera.
       // Context context =  this;
        PackageManager packageManager = getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false){
            Toast.makeText(this, "This device does not have a camera!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Camera exists? Then proceed...
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        CameraActivity activity = this;
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go.
            // If you don't do this, you may get a crash in some devices.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,ex.getMessage());
                Toast toast = Toast.makeText(PostFeedActivity.this, "There was a problem saving the photo...", Toast.LENGTH_SHORT);
                toast.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri fileUri = Uri.fromFile(photoFile);
                activity.setCapturedImageURI(fileUri);
                activity.setCurrentPhotoPath(fileUri.getPath());
                activity.setPhotoActionType(PhotoActionType.CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, activity.getCapturedImageURI());
                startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * The activity returns with the photo.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            //addPhotoToGallery();
            CameraActivity activity = this;

            Bitmap bitmap = setFullImageFromFilePath(activity.getCurrentPhotoPath(), mImageView);// Show the full sized image.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File file = new File(activity.getCurrentPhotoPath());
            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
            new LoadData().execute(file);

            /*Intent postPictureIntent = new Intent(this, PostFeedActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable(MediaStore.EXTRA_OUTPUT, activity.getCapturedImageURI());
            extras.putString("POSTTYPE",PhotoActionType.CAPTURE.toString());
            postPictureIntent.putExtras(extras);
            startActivityForResult(postPictureIntent, POST_PICTURE);*/



        }
        else if (requestCode == IMAGE_PICKER_SELECT  && resultCode == Activity.RESULT_OK) {


            Uri selectedImage = data.getData();
            File file = new File(getImagePathUri(selectedImage));
            CameraActivity activity = this;
            activity.setCurrentPhotoPath("file:" + selectedImage.getPath());
            activity.setCapturedImageURI(selectedImage);
            activity.setPhotoActionType(PhotoActionType.PICK);
            setFullImageFromFilePath(selectedImage.getPath(),mImageView);

            /*Intent postPictureIntent = new Intent(this, PostFeedActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable(MediaStore.EXTRA_OUTPUT, activity.getCapturedImageURI());
            extras.putString("POSTTYPE",PhotoActionType.PICK.toString());
            postPictureIntent.putExtras(extras);
            startActivityForResult(postPictureIntent, POST_PICTURE);*/


            if(file.exists() && file.getAbsolutePath() != null && file.getAbsolutePath() != "")
            {
                new LoadData().execute(file);
            }else
            {
                Toast.makeText(this, "Picture Not Uploaded",Toast.LENGTH_LONG).show();
                mImageView.setImageBitmap(null);
                mImageView.setImageDrawable(null);
                activity.setCurrentPhotoPath(null);
                activity.setCapturedImageURI(null);
            }

            // Bitmap bitmap = getBitmapFromCameraData(data,getActivity());


        }
        else {
            Toast.makeText(this, "Image Capture Failed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Creates the image file to which the image must be saved.
     * @return
     * @throws IOException
     */
    protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /*File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES,R.string.album_name);*/

        File storageDir = getAlbumDir();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        CameraActivity activity = this;
        activity.setCurrentPhotoPath("file:" + image.getAbsolutePath());
        return image;
    }




    //get the album directory
    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            // storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getString(R.string.album_name));

            storageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ),
                    getString(R.string.album_name)
            );

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d(TAG, "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }


    /**
     * Add the picture to the photo gallery.
     * Must be called on all camera images or they will
     * disappear once taken.
     */
    protected void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        CameraActivity activity = this;
        File f = new File(activity.getCurrentPhotoPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public String getImagePathUri(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        //Cursor cursor = managedQuery(uri, projection, null, null, null);
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }


    private Bitmap setFullImageFromFilePath(String imagePath, ImageView imageView) {

        Bitmap bitmap = null;

        try {
            //first check out the image path
            if (imagePath == null) {
                Log.e(TAG, "Image path is null!");
                return null;
            }

            // Get the dimensions of the View
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = 1;
            if ((targetW > 0) || (targetH > 0)) {
                scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            }

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            // bmOptions.inPurgeable = true;
            bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

            //rotate image
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.e("ExifInteface .........", "rotation ="+orientation);
            Log.d(TAG,"orientation=" + orientation);

            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180))
                bitmap = ImageUtil.RotateBitmap(bitmap, 180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                bitmap = ImageUtil.RotateBitmap(bitmap, 90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                bitmap = ImageUtil.RotateBitmap(bitmap, 270);

            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            Log.e(TAG,"setPic Error:"+e.getMessage());
        }

        return bitmap;
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

    class LoadData extends AsyncTask<File, Void, File> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try{
                pDialog.setMessage("Sending...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }catch(Exception e){
                Log.e(TAG,e.getMessage());
            }
        }

        protected File doInBackground(File... files) {

            File file = null;
            try {
                file = files[0];
                uploadData(file);
                return file;

            }catch(Exception e){
                Log.e(TAG,e.getMessage());
            }

            return file;
        }

        @Override
        protected void onPostExecute(final File thumbnail) {

            try {
                // setResult(CROPPED_IMAGE);
                // finish();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        pDialog.dismiss();
                        if (thumbnail == null) {
                            Toast.makeText(PostFeedActivity.this, "Image is null!", Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        //mImageView.setImageBitmap(thumbnail.getAbsolutePath());
                        Toast.makeText(PostFeedActivity.this, "Data Sent!", Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }catch(Exception e){
                Log.e(TAG,e.getMessage());
            }
        }
    }// end of LoadPicture class


    public void uploadData(File file) {

        try {
            HttpPost httpPost = new HttpPost(URL_AJAX);
            httpPost.addHeader("Accept", "application/json");
            HttpClient httpClient = new DefaultHttpClient();

            if(file==null){
                 Toast.makeText(PostFeedActivity.this, "Invalid File, please try again!", Toast.LENGTH_LONG).show();
                 return;
            }

            if (!gpsTracker.canGetLocation()){
                 gpsTracker.showSettingsAlert();
                 Toast.makeText(PostFeedActivity.this, "Invalid Geo coordinates, please enable your GPS and try again!", Toast.LENGTH_LONG)
                           .show();
                 return;
            }

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            FileBody fileBody = (file.exists())?new FileBody(file):null;
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(MIME.UTF8_CHARSET);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", fileBody);
            builder.addTextBody("processType", "UPLOADPHOTO", ContentType.create("text/plain", MIME.UTF8_CHARSET));
            builder.addTextBody("userID", "12", ContentType.create("text/plain", MIME.UTF8_CHARSET));
            builder.addTextBody("albumID", "2", ContentType.create("text/plain", MIME.UTF8_CHARSET));
            builder.addTextBody("latitude", Double.toString(latitude), ContentType.create("text/plain", MIME.UTF8_CHARSET));
            builder.addTextBody("longitude", Double.toString(longitude), ContentType.create("text/plain", MIME.UTF8_CHARSET));
            httpPost.setEntity(builder.build());
            Log.d(TAG,"Executing request: " + httpPost.getRequestLine());
            httpClient.execute(httpPost, new DataUploadResponseHandler());

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    private class DataUploadResponseHandler implements ResponseHandler<Object> {

        private InputStream is = null;
        private String json = null;

        @Override
        public Object handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {

            HttpEntity r_entity = response.getEntity();
            is = r_entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();

            Log.d("Post Data JSON Result: ", "> " + json);

            try {
                JSONArray dataArray = new JSONArray(json);

                if (dataArray != null) {
                    // looping through All feeds
                    Log.d(TAG,"DataArray Length: "+dataArray.length());
                    for (int i = 0; i < dataArray.length(); i++) {

                        JSONObject c = dataArray.getJSONObject(i);
                        Boolean success = c.getBoolean("success");
                        String error = c.getString("error");
                        if(success)
                        {
							/*String userID = c.getString("userID");
							String username = c.getString("username");
							Long filesize = c.getLong("filesize");
							String filename = c.getString("filename");
							String fileserverpath = c.getString("fileserverpath");
							String filehttppath = c.getString("filehttppath");

							Log.d(TAG,"userID:"+userID+ " username:"+username+" filesize:"+filesize+" filename:"+filename+" fileserverpath:"+fileserverpath+" filehttppath:"+filehttppath);
							*/


                            // setResult(RESULT_CODE_SEND_IMAGE);
                            //finish();

                            Log.d(TAG,"Yes!");

                        }else
                        {
                            Log.d(TAG,"Success: "+success+"  Error:"+error);
                        }

                    }
                }else{
                    Log.d("DataArray: ", "null");
                }

            } catch (JSONException e) {
                Log.e(TAG,e.getMessage());
            }
            return null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return false;
    }


}
