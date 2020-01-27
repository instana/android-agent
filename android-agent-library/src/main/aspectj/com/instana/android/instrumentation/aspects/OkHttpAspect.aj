package com.instana.android.instrumentation.aspects;

import com.instana.android.core.util.Logger;
import com.instana.android.instrumentation.okhttp.OkHttpGlobalInterceptor;
import okhttp3.OkHttpClient;

/**
 * This aspect adds OkHttp interceptor to the OkHttp builder
 */
aspect OkHttpAspect {
    pointcut builderCall(OkHttpClient.Builder builder): target(builder) && call(OkHttpClient.Builder.new());

    after(OkHttpClient.Builder builder) returning: builderCall(builder) {
        Logger.i("Adding interceptor to OkHttp");
        if (!builder.interceptors().contains(OkHttpGlobalInterceptor.INSTANCE)) {
            builder.addInterceptor(OkHttpGlobalInterceptor.INSTANCE);
        }
    }

}
