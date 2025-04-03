package com.example.attendance_tracker.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.attendance_tracker.R
import com.example.attendance_tracker.adapters.CourseAdapter
import com.example.attendance_tracker.database.AppDatabase
import com.example.attendance_tracker.database.CourseDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import org.apache.poi.ss.usermodel.FillPatternType
import java.io.FileOutputStream
import java.io.File
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CourseListingActivity : AppCompatActivity() {
    private lateinit var courseDao: CourseDao
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textNoCourses: TextView
    private lateinit var fabAddCourse: FloatingActionButton
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_listing)

        courseDao = CourseDao(this)
        recyclerView = findViewById(R.id.recyclerView)
        textNoCourses = findViewById(R.id.textNoCourses)
        fabAddCourse = findViewById(R.id.fabAddCourse)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Register ActivityResultLauncher for adding courses
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadCourses()
            }
        }

        loadCourses()

        fabAddCourse.setOnClickListener {
            val intent = Intent(this, NewCourseActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    }

    private fun loadCourses() {
        val courses = courseDao.getAllCourses()
        courseAdapter = CourseAdapter(courses,
            onCourseClick = { courseId ->
                val intent = Intent(this, ClassesListingActivity::class.java)
                intent.putExtra("courseId", courseId)
                startActivity(intent)
            },
            onExportClick = { courseId, callback -> exportToExcel(courseId, callback) },
            onDeleteClick = { courseId, shouldExport -> deleteCourse(courseId, shouldExport) }
        )
        recyclerView.adapter = courseAdapter
        textNoCourses.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun exportToExcel(courseId: Int, callback: () -> Unit) {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show() // Show loader when export starts

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase(this@CourseListingActivity).readableDatabase

                // Fetch course name
                var courseName = "Course_$courseId"
                db.rawQuery("SELECT course_name FROM Course WHERE course_id = ?", arrayOf(courseId.toString())).use { cursor ->
                    if (cursor.moveToFirst()) {
                        courseName = cursor.getString(0).replace(" ", "_") // Remove spaces
                    }
                }

                // Generate file name with timestamp
                val timestamp = System.currentTimeMillis()
                val fileName = "${courseName}_$timestamp.xlsx"

                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(dir, fileName)

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Attendance")

                // Define font styles
                val redFont = workbook.createFont().apply { color = IndexedColors.RED.index }
                val greenFont = workbook.createFont().apply { color = IndexedColors.GREEN.index }
                val yellowFont = workbook.createFont().apply { color = IndexedColors.DARK_YELLOW.index }

                val redStyle = workbook.createCellStyle().apply { setFont(redFont) }
                val greenStyle = workbook.createCellStyle().apply { setFont(greenFont) }
                val yellowStyle = workbook.createCellStyle().apply { setFont(yellowFont) }

                // Fetch all dates
                val dates = mutableListOf<String>()
                db.rawQuery("SELECT DISTINCT date FROM CourseDay WHERE course_id = ?", arrayOf(courseId.toString())).use { cursor ->
                    while (cursor.moveToNext()) {
                        dates.add(cursor.getString(0))
                    }
                }

                // Create header row
                val headerRow = sheet.createRow(0)
                headerRow.createCell(0).setCellValue("Roll No")
                headerRow.createCell(1).setCellValue("Name")
                dates.forEachIndexed { index, date -> headerRow.createCell(index + 2).setCellValue(date) }

                // Fetch student data
                db.rawQuery("SELECT roll_no, name FROM Student WHERE course_id = ? GROUP BY roll_no", arrayOf(courseId.toString())).use { cursor ->
                    var rowNum = 1
                    while (cursor.moveToNext()) {
                        val row = sheet.createRow(rowNum++)
                        row.createCell(0).setCellValue(cursor.getString(0)) // Roll No
                        row.createCell(1).setCellValue(cursor.getString(1)) // Name

                        dates.forEachIndexed { index, date ->
                            db.rawQuery(
                                """SELECT present, 
                            (SELECT attendance_taken FROM CourseDay WHERE date = ? AND course_id = ? LIMIT 1)
                        FROM Student WHERE roll_no = ? AND course_id = ? AND day_id IN 
                            (SELECT day_id FROM CourseDay WHERE date = ?)""",
                                arrayOf(date, courseId.toString(), cursor.getString(0), courseId.toString(), date)
                            ).use { attCursor ->
                                val cell = row.createCell(index + 2)

                                if (attCursor.moveToFirst()) {
                                    val present = attCursor.getInt(0)
                                    val attendanceTaken = attCursor.getInt(1)

                                    if (attendanceTaken == 0) {
                                        cell.setCellValue("NT")
                                        cell.cellStyle = yellowStyle
                                    } else {
                                        if (present == 1) {
                                            cell.setCellValue("P")
                                            cell.cellStyle = greenStyle
                                        } else {
                                            cell.setCellValue("A")
                                            cell.cellStyle = redStyle
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FileOutputStream(file).use { workbook.write(it) }
                workbook.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CourseListingActivity, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    openExcelFile(file)
                    callback()
                    loadingDialog.dismiss() // Dismiss loader after export completes
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CourseListingActivity, "Error exporting file", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss() // Dismiss loader in case of error
                }
            }
        }
    }




    private fun openExcelFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found to open the file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCourse(courseId: Int, shouldExport: Boolean) {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show() // Show loader when deletion starts

        if (shouldExport) {
            exportToExcel(courseId) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Final Confirmation")
                        .setMessage("Export successful. Are you sure you want to delete this course?")
                        .setPositiveButton("Yes") { _, _ ->
                            proceedWithDeletion(courseId, loadingDialog)
                        }
                        .setNegativeButton("No") { _, _ ->
                            loadingDialog.dismiss() // Dismiss loader if user cancels deletion
                        }
                        .show()
                }
            }
        } else {
            proceedWithDeletion(courseId, loadingDialog)
        }
    }

    private fun proceedWithDeletion(courseId: Int, loadingDialog: LoadingDialog) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase(this@CourseListingActivity).writableDatabase
            val result = db.delete("Course", "course_id = ?", arrayOf(courseId.toString()))
            db.close()

            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(this@CourseListingActivity, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                    loadCourses()
                } else {
                    Toast.makeText(this@CourseListingActivity, "Failed to delete course", Toast.LENGTH_SHORT).show()
                }
                loadingDialog.dismiss() // Dismiss loader when operation completes
            }
        }
    }

}


