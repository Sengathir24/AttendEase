package com.example.attendance_tracker.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance_tracker.R
import com.example.attendance_tracker.adapters.ClassAdapter
import com.example.attendance_tracker.database.AppDatabase
import com.example.attendance_tracker.database.CourseDayDao

class ClassesListingActivity : AppCompatActivity() {
    private lateinit var courseDayDao: CourseDayDao
    private lateinit var classAdapter: ClassAdapter
    private lateinit var recyclerView: RecyclerView
    private var courseId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classes_listing)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase(this).readableDatabase
        courseDayDao = CourseDayDao(db)

        courseId = intent.getIntExtra("courseId", -1)

        if (courseId != -1) {
            loadClasses()
        }
    }

    override fun onResume() {
        super.onResume()
        // Perform your updates here (reload data, refresh UI, etc.)
        loadClasses() // Example: Reload students list
    }


    private fun loadClasses() {
        val classes = courseDayDao.getCourseDaysByCourseId(courseId)
        if (::classAdapter.isInitialized) {
            classAdapter.updateClasses(classes)
        } else {
            classAdapter = ClassAdapter(classes.toMutableList()) // Convert to MutableList
            recyclerView.adapter = classAdapter
        }
    }


    fun refreshList() {
        loadClasses() // Reloads classes and updates RecyclerView
    }


}
