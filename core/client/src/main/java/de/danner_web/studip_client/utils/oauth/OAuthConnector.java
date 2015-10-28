package de.danner_web.studip_client.utils.oauth;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.OAuthServer;

public class OAuthConnector {

    /**
     * Logger dieser Klasse.
     */
    private static Logger logger = LogManager.getLogger(OAuthConnector.class);

    private OAuthServer mServer = null;

    private OAuthConsumer mConsumer = null;
    private OAuthProvider mProvider = null;
    private String authUrl = "";

    private boolean isAuthorized = false;

    public OAuthConnector(OAuthServer server) {
        logger.entry(server);
        if (server == null) {
            throw new IllegalArgumentException("Server must not be null.");
        }

        logger.debug("new OAuthconn");

        this.mServer = server;
        this.mConsumer = new DefaultOAuthConsumer(server.getConsumerKey(), server.getConsumerSecret());

        String accessToken = mServer.getAccessToken();
        String accessTokenSecret = mServer.getAccessTokenSecret();

        if (accessToken != null || accessTokenSecret != null) {
            this.mConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
            isAuthorized = true;
        }
        logger.exit(server);
    }

    /**
     * This method request a request token and saves the received authUrl in the
     * global variable
     * 
     * @return true if success; false otherwise
     */
    public ResponseCode getRequestToken() {
        logger.entry();

        if (isAuthorized) {
            return logger.exit(ResponseCode.APPLICATION_ALREADY_AUTHORIZED);
        }

        mProvider = new DefaultOAuthProvider(mServer.getRequestUrl(), mServer.getAccessUrl(),
                mServer.getAuthorizationUrl());
        try {
            authUrl = mProvider.retrieveRequestToken(mConsumer, "http://studip-client.danner-web.de/callback");
        } catch (OAuthMessageSignerException | OAuthNotAuthorizedException | OAuthExpectationFailedException
                | OAuthCommunicationException e) {
            logger.debug("Unable to fetch oauth_request_token");
            return logger.exit(ResponseCode.SERVER_NOT_REACHABLE);
        }

        return logger.exit(ResponseCode.SUCCESS);
    }

    public ResponseCode getAccessToken() {

        if (isAuthorized) {
            return logger.exit(ResponseCode.APPLICATION_ALREADY_AUTHORIZED);
        }

        if (mProvider != null) {
            try {
                mProvider.retrieveAccessToken(mConsumer, OAuth.OUT_OF_BAND);

                mServer.setAccessToken(mConsumer.getToken());
                mServer.setAccessTokenSecret(mConsumer.getTokenSecret());

                isAuthorized = true;

            } catch (OAuthMessageSignerException | OAuthNotAuthorizedException | OAuthExpectationFailedException
                    | OAuthCommunicationException e) {
                logger.debug("Unable to fetch oauth_access_token");
                return logger.exit(ResponseCode.APPLICATION_NOT_AUTHORIZED);
            }
        }
        return logger.exit(ResponseCode.SUCCESS);
    }

    /**
     * This method return the authorized OAuthServer to save it to the Prefs
     * 
     * @return authorized OAuthServer or null
     */
    public OAuthServer getAuthorizedOAuthServer() {
        if (isAuthorized) {
            return mServer;
        }
        return null;
    }

