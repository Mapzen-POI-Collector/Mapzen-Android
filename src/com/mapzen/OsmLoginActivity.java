/*Copyright (c) 2011-2012, Cloudmade
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/

package com.mapzen;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mapzen.R;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.osm.oauth.OAuthAccessTokenHolder;
import com.mapzen.data.osm.oauth.OAuthParameters;
import com.mapzen.data.osm.oauth.OAuthToken;

import junit.framework.Assert;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OsmLoginActivity extends Activity implements MapzenConstants {

	private static final String TAG = OsmLoginActivity.class.getSimpleName();

	private static final OAuthParameters oauthParams = OAuthParameters
			.createDefault();
	private static OAuthProvider provider = oauthParams.buildProvider();
	private static OAuthConsumer consumer = oauthParams.buildConsumer();
	private static OAuthToken oauthAccessToken;
	private static OAuthAccessTokenHolder accessTokenHolder = OAuthAccessTokenHolder
			.getInstance();
	private static OauthValidationTask oauthCredentaialsValidationTask;
	
	private static String token;
	private static String secret;
	
	private static final int DIALOG_SD_CARD_UNAVAILABLE = 1;
	private static final int DIALOG_NETWORK_PROBLEMS = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_screen);
		
		// verifing OAUTH credentials here (in background)
		OAuthAccessTokenHolder oauthTokenHolder = OAuthAccessTokenHolder.getInstance();
		oauthTokenHolder.loadFromPreferences(Mapzen.getSharedPreferences());
		
		//validate oauth credentials async
		oauthCredentaialsValidationTask = new OauthValidationTask();
		oauthCredentaialsValidationTask.execute(oauthTokenHolder);
	
		if (!isSDCardAvailable()) {
			showDialog(DIALOG_SD_CARD_UNAVAILABLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		Uri uri = this.getIntent().getData();

		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			Intent i = new Intent(this, MapActivity.class);

			try {
				if ((token != null && secret != null)) {
					consumer.setTokenWithSecret(token, secret);
				}

				String otoken = uri.getQueryParameter(OAuth.OAUTH_TOKEN);
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				//Assert.assertEquals(otoken, consumer.getToken());

				provider.retrieveAccessToken(consumer, verifier);
				token = consumer.getToken();
				secret = consumer.getTokenSecret();
				oauthAccessToken = OAuthToken.createToken(consumer);
				accessTokenHolder.setAccessToken(oauthAccessToken);
				accessTokenHolder.saveToPreferences(Mapzen.getSharedPreferences());
				i.putExtra(ACCESS_TOKEN, token);
				i.putExtra(ACCESS_SECRET, secret);
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				/*
				 * we either authenticated and have the token & secret or not,
				 * but we're going back
				 */
				if(validateOsmOauthCredentials(token, secret) == 1) {
					startActivity(new Intent(OsmLoginActivity.this, MapActivity.class));
				} else 
					showDialog(DIALOG_NETWORK_PROBLEMS);
			}

		}
	}
	
	@Override
	protected void onPause() {
		//cancel oauth validation task in case if activity is paused.
		oauthCredentaialsValidationTask.cancel(true);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(EXIT_APP_ACTIVITY_RESULT);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final AlertDialog dialog;
		AlertDialog.Builder builder;
		
		switch (id) {
		case DIALOG_SD_CARD_UNAVAILABLE:
			builder = new AlertDialog.Builder(this)
			.setTitle("SD Card is unavailable")
			.setMessage("Please mount SD card to start the application")
			.setPositiveButton(R.string.dialog_ok_button, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					finish();	
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_NETWORK_PROBLEMS:
			builder = new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_err_ms_no_osm_connection_title)
			.setMessage(R.string.dialog_err_msg_no_osm_connection)
			.setPositiveButton(R.string.dialog_check_network_settings, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
					finish();
				}
			});
			dialog = builder.create();
			break;
		default: 
			dialog= null; 
			break;
		}
		return dialog;
	}

	private String getAuthorizationUrl() {
		String authUrl = null;
		try {
			authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
			token = consumer.getToken();
			secret = consumer.getTokenSecret();
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
			showDialog(DIALOG_NETWORK_PROBLEMS);
		}

		return authUrl;
	}

	private void authorizeApplicationInOSM(String authUrl) {
		try {
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse(authUrl)));
		} catch (NullPointerException e) {
			Log.e(TAG, "Authorization URL is null");
			e.printStackTrace();
		}
	}

	/**
	 * Validates oauth credentials specified here with default OSM server, specified in {@see MapzenConstants}
	 * 
	 * @param oauthToken OAuth token
	 * @param oauthSecret OAuth secret
	 * @return result of validation:
	 * 1 - valid;  
	 * 0 - invalid; 
	 * -1 - network connection problem.
	 */
	//TODO: move this method to other class (osmapi, osmfacade, new class??)
	public int validateOsmOauthCredentials(String oauthToken,
			String oauthSecret) {
		int result = 0;
		if (oauthSecret != null && oauthToken != null) {
			HttpGet get = new HttpGet(OSM_SERVER_ADDRESS
					+ "/api/0.6/user/details");
			try {
				consumer.setTokenWithSecret(oauthToken, oauthSecret);
				consumer.sign(get);
			} catch (OAuthException e) {
				Log.e(TAG, "Exception while signing request", e);
			}

			HttpClient client = new DefaultHttpClient();
			org.apache.http.HttpResponse response;

			try {
				response = client.execute(get);
				int statusCode = response.getStatusLine().getStatusCode();
				final String reason = response.getStatusLine()
						.getReasonPhrase();
				response.getEntity().consumeContent();
				if (statusCode == 200) {
					Log.i(TAG, "OAuth credentials are valid");
					result = 1;
				} else
					Log.w(TAG, "Oauth credentials are NOT valid! Status code:"
							+ statusCode + ", reason:" + reason);
			} 
			catch (IOException e) {
				Log.e(TAG, "There is a network connection problem while validating OAuth credentials", e);
				result = -1;
			}
		}
		return result;
	}

	/***************************************************************
	 * Event listeners
	 ***************************************************************/
	private OnClickListener loginButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Intent i = OsmLoginActivity.this.getIntent();
			if (i.getData() == null) {
				String authUrl = OsmLoginActivity.this.getAuthorizationUrl();
				OsmLoginActivity.this.authorizeApplicationInOSM(authUrl);
			}
		}
	};

	private OnClickListener registerAtOsmOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri
					.parse(OSM_SERVER_ADDRESS + "/user/new"));
			startActivity(i);
		}
	};
	
	
	/*****************************************************************
	 * Helper methods
	 ****************************************************************/
	public static boolean isSDCardAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}
	
	/*****************************************************************
	 * Inner classes
	 ****************************************************************/
	private class OauthValidationTask extends AsyncTask<OAuthAccessTokenHolder, Void, Integer> {

		@Override
		protected Integer doInBackground(OAuthAccessTokenHolder... params) {
			String savedUserToken = params[0].getAccessTokenKey();
			String savedUserSecret = params[0].getAccessTokenSecret();
							
			 int result = 
				 validateOsmOauthCredentials(savedUserToken, savedUserSecret);
					
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case -1:
				showDialog(DIALOG_NETWORK_PROBLEMS);
				break;
				
			case 0:
				OsmLoginActivity.this.setContentView(R.layout.login);
				Button loginButton = (Button) findViewById(R.id.button_osm_login);
				loginButton.setOnClickListener(loginButtonOnClickListener);
				TextView registerAtOsmLink = (TextView) findViewById(R.id.osm_login_page_text5);
				registerAtOsmLink.setOnClickListener(registerAtOsmOnClickListener);
				break;
				
			case 1:
				OsmLoginActivity.this.finish();
				startActivity(new Intent(OsmLoginActivity.this, MapActivity.class));
				break;
				
			default:
				break;
			}	
		}
	}
	
}
