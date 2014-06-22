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

package com.mapzen.io.exceptions;

import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an exception thrown by the OSM API if JOSM tries to update or delete a primitive
 * which is already deleted on the server.
 *
 */
public class OsmApiPrimitiveGoneException extends OsmApiException{

    private static final long serialVersionUID = 8742444504221169554L;

    /**
     * The regexp pattern for the error header replied by the OSM API
     */
    static public final String ERROR_HEADER_PATTERN = "The (\\S+) with the id (\\d+) has already been deleted";
    /** the type of the primitive which is gone on the server */
    private String type;
    /** the id of the primitive */
    private long id;

    public OsmApiPrimitiveGoneException(String errorHeader, String errorBody) {
        super(HttpURLConnection.HTTP_GONE, errorHeader, errorBody);
        if (errorHeader == null) return;
        Pattern p = Pattern.compile(ERROR_HEADER_PATTERN);
        Matcher m = p.matcher(errorHeader);
        if (m.matches()) {
            type = m.group(1);
            id = Long.parseLong(m.group(2));
        }
    }

    /**
     * Replies true if we know what primitive this exception was thrown for
     *
     * @return true if we know what primitive this exception was thrown for
     */
    public boolean isKnownPrimitive() {
        return id > 0 && type != null;
    }

    /**
     * Replies the type of the primitive this exception was thrown for. null,
     * if the type is not known.
     *
     * @return the type of the primitive this exception was thrown for
     */
    public String getPrimitiveType() {
        return type;
    }

    /**
     * Replies the id of the primitive this exception was thrown for. 0, if
     * the id is not known.
     *
     * @return the id of the primitive this exception was thrown for
     */
    public long getPrimitiveId() {
        return id;
    }
}
