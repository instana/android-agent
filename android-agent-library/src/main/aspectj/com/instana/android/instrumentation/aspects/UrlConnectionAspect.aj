package com.instana.android.instrumentation.aspects;

import android.os.Build;
import com.instana.android.Instana;
import com.instana.android.core.event.models.RemoteCall;
import com.instana.android.core.util.ConstantsAndUtil;
import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.RemoteCallMarker;
import org.aspectj.lang.JoinPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.instana.android.core.util.ConstantsAndUtil.*;

public aspect UrlConnectionAspect {
    private final ArrayList<RemoteCallMarker> remoteMarkers = new ArrayList<>();

    pointcut openConnectionMethodCall(): call(* java.net.URL.openConnection());

    pointcut disconnectMethodCall(HttpURLConnection connection):
            target(connection) && call(* java.net.HttpURLConnection.disconnect());

    pointcut outputStream(): call(* java.net.HttpURLConnection.getOutputStream());

    pointcut inputStream(): call(* java.net.HttpURLConnection.getInputStream());

    pointcut connect(): call(* java.net.HttpURLConnection.connect());

    pointcut setRequestMethod(): call(* java.net.HttpURLConnection.setRequestMethod(..));

    after() returning(HttpURLConnection connection): openConnectionMethodCall() {
        Logger.i("Interceptiong openConnection");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && !checkTag(header) && isNotLibraryCallBoolean(url)) {
            RemoteCallMarker marker = Instana.remoteCallInstrumentation.markCall(url);
            connection.setRequestProperty(marker.headerKey(), marker.headerValue());
            remoteMarkers.add(marker);
        }
    }

    after(HttpURLConnection connection): disconnectMethodCall(connection) {
        Logger.i("Intercepting disconnect");
        String header = connection.getRequestProperty(TRACKING_HEADER_KEY);
        String url = connection.getURL().toString();
        if (isAutoEnabled() && isNotLibraryCallBoolean(url) && checkTag(header)) {
            try {
                RemoteCallMarker marker = remoteMarkers.stream().filter(m -> m.headerValue().equals(header)).findFirst().get();
                long responseSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? connection.getContentLengthLong() : (long) connection.getContentLength();
                marker.endedWith(0, responseSize, connection);
                remoteMarkers.remove(marker);
            } catch (NoSuchElementException ignored) {
                // swallow exception
            }
        }
    }

    after() throwing(IOException e): outputStream() {
        handleException(thisJoinPoint, e);
    }

    after() throwing(IOException e): inputStream() {
        handleException(thisJoinPoint, e);
    }

    after() throwing(ProtocolException e): setRequestMethod() {
        handleException(thisJoinPoint, e);
    }

    after() throwing(IOException e): connect() {
        handleException(thisJoinPoint, e);
    }

    private void handleException(JoinPoint joinPoint, Throwable e) {
        Logger.i("Handling exception");
        if (joinPoint.getTarget() instanceof HttpURLConnection) {
            HttpURLConnection it = (HttpURLConnection) joinPoint.getTarget();
            String header = it.getRequestProperty(TRACKING_HEADER_KEY);
            String url = it.getURL().toString();
            if (isAutoEnabled() && hasTrackingHeader(header) && isNotLibraryCallBoolean(url) && checkTag(header)) {
                try {
                    RemoteCallMarker marker = remoteMarkers.stream().filter(m -> m.headerValue().equals(header)).findFirst().get();
                    marker.endedWith(it, e);
                    remoteMarkers.remove(marker);
                } catch (NoSuchElementException ignored) {
                    // swallow exception
                }
            }
        }
    }
}
