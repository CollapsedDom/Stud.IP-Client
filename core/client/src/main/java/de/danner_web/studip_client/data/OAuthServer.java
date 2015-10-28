package de.danner_web.studip_client.data;

import java.io.Serializable;

public class OAuthServer implements Serializable {

	/**
   *
   */
	private static final long serialVersionUID = -8829174284201506171L;

	// Base properties
	private String name;
	private String consumerKey;
	private String consumerSecret;
	private String studipBaseUrl;
	private String accessUrl;
	private String requestUrl;
	private String authorizationUrl;
	private String apiUrl;
	private String contactEmail;

	// Additional properties
	private String accessToken;
	private String accessTokenSecret;

	public OAuthServer() {
	}

	/**
	 * * This class represents an OAuthServer.
	 * 
	 * @param name
	 *            name of the university
	 * @param consumerKey
	 *            consumer key for oauth authentication
	 * @param consumerSecret
	 *            consumer secret for oauth authentication
	 * @param studipBaseUrl
	 *            base URL to the start page of the studip isntallation
	 * @param oauthBaseUrl
	 *            base URL to the oauth authentication
	 * @param apiBaseUrl
	 *            base URL to the APi
	 * @param contactEmail
	 *            email address of the maintainer
	 */
	public OAuthServer(String name, String consumerKey, String consumerSecret,
			String studipBaseUrl, String oauthBaseUrl, String apiBaseUrl,
			String contactEmail) {
		this.name = name;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.studipBaseUrl = studipBaseUrl;
		this.accessUrl = oauthBaseUrl + "/access_token";
		this.authorizationUrl = oauthBaseUrl + "/authorize";
		this.requestUrl = oauthBaseUrl + "/request_token";
		this.apiUrl = apiBaseUrl;
		this.contactEmail = contactEmail;
	}

	public boolean isAuthorized() {
		return (accessToken != null) && (accessTokenSecret != null);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the consumerKey
	 */
	public String getConsumerKey() {
		return consumerKey;
	}

	/**
	 * @param consumerKey
	 *            the consumerKey to set
	 */
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	/**
	 * @return the consumerSecret
	 */
	public String getConsumerSecret() {
		return consumerSecret;
	}

	/**
	 * @param consumerSecret
	 *            the consumerSecret to set
	 */
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	/**
	 * @return the baseUrl
	 */
	public String getStudipBaseUrl() {
		return studipBaseUrl;
	}

	/**
	 * @param baseUrl
	 *            the baseUrl to set
	 */
	public void setStudipBaseUrl(String baseUrl) {
		this.studipBaseUrl = baseUrl;
	}

	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl() {
		return accessUrl;
	}

	/**
	 * @param accessUrl
	 *            the accessUrl to set
	 */
	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}

	/**
	 * @return the requestUrl
	 */
	public String getRequestUrl() {
		return requestUrl;
	}

	/**
	 * @param requestUrl
	 *            the requestUrl to set
	 */
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	/**
	 * @return the authorizationUrl
	 */
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	/**
	 * @param authorizationUrl
	 *            the authorizationUrl to set
	 */
	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

	/**
	 * @return the apiUrl
	 */
	public String getApiUrl() {
		return apiUrl;
	}

	/**
	 * @param apiUrl
	 *            the apiUrl to set
	 */
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	/**
	 * @return the contactEmail
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken
	 *            the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return the accessTokenSecret
	 */
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	/**
	 * @param accessTokenSecret
	 *            the accessTokenSecret to set
	 */
	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}