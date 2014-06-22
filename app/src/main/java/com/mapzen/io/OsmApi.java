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

package com.mapzen.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.http.client.methods.HttpUriRequest;

import android.util.Log;

import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.osm.Changeset;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.io.exceptions.ChangesetClosedException;
import com.mapzen.io.exceptions.OsmApiException;
import com.mapzen.io.exceptions.OsmApiPrimitiveGoneException;
import com.mapzen.io.exceptions.OsmTransferCancelledException;
import com.mapzen.io.exceptions.OsmTransferException;

/**
 * Class that encapsulates the communications with the OSM API.
 *
 * All interaction with the server-side OSM API should go through this class.
 *
 * It is conceivable to extract this into an interface later and create various
 * classes implementing the interface, to be able to talk to various kinds of
 * servers.
 *
 */
public class OsmApi extends OsmConnection implements MapzenConstants {

    private final static String TAG = OsmApi.class.getSimpleName();

    private static OsmApi instance;

    /**
     * replies the {@see OsmApi} for the URL given by the preference
     * <code>osm-server.url</code>
     *
     * @return the OsmApi
     * @exception IllegalStateException
     *                thrown, if the preference <code>osm-server.url</code> is
     *                not set
     *
     */
    static public OsmApi getOsmApi() {
        if (instance == null) {
            instance = new OsmApi();
        }
        return instance;
    }

    private OsmApi() {}

    /**
     * Object describing current changeset.
     * MUST BE open while submiting data to server
     */
    private Changeset changeset;

    private StringWriter swriter = new StringWriter();
    private OsmWriter osmWriter = new OsmWriter(new PrintWriter(swriter));


    private String toOsmChangeXml(OsmNode node, String action) {
        swriter.getBuffer().setLength(0);
        osmWriter.setChangeset(changeset);
        osmWriter.osmChangeHeader();
        osmWriter.osmChangeActionOpen(action);
        osmWriter.visit(node);
        osmWriter.osmChangeActionClose(action);
        osmWriter.osmChangeFooter();
        osmWriter.out.flush();
        Log.d(TAG, swriter.toString());
        return swriter.toString();
    }
    /**
     * Makes an XML string from an OSM primitive. Uses the OsmWriter class.
     * @return XML string
     */
    private String toXml(OsmNode node) {
        swriter.getBuffer().setLength(0);
        osmWriter.setChangeset(changeset);
        osmWriter.header();
        osmWriter.visit(node);
        osmWriter.footer();
        osmWriter.out.flush();
        Log.d(TAG, swriter.toString());
        return swriter.toString();
    }

    /**
     * Makes an XML string from an OSM primitive. Uses the OsmWriter class.
     * @return XML string
     */
    private String toXml(Changeset s) {
        swriter.getBuffer().setLength(0);
        osmWriter.header();
        osmWriter.visit(s);
        osmWriter.footer();
        osmWriter.out.flush();
        return swriter.toString();
    }

    /**
     * Returns the base URL for API requests, including the negotiated version
     * number.
     *
     * @return base URL string
     */
    public String getBaseUrl() {
        String url = OSM_API_BASE_URL + "/" + OSM_API_VERSION + "/";
        return url;
    }

    /**
     * Creates an OSM node on the server. The OsmNode object passed in
     * is modified by giving it the server-assigned id.
     *
     * @param node
     *            the Node
     * @throws OsmTransferException
     *             if something goes wrong
     */
    public void createNode(OsmNode node) throws OsmTransferException {
        String ret = "";
        try {
            ensureValidChangeset();
            ret = sendRequest("PUT", "node/create", toXml(node));
            node.setOsmIdAndVersion(Long.parseLong(ret.trim()), 1);
            node.setChangesetId(getChangeset().getId());
        } catch (NumberFormatException e) {
            throw new OsmTransferException(
                    "Unexpected format of ID replied by the server. Got ''"
                            + ret + "''.");
        }
    }

    /**
     * Modifies an OSM node on the server.
     *
     * @param node
     *            the primitive. Must not be null.

     * @throws OsmTransferException
     *             if something goes wrong
     */
    public void modifyNode(OsmNode node) throws OsmTransferException {
        String ret = null;
        try {
            ensureValidChangeset();
            // normal mode (0.6 and up) returns new object version.
            ret = sendRequest("PUT", "node/" + node.getId(), toXml(node));
            node.setOsmIdAndVersion(node.getId(), Integer.parseInt(ret.trim()));
            node.setChangesetId(getChangeset().getId());
        } catch (NumberFormatException e) {
            throw new OsmTransferException(
                    "Unexpected format of new version of modified primitive ''"
                            + node.getId() + "''. Got ''" + ret + "''.");
        }
    }

