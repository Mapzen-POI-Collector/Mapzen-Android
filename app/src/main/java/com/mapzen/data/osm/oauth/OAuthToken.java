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

import oauth.signpost.OAuthConsumer;

public class OAuthToken {

    /**
     * Creates an OAuthToken from the token currently managed by the {@see OAuthConsumer}.
     * 
     * @param consumer the consumer
     * @return the token
     */
    static public OAuthToken createToken(OAuthConsumer consumer) {
        return new OAuthToken(consumer.getToken(), consumer.getTokenSecret());
    }

    private String key;
    private String secret;

    /**
     * Creates a new token
     * 
     * @param key the token key
     * @param secret the token secret
     */
    public OAuthToken(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    /**
     * Creates a clone of another token
     * 
     * @param other the other token. Must not be null.
     * @throws IllegalArgumentException thrown if other is null
     */
    public OAuthToken(OAuthToken other) throws IllegalArgumentException {
        if (other == null)
        	throw new NullPointerException();
        this.key = other.key;
        this.secret = other.secret;
    }

    /**
     * Replies the token key
     * 
     * @return the token key
     */
    public String getKey() {
        return key;
    }

    /**
     * Replies the token secret
     * 
     * @return the token secret
     */
    public String getSecret() {
        return secret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((secret == null) ? 0 : secret.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OAuthToken other = (OAuthToken) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (secret == null) {
            if (other.secret != null)
                return false;
        } else if (!secret.equals(other.secret))
            return false;
        return true;
    }
}
