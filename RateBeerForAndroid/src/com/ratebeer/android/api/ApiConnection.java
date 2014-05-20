package com.ratebeer.android.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.api.Scope;
import com.ratebeer.android.api.ApiException.ExceptionType;
import com.ratebeer.android.api.command.SignInCommand;
import com.ratebeer.android.gui.components.helpers.Log;

@EBean(scope = Scope.Singleton)
public class ApiConnection {

	private static final String USER_AGENT = "RateBeer for Android";
	private static final int TIMEOUT = 6000;
	private static final int RETRIES = 3;

	@Bean
	protected Log Log;
	@SystemService
	protected ConnectivityManager connectivityManager;

	private HttpClient httpClient;
	private boolean isSignedIn = false;
	public static final String RB_KEY = "tTmwRTWT-W7tpBhtL";

	protected ApiConnection(Context context) {
		httpClient = new HttpClient(context);
		httpClient.setUserAgent(USER_AGENT);
		httpClient.setConnectTimeout(TIMEOUT);
		httpClient.setReadTimeout(TIMEOUT);
	}

	/**
	 * Execute a POST HTTP request given some parameter. Note that this calls post(url, parameters, expectedHttpCode)
	 * with HttpURLConnection.HTTP_OK.
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
	 * @param expectedHttpCode The HTTP code that the caller expects will be returned (normally
	 *            HttpURLConnection.HTTP_OK)
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String post(String url, List<? extends NameValuePair> parameters, int expectedHttpCode) throws ApiException {
		HttpRequestBuilder post = httpClient.post(url).expect(expectedHttpCode);
		for (NameValuePair pair : parameters) {
			post.param(pair.getName(), pair.getValue());
		}
		return readStream(executeRequest(post));
	}

	/**
	 * Execute a POST HTTP request given some parameters and a file to upload
	 * @param url The url to post to
	 * @param parameters The names and values to post as parameters
	 * @param file The file to upload
	 * @param fileFieldName The name of the file upload field in the html form
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String postFile(String url, List<? extends NameValuePair> parameters, File file, String fileFieldName)
			throws ApiException {

		// Prepare a POST request to url
		HttpRequestBuilder prepared = httpClient.post(url);
		for (NameValuePair pair : parameters) {
			prepared.param(pair.getName(), pair.getValue());
		}

		// Build a multipart http POST request where the content type and file contents are written to
		final String BOUNDARY = "xxxxxxxxxx";
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytes);
		byte[] requestBody = null;
		try {
			dataOut.writeBytes("--");
			dataOut.writeBytes(BOUNDARY);
			dataOut.writeBytes("\r\n");
			dataOut.writeBytes("Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\""
					+ file.getName() + "\"\r\n");
			// dataOut.writeBytes("Content-Type: text/xml; charset=utf-8\r\n");
			dataOut.writeBytes("Content-Type: application/octet-stream\r\n");
			dataOut.writeBytes("\r\n");
			FileInputStream inputStream = new FileInputStream(file);
			byte[] buffer = new byte[4096]; // 4kB buffer should be enough, as the photo uploads are only small
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				dataOut.write(buffer, 0, bytesRead);
			}
			inputStream.close();
			dataOut.writeBytes("\r\n");
			dataOut.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
			dataOut.flush();
			requestBody = bytes.toByteArray();
		} catch (IOException ex) {
			throw new ApiException(ExceptionType.ConnectionError, "Could not read or write the file bytes."
					+ ex.toString());
		} finally {
			if (bytes != null) {
				try {
					bytes.close();
				} catch (IOException e) {
				}
			}
		}

		if (requestBody == null)
			throw new ApiException(ExceptionType.ConnectionError,
					"Could not build a request body for the multipart file upload.");
		// Set the content bytes (multipart form data and file data) on the POST request
		prepared.content(requestBody, "multipart/form-data; boundary=" + BOUNDARY);
		// Perform the POST request
		return readStream(executeRequest(prepared));

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
	 * Execute a GET HTTP request for some url and a specific expected HTTP code
	 * @param url The url to get
	 * @param expectedHttpCode The HTTP code that the caller expects will be returned (normally
	 *            HttpURLConnection.HTTP_OK)
	 * @return The server response data as String
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public String get(String url, int expectedHttpCode) throws ApiException {
		return readStream(executeRequest(httpClient.get(url).expect(expectedHttpCode)));
	}

	/**
	 * Execute a GET HTTP request for some url and retrieve the data as input stream
	 * @param url The url to get
	 * @return The raw server response data stream
	 * @throws ApiException Thrown when a connection exception occurs, including when the user is offline
	 */
	public InputStream getRaw(String url) throws ApiException {
		return executeRequest(httpClient.get(url).expect(HttpURLConnection.HTTP_OK));
	}

