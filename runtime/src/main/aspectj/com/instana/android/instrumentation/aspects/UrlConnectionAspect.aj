package com.instana.android.instrumentation.aspects;

import com.instana.android.Instana;
import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.HTTPMarker;
import org.aspectj.lang.JoinPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.instana.android.core.util.ConstantsAndUtil.*;

public aspect UrlConnectionAspect {
    private final Map<String, HTTPMarker> httpMarkers = new ConcurrentHashMap<>();

    pointcut openConnectionMethodCall(): call(* java.net.URL.openConnection());
    after() returning(HttpURLConnection connection): openConnectionMethodCall() {
        Logger.i("HttpURLConnection: intercepting openConnection");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && !checkTag(header) && isNotLibraryCallBoolean(url) && !isBlacklistedURL(url)) {
            HTTPMarker marker = Instana.startCapture(url);
            connection.setRequestProperty(marker.headerKey(), marker.headerValue());
            httpMarkers.put(marker.headerValue(), marker);
        }
    }

    pointcut disconnectMethodCall(HttpURLConnection connection): target(connection) && call(* java.net.HttpURLConnection.disconnect());
    before(HttpURLConnection connection): disconnectMethodCall(connection) {
        Logger.i("HttpURLConnection: intercepting disconnect");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && isNotLibraryCallBoolean(url) && checkTag(header)) {
            HTTPMarker marker = httpMarkers.get(header);
            if (marker != null) {
                marker.finish(connection);
                httpMarkers.remove(header);
            }
        }
    }

    pointcut outputStream(): call(* java.net.HttpURLConnection.getOutputStream());
    after() throwing(IOException e): outputStream() {
        handleException(thisJoinPoint, e);
    }

    pointcut inputStream(): call(* java.net.HttpURLConnection.getInputStream());
    after() throwing(IOException e): inputStream() {
        handleException(thisJoinPoint, e);
    }

    pointcut setRequestMethod(): call(* java.net.HttpURLConnection.setRequestMethod(..));
    after() throwing(ProtocolException e): setRequestMethod() {
        handleException(thisJoinPoint, e);
    }

    pointcut connect(): call(* java.net.HttpURLConnection.connect());
    after() throwing(IOException e): connect() {
        handleException(thisJoinPoint, e);
    }

    private void handleException(JoinPoint joinPoint, Throwable e) {
        Logger.i("HttpURLConnection: handling exception");
        if (joinPoint.getTarget() instanceof HttpURLConnection) {
            HttpURLConnection urlConnection = (HttpURLConnection) joinPoint.getTarget();
            String header = urlConnection.getRequestProperty(TRACKING_HEADER_KEY);
            String url = urlConnection.getURL().toString();
            if (isAutoEnabled() && hasTrackingHeader(header) && isNotLibraryCallBoolean(url) && checkTag(header)) {
                HTTPMarker marker = httpMarkers.get(header);
                if (marker != null) {
                    marker.finish(urlConnection, e);
                    httpMarkers.remove(header);
                }
            }
        }
    }
}
