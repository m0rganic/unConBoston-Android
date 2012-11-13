package com.kinvey.unconboston;

import android.accounts.AccountManager;
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
import com.google.android.gms.common.AccountPicker;
import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;
import com.textuality.authorized.AuthorizedActivity;
import com.textuality.authorized.Response;
import com.textuality.authorized.ResponseHandler;

public class LoginSocialActivity extends AuthorizedActivity {

	/** the logging tag **/
	private static final String TAG = LoginSocialActivity.class.getSimpleName();

	/** facebook app id **/
	//TODO: add shared facebook app
	private static final String FB_APP_ID = "430678296979907";
	
	/** facebook client **/
	private static Facebook facebook = new Facebook(FB_APP_ID);
	
    /** OAuth 2.0 scope for writing a moment to the user's Google+ history. */
    static final String SCOPE_STRING = "oauth2:https://www.googleapis.com/auth/plus.me";
    private static final String PLUS_PEOPLE_ME = "https://www.googleapis.com/plus/v1/people/me";
	private static final int GPLAY_REQUEST_CODE = 782049854;
	
	/** kinvey client **/
	private static KCSClient mKinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_social);
		
		mKinveyClient = ((MainApplication) getApplication()).getKinveyService();
		
		
		if (facebook.isSessionValid()){
			startHomeActivity();
		} else if (super.getAuthToken() != null){
			startHomeActivity();
		}
	}
	


	public void submitFacebook(View view){
		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(
				LoginSocialActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");
		
    	facebook.authorize(LoginSocialActivity.this, 
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						// Close the progress dialog and toast success to the user
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						Toast.makeText(LoginSocialActivity.this, "Logged in with Facebook.", 
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
						Toast.makeText(LoginSocialActivity.this, "FB login cancelled", 
								Toast.LENGTH_LONG).show();
					}
				});
    }
	
	public void submitGoogle(View view) {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, 
                false, null, null, null, null);  
        startActivityForResult(intent, GPLAY_REQUEST_CODE);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        
    	if (requestCode == 32665 && resultCode == -1){
    		facebook.authorizeCallback(requestCode, resultCode, data);
    	} else if (requestCode == GPLAY_REQUEST_CODE && resultCode == RESULT_OK) {
            String account = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            get(PLUS_PEOPLE_ME, account, SCOPE_STRING, 
                    null, new ResponseHandler() {
                        

				@Override
                public void handle(Response response) {
                    if (response.status != 200) {
                        error(null, new String(response.body));
                        return;
                    }
                    loginGoogleKinveyUser();
                }

				
            });        
        }
    }
    
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {
	
		mKinveyClient.loginWithFacebookAccessToken(accessToken, new KinveyCallback<KinveyUser>() {
			
			@Override
			public void onFailure(Throwable e) {
				error(progressDialog, "Kinvey: " + e.getMessage());
				Log.e(TAG, "failed Kinvey facebook login", e);
			}
			
			@Override
			public void onSuccess(KinveyUser u) {
                startHomeActivity();
			}
		});
		
	}

	private void loginGoogleKinveyUser() {
		mKinveyClient.loginWithGoogleAuthToken(getAuthToken(), 
				new KinveyCallback<KinveyUser>() {
			
			public void onFailure(Throwable e) {
				error(null, "Kinvey: " + e.getMessage());
				Log.e(TAG, "failed Kinvey google login", e);
			};
			
			@Override
			public void onSuccess(KinveyUser r) {
				Log.d(TAG, "successfully logged in with google");
                startHomeActivity();
			}
		});
	}
	

	private void startHomeActivity() {
		Intent intent = new Intent(LoginSocialActivity.this, SessionsActivity.class);
		startActivity(intent);
        finish();
	}

	protected void error(ProgressDialog progressDialog, String error) {
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
        
        Log.e(TAG, "Error logging in with facebook " + error);
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginSocialActivity.this, MainActivity.class));

	}
	
	
}
