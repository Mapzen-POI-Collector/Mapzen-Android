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

import com.mapzen.constants.MapzenConstants;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/**
 * This class manages a set of OAuth parameters.
 * 
 */
public class OAuthParameters implements MapzenConstants {

    private String consumerKey;
    private String consumerSecret;
    private String requestTokenUrl;
    private String accessTokenUrl;
    private String authoriseUrl;
    
    /**
     * Replies a set of default parameters for a consumer accessing the standard OSM server
     * at http://api.openstreetmap.org/api
     * 
     * @return a set of default parameters
     */
    static public OAuthParameters createDefault() {
        OAuthParameters parameters = new OAuthParameters();
        parameters.setConsumerKey(CONSUMER_KEY);
        parameters.setConsumerSecret(CONSUMER_SECRET);
        parameters.setRequestTokenUrl(REQUEST_TOKEN_URL);
        parameters.setAccessTokenUrl(ACCESS_TOKEN_URL);
        parameters.setAuthoriseUrl(AUTHORIZATION_URL);
        return parameters;
    }


    public OAuthParameters() {}

    /**
     * Creates a clone of the parameters in <code>other</code>.
     * 
     * @param other the other parameters. Must not be null.
     * @throws IllegalArgumentException thrown if other is null
     */
    public OAuthParameters(OAuthParameters other) throws IllegalArgumentException{
        if (other == null)
        	throw new NullPointerException();
        this.consumerKey = other.consumerKey;
        this.consumerSecret = other.consumerSecret;
        this.accessTokenUrl = other.accessTokenUrl;
        this.requestTokenUrl = other.requestTokenUrl;
        this.authoriseUrl = other.authoriseUrl;
    }

    public String getConsumerKey() {
        return consumerKey;
    }
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }
    public String getConsumerSecret() {
        return consumerSecret;
    }
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
    public String getRequestTokenUrl() {
        return requestTokenUrl;
    }
    public void setRequestTokenUrl(String requestTokenUrl) {
        this.requestTokenUrl = requestTokenUrl;
    }
    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }
    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }
    public String getAuthoriseUrl() {
        return authoriseUrl;
    }
    public void setAuthoriseUrl(String authoriseUrl) {
        this.authoriseUrl = authoriseUrl;
    }

    /**
     * Builds an {@see OAuthConsumer} based on these parameters
     * 
     * @return the consumer
     */
    public OAuthConsumer buildConsumer() {
        return new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
    }
    
    public OAuthConsumer buildDefaultConsumer() {
    	return new DefaultOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * Builds an {@see OAuthProvider} based on these parameters and a OAuth consumer <code>consumer</code>.
     * 
     * @return the provider
     * @throws IllegalArgumentException thrown if consumer is null
     */
    public OAuthProvider buildProvider() throws IllegalArgumentException {
        
        return new CommonsHttpOAuthProvider(
                requestTokenUrl,
                accessTokenUrl,
                authoriseUrl
        );
    }
}
