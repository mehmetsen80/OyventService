package com.oy.vent;

import com.google.gson.Gson;
import com.oy.vent.model.UserInfo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;

import org.apache.http.client.methods.HttpPost;


public abstract class BaseActivity extends Activity{

	protected static final String PREF_FILE_NAME = "OYVENTDATA";//hard disk file name
	/*protected static final int REQUEST_CODE_MAIN_CLASS = 111;//main activity
	protected static final int REQUEST_CODE_LOGIN_CLASS = 222;//login activity
	protected static final int REQUEST_CODE_REGISTER_CLASS = 333;	//register activity*/
	//protected final HttpPost httpPost = new HttpPost();//Creating JSON Parser object
	
	@Override
    protected void onStart() {
        super.onStart();        
        handleAnonymousUser();//first we lookup the anonymous user        
        UserInfo userInfo = getUserInfo();//if userInfo object exists on harddisk then skip to main Activity
        if(userInfo != null && userInfo.email != "")
             goToMainActivity();    
    }
	
	//go to the home-all local feeds screen
	protected void goToMainActivity() {
		Intent intent = new Intent(this,MainActivity.class);
		//startActivityForResult(intent, REQUEST_CODE_MAIN_CLASS);
		startActivity(intent);
		finish();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	handleAnonymousUser();        	
        	finish();
        }
		return false;
    }
	
	//save user info to hard disk
  	protected void saveUserInfo(UserInfo usr)
  	{
  		SharedPreferences sharedPref =  getSharedPreferences( PREF_FILE_NAME, MODE_PRIVATE);
  		SharedPreferences.Editor editor = sharedPref.edit();
  		Gson gson = new Gson();
  		String json = gson.toJson(usr);
  		editor.putString("userInfo", json);
  		editor.commit();
  	}
  	
  	//read user info from hard disk
  	protected UserInfo getUserInfo()
  	{		
  		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);		
  		Gson gson = new Gson();
  		String json = sharedPref.getString("userInfo", "");
  		UserInfo usr = gson.fromJson(json, UserInfo.class);		
  		return usr;
  	}
  	
  	//remove user info from hard disk
  	protected void removeUser()
  	{
  		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
  		sharedPref.edit().remove("userInfo").commit();
  	}
  	
  	//handle the anonymous user
  	protected void handleAnonymousUser(){ 
  		//if userInfo object exists on harddisk then skip to main Activity
          UserInfo userInfo = getUserInfo();
          if(userInfo != null){        	
          	if(userInfo.email.indexOf("anonymous@anonymous") > -1 && 
          			userInfo.username.indexOf("anonymous") > -1 && 
          			userInfo.password.indexOf("anonymous") > -1)
          		removeUser();
          }
  	}
  	
  	//go to the Login Screen
  	protected void goToLoginActivity(){
  		Intent intent = new Intent(this,LoginActivity.class);
  		//startActivityForResult(intent, REQUEST_CODE_LOGIN_CLASS);
  		startActivity(intent);
  		finish();
  	}
  	
  //go to the Register Screen
  	protected void goToRegisterActivity(){
  		Intent intent = new Intent(this,RegisterActivity.class);
  		//startActivityForResult(intent, REQUEST_CODE_REGISTER_CLASS);
  		startActivity(intent);
  		finish();
  	}
  	
  	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_MAIN_CLASS || 
				requestCode == REQUEST_CODE_LOGIN_CLASS ||
				requestCode == REQUEST_CODE_REGISTER_CLASS){			
			if(resultCode == 5)//if logged out
			{				
				removeUser();				
				goToLoginActivity();//and then go to login screen
			}			
			else if(resultCode == 6)//just exit the application
			{
				//Toast toast = Toast.makeText(getApplicationContext(), "resultCode 6",  Toast.LENGTH_LONG);
				//toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				//toast.show();
				
				handleAnonymousUser();
				finish();
				System.exit(0);
			}else{
				removeUser();
				goToLoginActivity();
			}		
		}
	}*/
}
