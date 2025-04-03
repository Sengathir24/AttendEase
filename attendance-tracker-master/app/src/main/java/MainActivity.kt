package com.example.attendance_tracker
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.attendance_tracker.activities.CourseListingActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to CourseListingActivity
        val intent = Intent(this, CourseListingActivity::class.java)
        startActivity(intent)
        finish()
    }
}
