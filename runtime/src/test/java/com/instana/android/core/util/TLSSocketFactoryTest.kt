/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import com.instana.android.BaseTest
import org.junit.Assert
import org.junit.Test
import javax.net.ssl.SSLSocket

class TLSSocketFactoryTest:BaseTest() {


    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket`(){
        val socket = TLSSocketFactory().createSocket()
        assert(socket is SSLSocket)
        if(socket is SSLSocket){
            assert(socket.enabledProtocols.contains("SSLv3"))
            assert(socket.enabledProtocols.contains("TLSv1"))
            assert(socket.enabledProtocols.contains("TLSv1.1"))
        }
    }


    @Test
    fun `test getTrustManagers should not return null`(){
        val trustManagers = TLSSocketFactory.getTrustManagers()
        Assert.assertNotNull(trustManagers)
    }

    @Test
    fun `test newInsecureSocketFactory should have TLS value`(){
        val (insecureSocketFactory, _) = TLSSocketFactory.newInsecureSocketFactory()
        val sslContextProtocol = insecureSocketFactory.supportedCipherSuites
        val size = sslContextProtocol.filter { it.contains("TLS") }.size
        assert(size>0)
    }




}