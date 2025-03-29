package com.example.ev3controller

import android.util.Log
import java.io.*
import java.net.Socket

class EV3Comm(private val ipAddress: String, private val port: Int) {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    companion object {
        private const val TAG = "EV3Comm"
        private const val CONNECTION_TIMEOUT = 5000
    }

    fun connect(): Boolean {
        return try {
            socket = Socket(ipAddress, port)
            socket?.soTimeout = CONNECTION_TIMEOUT
            outputStream = socket?.getOutputStream()
            inputStream = socket?.getInputStream()
            reader = BufferedReader(InputStreamReader(inputStream))
            writer = PrintWriter(outputStream, true)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            disconnect()
            false
        }
    }

    fun disconnect() {
        try {
            reader?.close()
            writer?.close()
            outputStream?.close()
            inputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        } finally {
            reader = null
            writer = null
            outputStream = null
            inputStream = null
            socket = null
        }
    }

    fun sendCommand(command: String): String? {
        return try {
            writer?.println(command)
            writer?.flush()
            reader?.readLine()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending command: ${e.message}")
            null
        }
    }

    fun isConnected(): Boolean {
        return socket?.isConnected ?: false
    }

    // EV3 Specific Commands
    fun moveForward(speed: Int): String? {
        return sendCommand("MOVE_FORWARD:$speed")
    }

    fun moveBackward(speed: Int): String? {
        return sendCommand("MOVE_BACKWARD:$speed")
    }

    fun turnLeft(degrees: Int): String? {
        return sendCommand("TURN_LEFT:$degrees")
    }

    fun turnRight(degrees: Int): String? {
        return sendCommand("TURN_RIGHT:$degrees")
    }

    fun stopMotors(): String? {
        return sendCommand("STOP")
    }

    fun getSensorValue(sensorPort: Int): String? {
        return sendCommand("GET_SENSOR:$sensorPort")
    }
}