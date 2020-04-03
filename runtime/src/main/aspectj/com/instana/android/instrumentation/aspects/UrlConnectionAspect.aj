package com.instana.android.instrumentation.aspects;

import com.instana.android.Instana;
import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.HTTPMarker;
import org.aspectj.lang.JoinPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.instana.android.core.util.ConstantsAndUtil.*;

public aspect UrlConnectionAspect {
    private final List<HTTPMarker> httpMarkers = new LinkedList<>();

    pointcut openConnectionMethodCall(): call(* java.net.URL.openConnection());
    after() returning(HttpURLConnection connection): openConnectionMethodCall() {
        Logger.i("HttpURLConnection: intercepting openConnection");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && !checkTag(header) && isNotLibraryCallBoolean(url) && !isBlacklistedURL(url)) {
            HTTPMarker marker = Instana.startCapture(url);
            connection.setRequestProperty(marker.headerKey(), marker.headerValue());
            httpMarkers.add(marker);
        }
    }

    pointcut disconnectMethodCall(HttpURLConnection connection): target(connection) && call(* java.net.HttpURLConnection.disconnect());
    before(HttpURLConnection connection): disconnectMethodCall(connection) {
        Logger.i("HttpURLConnection: intercepting disconnect");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && isNotLibraryCallBoolean(url) && checkTag(header)) {
            try {
                HTTPMarker marker = findFirst(httpMarkers, header);
                marker.finish(connection);
                httpMarkers.remove(marker);
            } catch (NoSuchElementException ignored) {
                // swallow exception
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
                try {
                    HTTPMarker marker = findFirst(httpMarkers, header);
                    marker.finish(urlConnection, e);
                    httpMarkers.remove(marker);
                } catch (NoSuchElementException ignored) {
                    // swallow exception
                }
            }
        }
    }

    private HTTPMarker findFirst(List<HTTPMarker> list, String tag) throws NoSuchElementException {
        for (HTTPMarker remoteMarker : list) {
            if (remoteMarker.headerValue().equals(tag)) {
                return remoteMarker;
            }
        }
        throw new NoSuchElementException();
    }
}
