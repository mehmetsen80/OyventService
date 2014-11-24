package com.oy.vent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oy.vent.model.UserInfo;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity{
	
	private EditText txtEmail = null;
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	private TextView txtLogin = null;
	private TextView txtBrowseNow = null;
	private Button btnRegister = null;
	private static final String TAG = "RegisterActivity.java";//log tag
	private static final String JSON_URL = "http://oyvent.com/ajax/Register.php";//json register url
	private ProgressDialog pDialog;// Progress Dialog	
	
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_register);       
        
        txtEmail = (EditText)findViewById(R.id.txtEmail);        
        txtUsername = (EditText) findViewById(R.id.txtUsername);        
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtEmail.setText("");
        txtUsername.setText("");
        txtPassword.setText("");		
        
        txtLogin = (TextView)findViewById(R.id.loginnow);
        txtLogin.setClickable(true);
        txtLogin.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {        		
        		goToLoginActivity();
        	}        
        });
        
        txtBrowseNow = (TextView)findViewById(R.id.browsenow);
        txtBrowseNow.setClickable(true);
        txtBrowseNow.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) { 
        		UserInfo userInfo = new UserInfo();
        		userInfo.userID = 0d;
        		userInfo.email = "anonymous@anonymous";
        		userInfo.username = "anonymous";
        		userInfo.password = "anonymous";
        		saveUserInfo(userInfo);
        		goToMainActivity();
        	}        
        });
        
     
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setClickable(true);
        btnRegister.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {        		
        		goToMainActivity();
        		new RegisterFeed().execute(txtEmail.getText().toString(),txtUsername.getText().toString(),txtPassword.getText().toString());
        	}        
        });       
        
        pDialog = new ProgressDialog(RegisterActivity.this);
	}	
		
	
	//register json feed task
	class RegisterFeed extends AsyncTask<String, String, UserInfo> {		
		
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog.setMessage("Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		/**
		 * getting Feeds JSON
		 * */
		@Override
		protected UserInfo doInBackground(String... args) {

            UserInfo userInfo = null;
			try{

                HttpPost httpPost = new HttpPost(JSON_URL);
                httpPost.addHeader("Accept", "application/json");
                HttpClient httpClient = new DefaultHttpClient();

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setCharset(MIME.UTF8_CHARSET);
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                builder.addTextBody("processType", "SIGNUPUSER", ContentType.create("text/plain", MIME.UTF8_CHARSET));
                builder.addTextBody("email", args[0], ContentType.create("text/plain", MIME.UTF8_CHARSET));
                builder.addTextBody("username", args[1], ContentType.create("text/plain", MIME.UTF8_CHARSET));
                builder.addTextBody("password", args[2], ContentType.create("text/plain", MIME.UTF8_CHARSET));
                httpPost.setEntity(builder.build());
                Log.d(TAG, "Executing register request: " + httpPost.getRequestLine());
                userInfo = (UserInfo) httpClient.execute(httpPost, new DataPostResponseHandler());

            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

			
			return userInfo;
		}

        private class DataPostResponseHandler implements ResponseHandler<Object> {

            private InputStream is = null;
            private String json = null;

            @Override
            public UserInfo handleResponse(HttpResponse response)
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

                Log.d("Register Data JSON Result: ", "> " + json);

                UserInfo userInfo = new UserInfo();
                userInfo.message = "Invalid register attempt!";
                userInfo.success =  false;

                try {
                    JSONArray results = new JSONArray(json);

                    if (results != null) {
                        // looping through All feeds
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject c = results.getJSONObject(i);
                            userInfo.success = c.getBoolean("success");
                            userInfo.message = c.getString("message");
                            //if successfully logged in get all the user info
                            if(userInfo.success)
                            {
                                userInfo.userID = c.getDouble("userID");
                                userInfo.username = c.getString("username");
                                userInfo.email = c.getString("email");
                                userInfo.lastlogindate = c.getString("lastlogindate");
                                userInfo.signupdate = c.getString("signupdate");
                            }
                            else
                            {
                                Log.d(TAG,"Success: "+userInfo.success+"  Error:"+userInfo.message);
                            }
                        }
                    }else{
                        Log.e("DataArray: ", "null");
                    }

                } catch (JSONException e) {
                    Log.e(TAG,e.getMessage());
                }
                return userInfo;
            }
        }
		
		/**
		* After completing background task Dismiss the progress dialog
		* **/
		@Override
		protected void onPostExecute(final UserInfo userInfo) {
			// dismiss the dialog after getting user info
			pDialog.dismiss();
			
			if(userInfo.success)
			{
				saveUserInfo(userInfo);
				goToMainActivity();
			}
			else{						
				Toast toast = Toast.makeText(getApplicationContext(), userInfo.message,  Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();	
			}			
		}	
		
	}
	
}