	protected InputStream executeRequest(HttpRequestBuilder prepared) throws ApiException {

		// Check if we are even connected to a network
		if (!isConnected())
			throw new ApiException(ExceptionType.Offline,
					"User is not connected to a network (as reported by the system)");

		// Now try to execute the http request; if it fails for some reach we retry at most RETRIES times
		for (int i = 0; i < RETRIES; i++) {
			try {
				return prepared.execute().getPayload();
			} catch (HttpClientException e) {
				Log.i(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "GET failed: " + e.toString()
						+ " (now retry)");
				// Retry
			}
		}

		throw new ApiException(ExceptionType.ConnectionError, "We tried " + RETRIES + " times but the request to "
				+ prepared.toString() + " still failed.");

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
			android.util.Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
					"HTTP InputStream received but an IO exception occured when reading it.");
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
		return isSignedIn;
	}

	public int signIn(String username, String password) throws ApiException, NumberFormatException {

		if (!isConnected())
			throw new ApiException(ExceptionType.Offline,
					"User is not connected to a network (as reported by the system)");

		HttpRequestBuilder prepared = httpClient.post("http://www.ratebeer.com/Signin_r.asp");
		prepared.param("SaveInfo", "on");
		prepared.param("username", username);
		prepared.param("pwd", password);
		prepared.expect(HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_OK);
		final String uidText = "?uid=";
		for (int i = 0; i < RETRIES; i++) {
			try {

				HttpResponse reply = prepared.execute();
				
				// We should have a session cookie now, which means we are signed in to RateBeer
				isSignedIn = reply.getCookies().containsKey("SessionCode");
				if (isSignedIn()) {
					// Find the user ID in the redirect response header 'Location'
					String header = reply.getFirstHeaderValue("Location");
					if (header != null && header.indexOf(uidText) >= 0) {
						return Integer.parseInt(header.substring(header.indexOf(uidText) + uidText.length()));
					}
					throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
							"Tried to sign in but the response header did not include the user ID. 'Location' header was: "
									+ header.toString());
				}
				if (reply.getStatusCode() == HttpURLConnection.HTTP_OK && readStream(reply.getPayload()).indexOf("User name or password is invalid") >= 0) {
					throw new ApiException(ExceptionType.AuthenticationFailed, "Incorrect username or password");
				}
				// No login cookies returned by the server... grrr... try to recover from RateBeer's unholy 
				// authentication/cookie mess by just trying again
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Could not sign in as no cookie with key SessionCode was found. We try again...");
			} catch (HttpClientException e) {
				Log.i(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "GET failed: " + e.toString()
						+ " (now retry)");
				// Retry
			}
		}

		throw new ApiException(ExceptionType.ConnectionError, "We tried " + RETRIES + " times but the request to "
				+ prepared.toString() + " still failed.");

	}

	public void signOut() throws ApiException {

		if (!isConnected())
			throw new ApiException(ExceptionType.Offline,
					"User is not connected to a network (as reported by the system)");

		// Calling Signout.asp should sign us out and remove any cookies
		get("http://www.ratebeer.com/Signout.asp?v=1");
		// Command was successful, assume we are now signed out
		isSignedIn = false;

	}

	public static void ensureLogin(ApiConnection apiConnection, UserSettings userSettings) throws ApiException {
		// Make sure we are logged in
		if (!apiConnection.isSignedIn()) {
			new SignInCommand(userSettings, userSettings.getUsername(), userSettings.getPassword())
					.execute(apiConnection);
			if (!apiConnection.isSignedIn()) {
				throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
						"Tried to sign in but no (login) cookies were returned by the server");
			}
		}

	}

}
