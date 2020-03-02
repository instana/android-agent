package com.instana.android.instrumentation.aspects;

import com.instana.android.Instana;
import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.RemoteCallMarker;

import org.aspectj.lang.JoinPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY;
import static com.instana.android.core.util.ConstantsAndUtil.checkTag;
import static com.instana.android.core.util.ConstantsAndUtil.hasTrackingHeader;
import static com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled;
import static com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL;
import static com.instana.android.core.util.ConstantsAndUtil.isNotLibraryCallBoolean;

public aspect UrlConnectionAspect {
    private final List<RemoteCallMarker> remoteMarkers = new LinkedList<>();

    pointcut openConnectionMethodCall(): call(* java.net.URL.openConnection());
    after() returning(HttpURLConnection connection): openConnectionMethodCall() {
        Logger.i("HttpURLConnection: intercepting openConnection");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && !checkTag(header) && isNotLibraryCallBoolean(url) && !isBlacklistedURL(url)) {
            RemoteCallMarker marker = Instana.remoteCallInstrumentation.markCall(url, connection.getRequestMethod());
            connection.setRequestProperty(marker.headerKey(), marker.headerValue());
            remoteMarkers.add(marker);
        }
    }

    pointcut disconnectMethodCall(HttpURLConnection connection): target(connection) && call(* java.net.HttpURLConnection.disconnect());
    before(HttpURLConnection connection): disconnectMethodCall(connection) {
        Logger.i("HttpURLConnection: intercepting disconnect");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && isNotLibraryCallBoolean(url) && checkTag(header)) {
            try {
                RemoteCallMarker marker = findFirst(remoteMarkers, header);
                marker.endedWith(connection);
                remoteMarkers.remove(marker);
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
                    RemoteCallMarker marker = findFirst(remoteMarkers, header);
                    marker.endedWith(urlConnection, e);
                    remoteMarkers.remove(marker);
                } catch (NoSuchElementException ignored) {
                    // swallow exception
                }
            }
        }
    }

    private RemoteCallMarker findFirst(List<RemoteCallMarker> list, String tag) throws NoSuchElementException {
        for (RemoteCallMarker remoteMarker : list) {
            if (remoteMarker.headerValue().equals(tag)) {
                return remoteMarker;
            }
        }
        throw new NoSuchElementException();
    }
}
