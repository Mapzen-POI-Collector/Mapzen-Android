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

public class OsmApiException extends OsmTransferException {

    private static final long serialVersionUID = 4211876368286271255L;

    private int responseCode;
    private String errorHeader;
    private String errorBody;
    private String accessedUrl;

    public OsmApiException() {
        super();
    }

    public OsmApiException(int responseCode, String errorHeader, String errorBody) {
        this.responseCode = responseCode;
        this.errorHeader = errorHeader;
        this.errorBody = errorBody;
    }

    public OsmApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public OsmApiException(String message) {
        super(message);
    }

    public OsmApiException(Throwable cause) {
        super(cause);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getErrorHeader() {
        return errorHeader;
    }

    public void setErrorHeader(String errorHeader) {
        this.errorHeader = errorHeader;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResponseCode=")
        .append(responseCode);
        if (errorHeader != null && errorBody != null && !errorBody.trim().equals("")) {
            sb.append(", Error Header=<")
            .append(errorHeader)
            .append(">");
        }
        if (errorBody != null && !errorBody.trim().equals("")) {
            errorBody = errorBody.trim();
            if(!errorBody.equals(errorHeader)) {
                sb.append(", Error Body=<")
                .append(errorBody)
                .append(">");
            }
        }
        return sb.toString();
    }

    /**
     * Replies a message suitable to be displayed in a message dialog
     *
     * @return a message which is suitable to be displayed in a message dialog
     */
    public String getDisplayMessage() {
        StringBuilder sb = new StringBuilder();
        if (errorHeader != null) {
            sb.append(errorHeader);
            sb.append("(Code="+responseCode+")");
        } else if (errorBody != null && !errorBody.trim().equals("")) {
            errorBody = errorBody.trim();
            sb.append(errorBody);
            sb.append("(Code=" + responseCode + ")");
        } else {
            sb.append("The server replied an error with code " + responseCode + ".");
        }
        return sb.toString();
    }

    public void setAccessedUrl(String url) {
        this.accessedUrl = url;
    }

    public String getAccessedUrl() {
        return accessedUrl;
    }
}
