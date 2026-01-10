package de.felixdieterle.babaphone.network

import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class AudioStreamManager {
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isRunning = AtomicBoolean(false)
    
    companion object {
        private const val TAG = "AudioStreamManager"
    }
    
    interface AudioDataListener {
        fun onAudioDataReceived(data: ByteArray, size: Int)
    }
    
    private var audioDataListener: AudioDataListener? = null
    
    fun setAudioDataListener(listener: AudioDataListener) {
        audioDataListener = listener
    }
    
    // Child mode: Start server and send audio
    fun startServer(port: Int, onReady: () -> Unit) {
        Thread {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Server started on port $port")
                onReady()
                
                isRunning.set(true)
                while (isRunning.get()) {
                    try {
                        // Close previous client if exists
                        clientSocket?.let {
                            if (!it.isClosed) {
                                it.close()
                            }
                        }
                        
                        clientSocket = serverSocket?.accept()
                        Log.d(TAG, "Client connected")
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.e(TAG, "Error accepting connection", e)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server", e)
            }
        }.start()
    }
    
    fun sendAudioData(data: ByteArray) {
        try {
            clientSocket?.getOutputStream()?.write(data)
        } catch (e: java.net.SocketException) {
            // Client disconnected, expected during normal operation
            Log.d(TAG, "Client disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending audio data", e)
        }
    }
    
    // Parent mode: Connect to child and receive audio
    fun connectToDevice(address: String, port: Int, onConnected: () -> Unit) {
        Thread {
            try {
                clientSocket = Socket(address, port)
                Log.d(TAG, "Connected to $address:$port")
                onConnected()
                
                isRunning.set(true)
                val inputStream = clientSocket?.getInputStream()
                val buffer = ByteArray(4096)
                
                while (isRunning.get()) {
                    try {
                        val bytesRead = inputStream?.read(buffer) ?: -1
                        if (bytesRead > 0) {
                            audioDataListener?.onAudioDataReceived(buffer, bytesRead)
                        } else if (bytesRead == -1) {
                            break
                        }
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.e(TAG, "Error receiving data", e)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to device", e)
            }
        }.start()
    }
    
    fun stop() {
        isRunning.set(false)
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping stream", e)
        }
        clientSocket = null
        serverSocket = null
    }
}