    /**
     * Deletes an OSM node on the server.
     *
     * @param osm
     *            the node
     * @throws OsmTransferException
     *             if something goes wrong
     */
    public void deleteNode(OsmNode node) throws OsmTransferException {
        ensureValidChangeset();
        //String ret = null;
        //ret = sendRequest("DELETE", "node/" + node.getId(), toXml(node));
        // can't use a the individual DELETE method in the 0.6 API. Java doesn't
        // allow
        // submitting a DELETE request with content, the 0.6 API requires it,
        // however. Falling back
        // to diff upload.
        uploadDiff(node);


    }

    /**
     * Creates a new changeset based on the keys in <code>changeset</code>. If
     * this method succeeds, changeset.getId() replies the id the server
     * assigned to the new changeset
     *
     * The changeset must not be null, but its key/value-pairs may be empty.
     *
     * @param changeset
     *            the changeset toe be created. Must not be null.
     * @param progressMonitor
     *            the progress monitor
     * @throws OsmTransferException
     *             signifying a non-200 return code, or connection errors
     * @throws IllegalArgumentException
     *             thrown if changeset is null
     */
    public void openChangeset(Changeset changeset) throws OsmTransferException {
        if (changeset == null)
            throw new NullPointerException("changset is null");

        String ret = "";
        try {
            ret = sendRequest("PUT", "changeset/create", toXml(changeset));
            changeset.setId(Integer.parseInt(ret.trim()));
            changeset.setOpen(true);
        } catch (NumberFormatException e) {
            throw new OsmTransferException(
                    "Unexpected format of ID replied by the server. Got ''"
                            + ret + "''.");
        }
        Log.d(TAG, String.format("Successfully opened changeset {0}", changeset
                .getId()));
    }

    /**
     * Uploads a list of changes in "diff" form to the server.
     *
     * @param osm
     * @return processed node
     * @throws OsmTransferException
     *             if something is wrong
     */
    public void uploadDiff(OsmNode osm)
            throws OsmTransferException {
        try {
            if (changeset == null)
                throw new OsmTransferException(
                        String.format("No changeset present for diff upload."));

            String diffUploadRequest = toOsmChangeXml(osm, "delete");

            String diffUploadResponse = sendRequest("POST", "changeset/"
                    + changeset.getId() + "/upload", diffUploadRequest);

        } catch (OsmTransferException e) {
            throw e;
        }
    }

