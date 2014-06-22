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

package com.mapzen.data.osm.oauth;

import android.content.SharedPreferences;
import android.util.Log;
import com.mapzen.constants.MapzenConstants;



public class OAuthAccessTokenHolder implements MapzenConstants {

	private static final String TAG = OAuthAccessTokenHolder.class.getSimpleName();

    private static OAuthAccessTokenHolder instance;

    public static OAuthAccessTokenHolder getInstance() {
        if (instance == null) {
            instance = new OAuthAccessTokenHolder();
        }
        return instance;
    }

    private String accessTokenKey;
    private String accessTokenSecret;

    /**
     * Replies the access token key. null, if no access token key is currently set.
     *
     * @return the access token key
     */
    public String getAccessTokenKey() {
        return accessTokenKey;
    }

    /**
     * Sets the access token key. Pass in null to remove the current access token key.
     *
     * @param accessTokenKey the access token key
     */
    public void setAccessTokenKey(String accessTokenKey) {
        this.accessTokenKey = accessTokenKey;
    }

    /**
     * Replies the access token secret. null, if no access token secret is currently set.
     *
     * @return the access token secret
     */
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    /**
     * Sets the access token secret. Pass in null to remove the current access token secret.
     *
     * @param accessTokenSecret
     */
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public OAuthToken getAccessToken() {
        if (!containsAccessToken())
            return null;
        return new OAuthToken(accessTokenKey, accessTokenSecret);
    }

    /**
     * Sets the access token hold by this holder.
     *
     * @param accessTokenKey the access token key
     * @param accessTokenSecret the access token secret
     */
    public void setAccessToken(String accessTokenKey, String accessTokenSecret) {
        this.accessTokenKey = accessTokenKey;
        this.accessTokenSecret = accessTokenSecret;
    }

    /**
     * Sets the access token hold by this holder.
     *
     * @param token the access token. Can be null to clear the content in this holder.
     */
    public void setAccessToken(OAuthToken token) {
        if (token == null) {
            this.accessTokenKey = null;
            this.accessTokenSecret = null;
        } else {
            this.accessTokenKey = token.getKey();
            this.accessTokenSecret = token.getSecret();
        }
    }

    /**
     * Replies true if this holder contains an complete access token, consisting of an
     * Access Token Key and an Access Token Secret.
     *
     * @return true if this holder contains an complete access token
     */
    public boolean containsAccessToken() {
        return accessTokenKey != null && accessTokenSecret != null;
    }

    /**
     * Initializes the content of this holder from the Access Token managed by the
     * credential manager.
     *
     * @param pref the preferences. Must not be null.
     * @param cm the credential manager. Must not be null.
     * @throws IllegalArgumentException thrown if cm is null
     */
    public void loadFromPreferences(SharedPreferences settings) {
        accessTokenKey = settings.getString(ACCESS_TOKEN, null);
        accessTokenSecret = settings.getString(ACCESS_SECRET, null);
    }

    /**
     * Saves the content of this holder to the preferences
     */

    public void saveToPreferences(SharedPreferences settings) {
		// null means to clear the old values
		SharedPreferences.Editor editor = settings.edit();
		if (accessTokenKey == null) {
			editor.remove(ACCESS_TOKEN);
			Log.d(TAG, "Clearing OAuth Request Token");
		} else {
			editor.putString(ACCESS_TOKEN, accessTokenKey);
			Log.d(TAG, "Saving OAuth Request Token: " + accessTokenKey);
		}
		if (accessTokenSecret == null) {
			editor.remove(ACCESS_SECRET);
			Log.d(TAG, "Clearing OAuth Secret");
		} else {
			editor.putString(ACCESS_SECRET, accessTokenSecret);
			Log.d(TAG, "Saving OAuth Secret: " + accessTokenSecret);
		}
		editor.commit();

	}

    /**
     * Clears the content of this holder
     */
    public void clear() {
        accessTokenKey = null;
        accessTokenSecret = null;
    }
}
