package com.example.ev3controller

import android.bluetooth.BluetoothDevice
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.example.ev3controller.EV3Comm

class GamepadHandler(private val ev3Comm: EV3Comm) {
    private var connectedGamepad: BluetoothDevice? = null
    private var isGamepadConnected = false

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (!isGamepadInput(event)) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> {
                ev3Comm.moveForward(50)
                true
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                ev3Comm.moveBackward(50)
                true
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                ev3Comm.turnLeft(90)
                true
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                ev3Comm.turnRight(90)
                true
            }
            KeyEvent.KEYCODE_BUTTON_SELECT -> {
                ev3Comm.stopMotors()
                true
            }
            else -> false
        }
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if (!isGamepadInput(event)) return false

        // Handle joystick input
        val leftStickX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftStickY = event.getAxisValue(MotionEvent.AXIS_Y)
        
        // Process joystick values and convert to EV3 commands
        processJoystickInput(leftStickX, leftStickY)
        
        return true
    }

    private fun processJoystickInput(x: Float, y: Float) {
        val threshold = 0.2f
        val speed = (Math.abs(y) * 100).toInt()
        
        when {
            y < -threshold -> ev3Comm.moveForward(speed)
            y > threshold -> ev3Comm.moveBackward(speed)
            x < -threshold -> ev3Comm.turnLeft(15)
            x > threshold -> ev3Comm.turnRight(15)
            else -> ev3Comm.stopMotors()
        }
    }

    private fun isGamepadInput(event: KeyEvent): Boolean {
        return (event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
    }

    private fun isGamepadInput(event: MotionEvent): Boolean {
        return (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }

    fun connectGamepad(device: BluetoothDevice) {
        connectedGamepad = device
        isGamepadConnected = true
    }

    fun disconnectGamepad() {
        connectedGamepad = null
        isGamepadConnected = false
    }

    fun isConnected(): Boolean = isGamepadConnected
}