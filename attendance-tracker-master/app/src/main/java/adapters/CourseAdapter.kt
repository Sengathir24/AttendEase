package com.example.attendance_tracker.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance_tracker.R
import com.example.attendance_tracker.database.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val onCourseClick: (Int) -> Unit,
    private val onExportClick: (Int, () -> Unit) -> Unit,  // Accept callback for export
    private val onDeleteClick: (Int, Boolean) -> Unit       // Accept export flag for delete
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textCourseName: TextView = view.findViewById(R.id.textCourseName)
        private val textCourseDate: TextView = view.findViewById(R.id.textCourseDate)
        private val btnExport: Button = view.findViewById(R.id.btnExport)
        private val btnDelete: Button = view.findViewById(R.id.btnDelete)

        fun bind(course: Course) {
            textCourseName.text = course.courseName
            textCourseDate.text = "${course.startDate} - ${course.endDate}"

            itemView.setOnClickListener {
                onCourseClick(course.courseId)
            }

            btnExport.setOnClickListener {
                AlertDialog.Builder(it.context)
                    .setTitle("Export Confirmation")
                    .setMessage("Are you sure you want to export this course?")
                    .setPositiveButton("Yes") { _, _ ->
                        onExportClick(course.courseId) {
                            Toast.makeText(it.context, "Export Completed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            btnDelete.setOnClickListener {
                val context = it.context
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirm, null)
                val checkBoxExport = dialogView.findViewById<CheckBox>(R.id.checkbox_export)

                AlertDialog.Builder(context)
                    .setTitle("Delete Confirmation")
                    .setView(dialogView)
                    .setPositiveButton("Continue") { _, _ ->
                        onDeleteClick(course.courseId, checkBoxExport.isChecked)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size
}


