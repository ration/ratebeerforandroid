/*
    This file is part of RateBeer For Android.
    
    RateBeer for Android is free software: you can redistribute it 
    and/or modify it under the terms of the GNU General Public 
    License as published by the Free Software Foundation, either 
    version 3 of the License, or (at your option) any later version.

    RateBeer for Android is distributed in the hope that it will be 
    useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RateBeer for Android.  If not, see 
    <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

public class HttpHelper {

	private static final int TIMEOUT = 10000;
	private static final String USER_AGENT = "RateBeer for Android";
	private static final String URL_SIGNIN = "http://www.ratebeer.com/signin/";
	public static final String RB_KEY = "tTmwRTWT-W7tpBhtL";
	public static final String UTF8 = "UTF-8";

	private static DefaultHttpClient httpClient = null;
	
	private static void ensureClient() {
		if (httpClient == null) {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", new PlainSocketFactory(), 80));
			HttpParams httpparams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpparams, TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpparams, TIMEOUT);
			HttpProtocolParams.setUserAgent(httpparams, USER_AGENT);
			
			httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpparams, registry), httpparams);
			// TODO: See if we can enable this again; RateBeer seemd to have problems with sending GZipped content
			//httpClient.addRequestInterceptor(HttpHelper.gzipRequestInterceptor);
			//httpClient.addResponseInterceptor(HttpHelper.gzipResponseInterceptor);
		}
	}

	public static String makeRBGet(String url) throws ClientProtocolException, IOException {
		return getResponseString(makeRawRBGet(url));
	}

	public static InputStream makeRawRBGet(String url) throws ClientProtocolException, IOException {
		ensureClient();
		
		// Execute a GET request and return the raw response stream
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return response.getEntity().getContent();
		}
		
		throw new IOException("ratebeer.com offline?");
	}

	public static String makeRBPost(String url, List<? extends NameValuePair> parameters) throws ClientProtocolException, IOException {
		return makeRBPost(url, parameters, HttpStatus.SC_OK);
	}
	
	public static String makeRBPost(String url, List<? extends NameValuePair> parameters, int expectedHttpCode) throws ClientProtocolException, IOException {
		ensureClient();

		// Set up POST request
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(parameters));
		
		// Execute a POST request to sign in
		HttpResponse response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == expectedHttpCode) {
			return getResponseString(response.getEntity().getContent());
		}
		
		throw new IOException("ratebeer.com offline?");
		
	}

	public static boolean signIn(String username, String password) throws ClientProtocolException, IOException {
		ensureClient();

		// Set up POST request
		// TODO: Use the API call to http://www.ratebeer.com/Signin_r.asp instead (which already returns the user ID too)
		HttpPost post = new HttpPost(URL_SIGNIN);
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("SaveInfo", "on"));
		parameters.add(new BasicNameValuePair("username", username));
		parameters.add(new BasicNameValuePair("pwd", password));
		post.setEntity(new UrlEncodedFormEntity(parameters));
		
		// Execute a POST request to sign in
		httpClient.execute(post);
		
		boolean success = isSignedIn();
		post.abort(); // Consume content
		return success;
		
	}

	public static String getResponseString(InputStream is) throws IOException {
		/*ByteArrayOutputStream ostream = new ByteArrayOutputStream();  
		response.getEntity().writeTo(ostream);
		//return ostream.toString("ISO8859_1");
		return ostream.toString("windows-1252"); */
    	InputStreamReader isr = new InputStreamReader(is, "windows-1252");
    	BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
	}
	
	public static boolean isSignedIn() {
		ensureClient();

		// Check if we have the required cookie now
		List<Cookie> beforeCookies = httpClient.getCookieStore().getCookies();
		for (Iterator<Cookie> iterator = beforeCookies.iterator(); iterator.hasNext();) {
			Cookie cookie = iterator.next();
			if(cookie.getName().equalsIgnoreCase("SessionCode")){
				return true;
			}
		}
		return false;

	}

	/**
	 * HTTP request interceptor to allow for GZip-encoded data transfer
	 */
	public static HttpRequestInterceptor gzipRequestInterceptor = new HttpRequestInterceptor() {
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			if (!request.containsHeader("Accept-Encoding")) {
				request.addHeader("Accept-Encoding", "gzip");
			}
		}
	};

	/**
	 * HTTP response interceptor that decodes GZipped data
	 */
	public static HttpResponseInterceptor gzipResponseInterceptor = new HttpResponseInterceptor() {
		public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			Header ceheader = entity.getContentEncoding();
			if (ceheader != null) {
				HeaderElement[] codecs = ceheader.getElements();
				for (int i = 0; i < codecs.length; i++) {

					if (codecs[i].getName().equalsIgnoreCase("gzip")) {
						response.setEntity(new HttpHelper.GzipDecompressingEntity(response.getEntity()));
						return;
					}
				}
			}
		}

	};

	/**
	 * HTTP entity wrapper to decompress GZipped HTTP responses
	 */
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {

			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();

			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			// length of ungzipped content is not known
			return -1;
		}

	}

	public static String cleanHtml(String value) {
		// Translation of HTML-encoded characters (and line breaks)
		value = value.replaceAll("\r\n", "");
		value = value.replaceAll("\n", "");
		value = value.replaceAll("\r", "");
		value = value.replaceAll("<br>", "\n");
		value = value.replaceAll("<br />", "\n");
		value = value.replaceAll("<BR>", "\n");
		value = value.replaceAll("<BR />", "\n");
		value = value.replaceAll("&quot;", "\"");
		return HTMLEntities.unhtmlentities(value);
	}

	public static String normalizeSearchQuery(String query) {
		// First translate diacritics
		// (from http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet)
		String normalized = Normalizer.normalize(query, Normalizer.Form.NFD); 
	    // And remove the marks to only leave Latin characters
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    query = pattern.matcher(normalized).replaceAll("");
		// Translate other special characters into English alphabet characters by hand
		query = query.replaceAll("æ", "ae");
		query = query.replaceAll("Æ", "AE");
		query = query.replaceAll("ß", "ss");
		query = query.replaceAll("ø", "o");
		query = query.replaceAll("Ø", "O");
		return query;
	}
	
}
