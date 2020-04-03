package com.instana.android.instrumentation.aspects;

import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.okhttp3.OkHttp3GlobalInterceptor;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * This aspect adds OkHttp interceptor to the OkHttp builder
 */
public aspect OkHttp3Aspect {
    pointcut builderCall(OkHttpClient.Builder builder): target(builder) && call(* okhttp3.OkHttpClient.Builder.build());
    before(OkHttpClient.Builder builder): builderCall(builder) {
        Logger.i("OkHttp3: adding interceptor to builder");
        if (!builder.interceptors().contains(OkHttp3GlobalInterceptor.INSTANCE)) {
            builder.addInterceptor(OkHttp3GlobalInterceptor.INSTANCE);
        }
    }

    pointcut clientConstructor(): call(OkHttpClient.new());
    OkHttpClient around(): clientConstructor() {
        Logger.i("OkHttp3: adding interceptor to constructor");
        return new OkHttpClient.Builder().addInterceptor(OkHttp3GlobalInterceptor.INSTANCE).build();
    }

    pointcut cancelCall(Call call): target(call) && call(* okhttp3.Call.cancel());
    before(Call call): cancelCall(call) {
        Logger.i("OkHttp3: intercepted single-call cancel");
        OkHttp3GlobalInterceptor.INSTANCE.cancel(call.request());
    }

    pointcut cancelAllCall(Dispatcher dispatcher): target(dispatcher) && call(* okhttp3.Dispatcher.cancelAll());
    before(Dispatcher dispatcher): cancelAllCall(dispatcher) {
        Logger.i("OkHttp3: intercepted dispatcher all-call cancel");
        for (Call call : dispatcher.runningCalls()) {
            OkHttp3GlobalInterceptor.INSTANCE.cancel(call.request());
        }
        for (Call call : dispatcher.queuedCalls()) {
            OkHttp3GlobalInterceptor.INSTANCE.cancel(call.request());
        }
    }
}
