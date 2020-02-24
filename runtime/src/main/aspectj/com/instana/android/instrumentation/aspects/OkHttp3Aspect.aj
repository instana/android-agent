package com.instana.android.instrumentation.aspects;

import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.okhttp.OkHttpGlobalInterceptor;
import okhttp3.OkHttpClient;

/**
 * This aspect adds OkHttp interceptor to the OkHttp builder
 */
public aspect OkHttp3Aspect {
    pointcut builderCall(OkHttpClient.Builder builder): target(builder) && call(* okhttp3.OkHttpClient.Builder.build());
    before(OkHttpClient.Builder builder): builderCall(builder) {
        Logger.i("OkHttp3: adding interceptor to builder");
        if (!builder.interceptors().contains(OkHttpGlobalInterceptor.INSTANCE)) {
            builder.addInterceptor(OkHttpGlobalInterceptor.INSTANCE);
        }
    }

    pointcut clientConstructor(): call(OkHttpClient.new());
    OkHttpClient around(): clientConstructor() {
        Logger.i("OkHttp3: adding interceptor to constructor");
        return new OkHttpClient.Builder().addInterceptor(OkHttpGlobalInterceptor.INSTANCE).build();
    }
}
