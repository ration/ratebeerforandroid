A basic Http client for Android 2.1+ devices using HttpURLConnection 
====================================================================
Copyright (C) 2012 [Pixmob](https://plus.google.com/108697477633201807305/) (http://github.com/pixmob)

Android provides two methods for making Http(s) requests:
[HttpURLConnection](http://developer.android.com/reference/java/net/HttpURLConnection.html) and [Apache HttpClient](http://developer.android.com/reference/org/apache/http/impl/client/DefaultHttpClient.html).

The Android development team [recommends](http://android-developers.blogspot.com/2011/09/androids-http-clients.html) the use of HttpURLConnection for new applications, as this interface is getting better over Android releases.

However, using HttpURLConnection on earlier Android devices (before ICS) is troublesome because of the underlying implementation bugs:

 1. [connection pool poisoning](http://stackoverflow.com/a/4261005/422906)
 2. [missing SSL certificates on some Android devices](http://stackoverflow.com/a/3998257/422906)
 3. [slow POST requests](http://code.google.com/p/android/issues/detail?id=13117)
 4. [headers in lower-case](http://code.google.com/p/android/issues/detail?id=6684)
 5. [incomplete read](http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html)
 6. [transparent Gzip compression](http://code.google.com/p/android/issues/detail?id=16227)
 7. [broken Http authentication](http://code.google.com/p/android/issues/detail?id=9579)

The framework Pixmob HttpClient (PHC) provides a clean interface to HttpURLConnection, with
workarounds for known bugs, from Android 2.1 to Android 4.0.

Using PHC will make your code easier to understand, while leveraging the Android native network layer.

Usage
-----

Making Http requests with PHC is easy.

    HttpClient hc = new HttpClient();
    hc.get("http://www.google.com").execute();

Downloading a file is easy:

    File logoFile = new File(context.getCacheDir(), "logo.png");
    hc.get("http://www.mysite.com/logo.png").to(logoFile).execute();

You may want to send POST requests:

    // the response buffer is reusable across requests (GC friendly)
    final StringBuilder buf = new StringBuilder(64);
    hc.post("http://www.mysite.com/query").param("q", "hello").to(
        new HttpResponseHandler() {
            public void onResponse(HttpResponse response) throws Exception {
                response.read(buf);
                System.out.println(buf);
            }
        }
    ).execute();

The same request with fewer lines:

    final HttpResponse response = hc.post("http://www.mysite.com/query").param("q", "hello").execute();
    final StringBuilder buf = new StringBuilder(64);
    response.read(buf);
    System.out.println(buf);

Send an authenticated request (using Http Basic Authentication) this way:

    // reuse the same authenticator instance across requests
    final HttpBasicAuthenticator auth = new HttpBasicAuthenticator("admin", "secret");
    hc.delete("https://www.mysite.com/item/12").with(auth).execute();

Google AppEngine authentication is supported as well:

    final String gaeHost = "myapp.appspot.com";
    final GoogleAppEngineAuthenticator auth = new GoogleAppEngineAuthenticator(context, account, gaeHost);
    try {
        hc.get("http://" + gaeHost).with(auth).execute();
    } catch(UserInteractionRequiredException e) {
        // user authorization is required:
        // open system activity for granting access to user credentials
        // (user password is never stored nor known by this library)
        context.startActivityForResult(e.getUserIntent(), USER_AUTH);
    }

Please read JavaDoc and [source code](http://github.com/pixmob/httpclient/tree/master/src/org/pixmob/httpclient) for advanced use.

A sample application (with [source code](http://github.com/pixmob/httpclient/tree/master/demo/src/org/pixmob/httpclient/demo)) is available in this repository.

License
-------

All of the source code in this project is licensed under the Apache 2.0 license except as noted.

How to use this library in my project?
--------------------------------------

This project is actually a [library project](http://developer.android.com/guide/developing/projects/projects-cmdline.html#ReferencingLibraryProject).
