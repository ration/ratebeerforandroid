package com.ratebeer.android.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.pixmob.httpclient.HttpClient;
import org.pixmob.httpclient.HttpClientException;
import org.pixmob.httpclient.HttpRequestBuilder;
import org.pixmob.httpclient.HttpResponse;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.api.Scope;
import com.ratebeer.android.api.ApiException.ExceptionType;
import com.ratebeer.android.app.RateBeerForAndroid;

@EBean(scope = Scope.Singleton)
public class ApiConnection {

	private static final String USER_AGENT = "RateBeer for Android";
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;

	@SystemService
	protected ConnectivityManager connectivityManager;
	
	private HttpClient httpClient;
	private boolean isSignedIn = false;
	
	protected ApiConnection(Context context) {
		httpClient = new HttpClient(context);
		httpClient.setUserAgent(USER_AGENT);
		httpClient.setConnectTimeout(TIMEOUT);
		httpClient.setReadTimeout(TIMEOUT);
	}

	/**
	 * Execute a POST HTTP request given some parameter. Note that this calls post(url, parameters, expectedHttpCode) with HttpURLConnection.HTTP_OK.
	 * @param url The url to post to
	 * @param parameters The names and values to post as parameters
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String post(String url, List<? extends NameValuePair> parameters) throws ApiException {
		return post(url, parameters, HttpURLConnection.HTTP_OK);
	}
	
	/**
	 * Execute a POST HTTP request given some parameters and a specific expected HTTP response code
	 * @param url The url to post to
	 * @param parameters The names and values to post as parameters
	 * @param expectedHttpCode The HTTP code that the caller expects will be returned (normally HttpURLConnection.HTTP_OK)
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String post(String url, List<? extends NameValuePair> parameters, int expectedHttpCode) throws ApiException {
		HttpRequestBuilder post = httpClient.post(url);
		for (NameValuePair pair : parameters) {
			post.param(pair.getName(), pair.getValue());
		}
		return readStream(executeRequest(post, expectedHttpCode));
	}
	
	/**
	 * Execute a GET HTTP request for some url
	 * @param url The url to get
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String get(String url) throws ApiException {
		return readStream(getRaw(url));
	}

	/**
	 * Execute a GET HTTP request for some url and retrieve the data as input stream
	 * @param url The url to get
	 * @return The raw server response data stream
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public InputStream getRaw(String url) throws ApiException {
		return executeRequest(httpClient.get(url), HttpURLConnection.HTTP_OK);
	}
	
	protected InputStream executeRequest(HttpRequestBuilder prepared, int expectedHttpCode) throws ApiException {
		
		if (!isConnected())
			throw new ApiException(ExceptionType.Offline, "User is not connected to a network (as reported by the system)");
		
		for (int i = 0; i < RETRIES; i++) {
			try {
				HttpResponse reply = prepared.execute();
				if (reply.getStatusCode() == expectedHttpCode)
					return reply.getPayload();
			} catch (HttpClientException e) {
				Log.i(RateBeerForAndroid.LOG_NAME, "GET failed: " + e.toString() + " (now retry)");
				// Retry
			}
		}
		
		throw new ApiException(ExceptionType.ConnectionError, "We tried " + RETRIES + " times but the request to " + prepared.toString() + " still failed.");
		
	}

	public static String readStream(InputStream is) throws ApiException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(is, "windows-1252");
		} catch (UnsupportedEncodingException e1) {
			throw new ApiException(ExceptionType.ConnectionError,
					"HTTP stream was received but it does not seem to be in the expected windows-1252 encoding");
		}
		BufferedReader reader = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e(RateBeerForAndroid.LOG_NAME, "HTTP InputStream received but an IO exception occured when reading it.");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Wasn't opened in the first place: ignore this case
			}
		}
		return sb.toString();
	}

	/**
	 * Returns whether the user has an active internet connection. Note that it migth still be unstable or not actually 
	 * connected to the internet (such as a local-only wifi network).
	 * @return Returns true if the system reports there is an active network connection
	 */
	public boolean isConnected() {
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public boolean isSignedIn() {
		return isSignedIn ;
	}

	public int signIn(String username, String password) throws ApiException {

		if (!isConnected())
			throw new ApiException(ExceptionType.Offline, "User is not connected to a network (as reported by the system)");
		
		HttpRequestBuilder prepared = httpClient.post("http://www.ratebeer.com/Signin_r.asp");
		prepared.param("SaveInfo", "on");
		prepared.param("username", username);
		prepared.param("pwd", password);
		final String uidText = "?uid=";
		for (int i = 0; i < RETRIES; i++) {
			try {
				HttpResponse reply = prepared.execute();
				if (reply.getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
					// We should have a session cookie now, which means we are signed in to RateBeer
					isSignedIn = reply.getCookies().containsKey("SessionCode");
					if (isSignedIn()) {
						// Find the user ID in the redirect response header
						// This should be encoded as a Location header, like
						String header = reply.getFirstHeaderValue("Location");
						if (header != null && header.indexOf(uidText) >= 0) {
							return Integer.parseInt(header.substring(header.indexOf(uidText) + uidText.length()));
						}
						throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
								"Tried to sign in but the response header did not include the user ID. Header was: "
										+ header.toString());
					}
					// No login cookies returned by the server... grrr... try to recover from RateBeer's unholy
					// authentication/cookie mess by just trying again
				}
			} catch (HttpClientException e) {
				Log.i(RateBeerForAndroid.LOG_NAME, "GET failed: " + e.toString() + " (now retry)");
				// Retry
			}
		}
		
		throw new ApiException(ExceptionType.ConnectionError, "We tried " + RETRIES + " times but the request to " + prepared.toString() + " still failed.");
		
	}
	
}
