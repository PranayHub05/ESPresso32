package com.pranay.espresso32.data.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.pranay.espresso32.data.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class DiscoveryService {
    fun discoverDevices(context: Context): Flow<DeviceInfo> = callbackFlow {
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val ip = serviceInfo.host.hostAddress ?: return
                val port = serviceInfo.port
                trySend(DeviceInfo(name = serviceInfo.serviceName, ipAddress = ip, port = port))
            }
        }

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onServiceFound(service: NsdServiceInfo) {
                nsdManager.resolveService(service, resolveListener)
            }
            override fun onServiceLost(service: NsdServiceInfo) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
        }

        try {
            nsdManager.discoverServices("_ws._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            // Ignore if NSD fails
        }

        val udpJob = launch(Dispatchers.IO) {
            try {
                DatagramSocket().use { socket ->
                    socket.broadcast = true
                    val sendData = "ESP32_DISCOVER".toByteArray()
                    val packet = DatagramPacket(sendData, sendData.size, InetAddress.getByName("255.255.255.255"), 4210)
                    socket.send(packet)
                    
                    val receiveData = ByteArray(1024)
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    while (isActive) {
                        socket.receive(receivePacket)
                        val response = String(receivePacket.data, 0, receivePacket.length)
                        if (response.startsWith("ESP32_DEVICE")) {
                            val parts = response.split("|")
                            if (parts.size >= 4) {
                                trySend(DeviceInfo(name = parts[2], ipAddress = parts[1], port = parts[3].toIntOrNull() ?: 81))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore UDP errors
            }
        }

        launch {
            delay(10_000)
            close()
        }

        awaitClose {
            udpJob.cancel()
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
