/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.annotation.SuppressLint
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.*

class TLSSocketFactory : SSLSocketFactory() {

    private val internalSSLSocketFactory: SSLSocketFactory

    override fun getDefaultCipherSuites(): Array<String> {
        return internalSSLSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return internalSSLSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket())
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket): Socket {
        // TLSv1.2 is disabled by default for API 16-19. Need to manually enable it
        // https://developer.android.com/reference/javax/net/ssl/SSLSocket.html#protocols
        if (socket is SSLSocket) {
            val supportedProtocols = socket.supportedProtocols
            socket.enabledProtocols = supportedProtocols
        }
        return socket
    }

    init {
        val context = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        internalSSLSocketFactory = context.socketFactory
    }

    companion object {
        fun getTrustManagers(): X509TrustManager? {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            return trustManagerFactory.trustManagers
                .mapNotNull { it as? X509TrustManager }
                .firstOrNull()
        }

        fun newInsecureSocketFactory(): Pair<SSLSocketFactory, InsecureTrustAllManager> {
            val insecureTrustAllManager = InsecureTrustAllManager()
            val insecureSocketFactory = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(insecureTrustAllManager), null)
            }.socketFactory

            return Pair(insecureSocketFactory, insecureTrustAllManager)
        }

        @SuppressLint("CustomX509TrustManager")
        class InsecureTrustAllManager : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        }
    }
}
