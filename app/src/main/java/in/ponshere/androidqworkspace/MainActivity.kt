package `in`.ponshere.androidqworkspace

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.bubbles.data.NotificationHelper

class MainActivity : AppCompatActivity() {

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper(this). apply {
            setUpNotificationChannels()
            showNotification("Test", false)
        }

    }
}
