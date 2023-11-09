package edu.uw.ischool.yc324.awty

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
                showCustomToast(phoneNumber, message)
                handler.postDelayed(this, interval * 60000L)
            }
        }
        handler.post(runnable)
    }

    private fun showCustomToast(phoneNumber: String, message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast, findViewById(R.id.custom_toast_container))

        val textTitle = layout.findViewById<TextView>(R.id.toast_title)
        textTitle.text = getString(R.string.texting_caption, phoneNumber)

        val textBody = layout.findViewById<TextView>(R.id.toast_body)
        textBody.text = message

        with (Toast(applicationContext)) {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
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
