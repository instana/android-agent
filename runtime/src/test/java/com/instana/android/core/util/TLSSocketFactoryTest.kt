/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import com.instana.android.BaseTest
import com.nhaarman.mockitokotlin2.doReturn
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.net.InetAddress
import javax.net.ssl.SSLSocket

class TLSSocketFactoryTest:BaseTest() {

    @Mock
    lateinit var tlsSocketFactory: TLSSocketFactory

    @Before
    fun `test setup`(){
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket`(){
        val socket = TLSSocketFactory().createSocket()
        assert(socket is SSLSocket)
        if(socket is SSLSocket){
            assert(socket.enabledProtocols.isNotEmpty())
        }
    }

    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket with host and port`(){
        val socket = TLSSocketFactory().createSocket()
        `when`(tlsSocketFactory.createSocket("192.168.1.1",4444)) doReturn socket
        val socket2 = tlsSocketFactory.createSocket("192.168.1.1",4444)
        assert(socket2 is SSLSocket)
        if(socket2 is SSLSocket){
            assert(socket2.enabledProtocols.isNotEmpty())
        }
    }

    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket with socket,host,port,autoclose`(){
        val socket = TLSSocketFactory().createSocket()
        `when`(tlsSocketFactory.createSocket(socket,"192.168.1.1",4444,true)) doReturn socket
        val socket2 = tlsSocketFactory.createSocket(socket,"192.168.1.1",4444,true)
        assert(socket2 is SSLSocket)
        if(socket2 is SSLSocket){
            assert(socket2.enabledProtocols.isNotEmpty())
        }
    }

    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket with host,port,localhost,localport`(){
        val socket = TLSSocketFactory().createSocket()
        `when`(tlsSocketFactory.createSocket("192.168.1.1",4444, InetAddress.getLocalHost(),5555)) doReturn socket
        val socket2 = tlsSocketFactory.createSocket("192.168.1.1",4444, InetAddress.getLocalHost(),5555)
        assert(socket2 is SSLSocket)
        if(socket2 is SSLSocket){
            assert(socket2.enabledProtocols.isNotEmpty())
        }
    }

    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket with inetHost, port`(){
        val socket = TLSSocketFactory().createSocket()
        `when`(tlsSocketFactory.createSocket(InetAddress.getLocalHost(),5555)) doReturn socket
        val socket2 = tlsSocketFactory.createSocket( InetAddress.getLocalHost(),5555)
        assert(socket2 is SSLSocket)
        if(socket2 is SSLSocket){
            assert(socket2.enabledProtocols.isNotEmpty())
        }
    }

    @Test
    fun `test createSocket will call enabledProtocols instances of SSL Socket with address, port, localAddress, localPort`(){
        val socket = TLSSocketFactory().createSocket()
        `when`(tlsSocketFactory.createSocket(InetAddress.getLocalHost(),5555,InetAddress.getLocalHost(),4444)) doReturn socket
        val socket2 = tlsSocketFactory.createSocket(InetAddress.getLocalHost(),5555,InetAddress.getLocalHost(),4444)
        assert(socket2 is SSLSocket)
        if(socket2 is SSLSocket){
            assert(socket2.enabledProtocols.isNotEmpty())
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
    
    @Test
    fun `test getDefaultCipherSuites should not be empty`(){
        assert(TLSSocketFactory().defaultCipherSuites.isNotEmpty())
    }

    @Test
    fun `test getSupportedCipherSuites should not be empty`(){
        assert(TLSSocketFactory().supportedCipherSuites.isNotEmpty())
    }




}