    public HttpURLConnection post(String pattern, String json) throws OAuthNotAuthorizedException,
            OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        return sign(pattern, Method.POST, json);
    }

    public HttpURLConnection get(String pattern) throws OAuthNotAuthorizedException, OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        return sign(pattern, Method.GET, "");
    }

    public HttpURLConnection put(String pattern) throws OAuthNotAuthorizedException, OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        return sign(pattern, Method.PUT, "");
    }

    public HttpURLConnection delete(String pattern) throws OAuthNotAuthorizedException, OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        return sign(pattern, Method.DELETE, "");
    }

    private HttpURLConnection sign(String pattern, Method method, String json) throws OAuthNotAuthorizedException,
            OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {

        if (mConsumer.getToken() == null || mConsumer.getTokenSecret() == null || !isAuthorized) {
            throw new OAuthNotAuthorizedException();
        }

        // Connect and send request
        HttpURLConnection connection = null;
        try {
            URL url = new URL(mServer.getApiUrl() + "/" + pattern);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method.toString());

            // POST
            if (method.equals(Method.POST)) {
                logger.warn("Not tested yet!");

                // Create an HttpURLConnection and add some headers
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf8");
                connection.setDoOutput(true);

                // Sign the request
                HttpParameters doubleEncodedParams = new HttpParameters();
                doubleEncodedParams.put("realm", url.toString());
                mConsumer.setAdditionalParameters(doubleEncodedParams);
                mConsumer.sign(connection);

                // Send the payload to the connection
                OutputStreamWriter outputStreamWriter = null;
                try {
                    outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                    outputStreamWriter.write(json);
                } finally {
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                }

                // Send the request and read the output
                // try {
                // System.out.println(
                // "Response: " + connection.getResponseCode() + " " +
                // connection.getResponseMessage());
                // InputStream in = new
                // BufferedInputStream(connection.getInputStream());
                // String inputStreamString = new Scanner(in,
                // "UTF-8").useDelimiter("\\A").next();
                // System.out.println(inputStreamString);
                // } finally {
                // connection.disconnect();
                // }

            } else {

                mConsumer.sign(connection);
                connection.connect();
            }

        } catch (IOException e) {
            logger.debug(e);
        }

        return logger.exit(connection);
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public URI getAuthUrI() {
        try {
            return new URI(authUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Testing method
     * 
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!
     * 
     * IMPORTANT: comment out this method, otherwise the jar. file from
     * build.xml wont work !!
     * 
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!
     * 
     * @param agrs
     * @throws Exception
     */
    // public static void main(String[] args) throws Exception {
    // OAuthServer server = new OAuthServer("Test core-API",
    // "c30b1f674ec41ad4bf843b6ae4f134af054ee6b21",
    // "1124bd599f04fd759bdce1117632489d", "http://danner-web.org/studip",
    // "http://danner-web.org/studip/dispatch.php/api/oauth",
    // "http://danner-web.org/studip/api.php", "philipp@danner-web.de");
    // OAuthConnector test = new OAuthConnector(server);
    //
    // System.out.println("OAuth-Login: "
    // + test.connect("test_dozent", "testing"));
    //
    // //HttpURLConnection con = test.get(CoreApiPattern.USER);
    // //List<User> users = JSONParserUtil.parse(con, User.class);
    // //System.out.println("Test printout Fullname of User: "
    // // + users.get(0).getFullName());
    //
    // HttpURLConnection response = null;
    // try {
    // response = test.get("ssf-core/semesters");
    // } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
    // | OAuthExpectationFailedException
    // | OAuthCommunicationException e) {
    // throw new UpdateFailureException();
    // }
    // List<SemesterData> semesterList = JSONParserUtil.parse(response,
    // SemesterData.class);
    //
    // System.out.println("semester_id: " + semesterList.get(1).semester_id);
    //
    // try {
    // response = test.get("ssf-core/courses/eb828ebb81bb946fac4108521a3b4697");
    // } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
    // | OAuthExpectationFailedException
    // | OAuthCommunicationException e) {
    // throw new UpdateFailureException();
    // }
    // List<CourseData> courseList = JSONParserUtil.parse(response,
    // CourseData.class);
    //
    // System.out.println("course_id: " + courseList.get(0).course_id);
    //
    // try {
    // response = test.get("ssf-core/folders/a07535cf2f8a72df33c12ddfa4b53dde");
    // } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
    // | OAuthExpectationFailedException
    // | OAuthCommunicationException e) {
    // throw new UpdateFailureException();
    // }
    // List<FolderData> folderList = JSONParserUtil.parse(response,
    // FolderData.class);
    //
    // System.out.println("folder_id: " + folderList.get(0).folder_id);
    //
    // try {
    // response =
    // test.get("ssf-core/documents/ca002fbae136b07e4df29e0136e3bd32");
    // } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
    // | OAuthExpectationFailedException
    // | OAuthCommunicationException e) {
    // throw new UpdateFailureException();
    // }
    // List<DocumentData> documentList = JSONParserUtil.parse(response,
    // DocumentData.class);
    //
    // System.out.println("document_id: " + documentList.get(0).document_id);
    // }
}