    private void sleepAndListen(int retry) throws OsmTransferCancelledException {
        System.out.print("Waiting 10 seconds ... ");
        for (int i = 0; i < 10; i++) {
            if (cancel)
                throw new OsmTransferCancelledException();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("OK - trying again.");
    }

    private String sendRequest(String requestMethod, String urlSuffix,
            String requestBody) throws OsmTransferException {
        return sendRequest(requestMethod, urlSuffix, requestBody, true);
    }



    /**
     * Generic method for sending requests to the OSM API.
     *
     * This method will automatically re-try any requests that are answered with
     * a 5xx error code, or that resulted in a timeout exception from the TCP
     * layer.
     *
     * @param requestMethod
     *            The http method used when talking with the server.
     * @param urlSuffix
     *            The suffix to add at the server url, not including the version
     *            number, but including any object ids (e.g.
     *            "/way/1234/history").
     * @param requestBody
     *            the body of the HTTP request, if any.
     * @param monitor
     *            the progress monitor
     * @param doAuthenticate
     *            set to true, if the request sent to the server shall include
     *            authentication credentials;
     *
     * @return the body of the HTTP response, if and only if the response code
     *         was "200 OK".
     * @exception OsmTransferException
     *                if the HTTP return code was not 200 (and retries have been
     *                exhausted), or rewrapping a Java exception.
     */
    private String sendRequest(String requestMethod, String urlSuffix,
            String requestBody, boolean doAuthenticate)
            throws OsmTransferException {
        StringBuffer responseBody = new StringBuffer();
        int retries = 5;

        while (true) { // the retry loop
            try {
                URL url = new URL(new URL(getBaseUrl()), urlSuffix);
                Log.d(TAG, requestMethod + " " + url + "... ");

                activeConnection = (HttpURLConnection) url.openConnection();
                activeConnection.setConnectTimeout(15000);
                activeConnection.setRequestMethod(requestMethod);
//				if (requestMethod.equals("DELETE")) {
//					activeConnection.setRequestMethod("PUT");
//					activeConnection.addRequestProperty("X_HTTP_METHOD_OVERRIDE", "DELETE");
//				}


                if (doAuthenticate) {
                    addAuth(activeConnection);
                }



                if (requestMethod.equals("PUT") || requestMethod.equals("POST") ||
                        requestMethod.equals("DELETE")) {
                    activeConnection.setDoOutput(true);
                    activeConnection.setRequestProperty("Content-type",
                            "text/xml");
                    OutputStream out = activeConnection.getOutputStream();

                    // It seems that certain bits of the Ruby API are very
                    // unhappy upon
                    // receipt of a PUT/POST message without a Content-length
                    // header,
                    // even if the request has no payload.
                    // Since Java will not generate a Content-length header
                    // unless
                    // we use the output stream, we create an output stream for
                    // PUT/POST
                    // even if there is no payload.
                    if (requestBody != null) {
                        BufferedWriter bwr = new BufferedWriter(
                                new OutputStreamWriter(out, "UTF-8"));
                        bwr.write(requestBody);
                        bwr.flush();
                    }
                    out.close();
                }

                activeConnection.connect();
                int retCode = activeConnection.getResponseCode();
                Log.d(TAG, retCode+": "+activeConnection.getResponseMessage());

                if (retCode >= 500) {
                    if (retries-- > 0) {
                        sleepAndListen(retries);
                        Log.d(TAG, String.format(
                                "Starting retry {0} of {1}.", 5 - retries, 5));
                        continue;
                    }
                }

                // populate return fields.
                responseBody.setLength(0);

                // If the API returned an error code like 403 forbidden,
                // getInputStream
                // will fail with an IOException.
                InputStream i = null;
                try {
                    i = activeConnection.getInputStream();
                } catch (IOException ioe) {
                    i = activeConnection.getErrorStream();
                }
                if (i != null) {
                    // the input stream can be null if both the input and the
                    // error stream
                    // are null. Seems to be the case if the OSM server replies
                    // a 401
                    // Unauthorized, see #3887.
                    //
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(i));
                    String s;
                    while ((s = in.readLine()) != null) {
                        responseBody.append(s);
                        responseBody.append("\n");
                    }
                }
                String errorHeader = null;
                // Look for a detailed error message from the server
                if (activeConnection.getHeaderField("Error") != null) {
                    errorHeader = activeConnection.getHeaderField("Error");
                    System.err.println("Error header: " + errorHeader);
                } else if (retCode != 200 && responseBody.length() > 0) {
                    System.err.println("Error body: " + responseBody);
                }
                activeConnection.disconnect();

                errorHeader = errorHeader == null ? null : errorHeader.trim();
                String errorBody = responseBody.length() == 0 ? null
                        : responseBody.toString().trim();
                switch (retCode) {
                case HttpURLConnection.HTTP_OK:
                    return responseBody.toString();
                case HttpURLConnection.HTTP_GONE:
                    throw new OsmApiPrimitiveGoneException(errorHeader,
                            errorBody);
                case HttpURLConnection.HTTP_CONFLICT:
                    if (ChangesetClosedException
                            .errorHeaderMatchesPattern(errorHeader))
                        throw new ChangesetClosedException(errorBody,
                                ChangesetClosedException.Source.UPLOAD_DATA);
                    else
                        throw new OsmApiException(retCode, errorHeader,
                                errorBody);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    OsmApiException e = new OsmApiException(retCode,
                            errorHeader, errorBody);
                    e.setAccessedUrl(activeConnection.getURL().toString());
                    throw e;
                default:
                    throw new OsmApiException(retCode, errorHeader, errorBody);
                }
            } catch (UnknownHostException e) {
                throw new OsmTransferException(e);
            } catch (SocketTimeoutException e) {
                if (retries-- > 0) {
                    continue;
                }
                throw new OsmTransferException(e);
            } catch (ConnectException e) {
                if (retries-- > 0) {
                    continue;
                }
                throw new OsmTransferException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new OsmTransferException(e);
            } catch (OsmTransferCancelledException e) {
                throw e;
            }
            catch (OsmTransferException e) {
                throw e;
            }
        }
    }

    /**
     * Ensures that the current changeset can be used for uploading data
     *
     * @throws OsmTransferException
     *             thrown if the current changeset can't be used for uploading
     *             data
     */
    protected void ensureValidChangeset() throws OsmTransferException {
        if (changeset == null)
            throw new OsmTransferException(String
                    .format("Current changeset is null. Cannot upload data."));
        if (changeset.getId() <= 0)
            throw new OsmTransferException(String.format(
                    "ID of current changeset > 0 required. Current ID is {0}.",
                    changeset.getId()));
    }

    /**
     * Replies the changeset data uploads are currently directed to
     *
     * @return the changeset data uploads are currently directed to
     */
    public Changeset getChangeset() {
        return changeset;
    }

    /**
     * Sets the changesets to which further data uploads are directed. The
     * changeset can be null. If it isn't null it must have been created, i.e.
     * id > 0 is required. Furthermore, it must be open.
     *
     * @param changeset
     *            the changeset
     * @throws IllegalArgumentException
     *             thrown if changeset.getId() <= 0
     * @throws IllegalArgumentException
     *             thrown if !changeset.isOpen()
     */
    public void setChangeset(Changeset changeset) {
        if (changeset == null) {
            this.changeset = null;
            return;
        }
        if (changeset.getId() <= 0)
            throw new IllegalArgumentException(String.format(
                    "Changeset ID > 0 expected. Got {0}.", changeset.getId()));
        if (!changeset.isOpen())
            throw new IllegalArgumentException(
                    String
                            .format(
                                    "Open changeset expected. Got closed changeset with id {0}.",
                                    changeset.getId()));
        this.changeset = changeset;
    }
}
