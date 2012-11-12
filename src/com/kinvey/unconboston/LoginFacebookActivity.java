package com.kinvey.unconboston;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;

public class LoginFacebookActivity extends Activity {

	/** the logging tag **/
	private static final String TAG = LoginFacebookActivity.class.getSimpleName();

	/** facebook app id **/
	//TODO: add shared facebook app
	private static final String FB_APP_ID = "430678296979907";
	
	/** facebook client **/
	private static Facebook facebook = new Facebook(FB_APP_ID);
	
	/** kinvey client **/
	private static KCSClient mKinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_facebook);
		
		mKinveyClient = ((MainApplication) getApplication()).getKinveyService();
	}
	

	public void submit(View view){
		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(
				LoginFacebookActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");
		
    	facebook.authorize(LoginFacebookActivity.this, 
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						// Close the progress dialog and toast success to the user
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						Toast.makeText(LoginFacebookActivity.this, "Logged in with Facebook.", 
								Toast.LENGTH_LONG).show();
						
						loginFacebookKinveyUser(progressDialog, facebook.getAccessToken());
						
					}

					@Override
					public void onFacebookError(FacebookError error) {
						error(progressDialog, error.getMessage());
					}

					@Override
					public void onError(DialogError e) {
						error(progressDialog, e.getMessage());
					}

					@Override
					public void onCancel() {
						Toast.makeText(LoginFacebookActivity.this, "FB login cancelled", 
								Toast.LENGTH_LONG).show();
					}
				});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {
	
		mKinveyClient.loginWithFacebookAccessToken(accessToken, new KinveyCallback<KinveyUser>() {
			
			@Override
			public void onFailure(Throwable e) {
				error(progressDialog, "Kinvey: " + e.getMessage());				
			}
			
			@Override
			public void onSuccess(KinveyUser u) {
                CharSequence text = "Welcome back," + u.getUsername() + ".";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                LoginFacebookActivity.this.startActivity(new Intent(LoginFacebookActivity.this, SessionsActivity.class));
                LoginFacebookActivity.this.finish();

				
			}
		});
		
	}

	protected void error(ProgressDialog progressDialog, String error) {
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
        
        Log.e(TAG, "Error logging in with facebook " + error);
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
	
	
}
