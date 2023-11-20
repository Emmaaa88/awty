package edu.uw.ischool.yc324.awty

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var editTextMessage: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextInterval: EditText
    private lateinit var buttonStartStop: Button

    private var isNagging = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)

        editTextMessage = findViewById(R.id.editTextMessage)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        editTextInterval = findViewById(R.id.editTextInterval)
        buttonStartStop = findViewById(R.id.buttonStartStop)

        buttonStartStop.setOnClickListener {
            if (isNagging) {
                stopNagging()
            } else {
                validateAndStartNagging()
            }
        }
    }

    private fun validateAndStartNagging() {
        val message = editTextMessage.text.toString()
        val phoneNumber = editTextPhoneNumber.text.toString()
        val intervalText = editTextInterval.text.toString()

        when {
            message.isBlank() -> showToast("Please enter a message.")
            phoneNumber.isBlank() -> showToast("Please enter a phone number.")
            intervalText.isBlank() -> showToast("Please enter an interval.")
            intervalText.toIntOrNull() ?: 0 <= 0 -> showToast("Interval must be a positive integer.")
            else -> startNagging(message, phoneNumber, intervalText.toInt())
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startNagging(message: String, phoneNumber: String, interval: Int) {
        isNagging = true
        buttonStartStop.text = "Stop"

        runnable = object : Runnable {
            override fun run() {
                sendSms(phoneNumber, message)
                handler.postDelayed(this, interval * 60000L)
            }
        }
        handler.post(runnable)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    private fun sendAudio(phoneNumber: String, audioFileUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, audioFileUri)
            putExtra("address", phoneNumber)
        }
        startActivity(Intent.createChooser(intent, "Send Audio"))
    }

    private fun sendVideo(phoneNumber: String, videoFileUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, videoFileUri)
            putExtra("address", phoneNumber)
        }
        startActivity(Intent.createChooser(intent, "Send Video"))
    }

    private fun stopNagging() {
        isNagging = false
        buttonStartStop.text = "Start"
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isNagging) {
            handler.removeCallbacks(runnable)
        }
    }
}