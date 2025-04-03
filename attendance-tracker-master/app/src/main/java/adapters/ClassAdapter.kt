package com.example.attendance_tracker.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance_tracker.R
import com.example.attendance_tracker.database.CourseDay
import android.graphics.Color
import com.example.attendance_tracker.activities.AttendanceNotTakenStudentListing
import android.content.Intent
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.attendance_tracker.activities.ClassesListingActivity
import com.example.attendance_tracker.database.AppDatabase
import com.example.attendance_tracker.database.CourseDayDao
class ClassAdapter(
    private var classes: MutableList<CourseDay> // Use MutableList
) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    inner class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDayName: TextView = view.findViewById(R.id.textDayName)
        val textDate: TextView = view.findViewById(R.id.textDate)
        val textHours: TextView = view.findViewById(R.id.textHours)
        val cardView: CardView = view as CardView
        val btnView: Button = view.findViewById(R.id.btnView)
        val btnHoliday: Button = view.findViewById(R.id.btnHoliday)

        fun bind(courseDay: CourseDay) {
            textDayName.text = courseDay.dayName
            textDate.text = courseDay.date
            textHours.text = "Hours: ${courseDay.hours}"

            if (courseDay.isHoliday) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2")) // Light Red
                btnHoliday.text = "Remove Holiday"
            } else {
                val cardColor = if (courseDay.attendanceTaken) {
                    Color.parseColor("#C8E6C9") // Light Green
                } else {
                    Color.parseColor("#FFF59D") // Light Yellow
                }
                cardView.setCardBackgroundColor(cardColor)
                btnHoliday.text = "Mark as Holiday"
            }

            // View button logic
            btnView.setOnClickListener {
                if (courseDay.isHoliday) {
                    showRemarkDialog(it.context, courseDay.remark ?: "No remark provided")
                } else {
                    val intent = Intent(it.context, AttendanceNotTakenStudentListing::class.java)
                    intent.putExtra("dayId", courseDay.dayId)
                    intent.putExtra("courseId", courseDay.courseId)
                    it.context.startActivity(intent)
                }
            }

            btnHoliday.setOnClickListener {
                showHolidayDialog(it.context, courseDay, adapterPosition)
            }
        }


        private fun updateUI(courseDay: CourseDay) {
            if (courseDay.isHoliday) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2"))  // Light Red
                btnView.isEnabled = false
                btnHoliday.text = "Remove Holiday"
            } else {
                val cardColor = if (courseDay.attendanceTaken) {
                    Color.parseColor("#C8E6C9") // Light Green
                } else {
                    Color.parseColor("#FFF59D") // Light Yellow
                }
                cardView.setCardBackgroundColor(cardColor)
                btnView.isEnabled = true
                btnHoliday.text = "Mark as Holiday"
            }
        }

        private fun showHolidayDialog(context: Context, courseDay: CourseDay, position: Int) {
            val builder = AlertDialog.Builder(context)

            // Inflate custom layout
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_holiday, null)
            builder.setView(dialogView)

            // Get reference to UI elements
            val textTitle: TextView = dialogView.findViewById(R.id.textHolidayTitle)
            val editRemark: EditText = dialogView.findViewById(R.id.editRemark)

            textTitle.text = if (courseDay.isHoliday) "Remove Holiday" else "Mark as Holiday"

            if (courseDay.isHoliday) {
                editRemark.setText(courseDay.remark)
            }

            builder.setPositiveButton(if (courseDay.isHoliday) "Remove" else "Mark") { _, _ ->
                val remark = if (courseDay.isHoliday) null else editRemark.text.toString().trim()
                val newHolidayStatus = !courseDay.isHoliday

                val database = AppDatabase(context).writableDatabase
                val courseDayDao = CourseDayDao(database)
                courseDayDao.setHolidayStatus(courseDay.dayId, newHolidayStatus, remark)

                // Update UI and notify adapter
                classes[position] = classes[position].copy(isHoliday = newHolidayStatus, remark = remark)
                notifyItemChanged(position)
            }

            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

    }
    private fun showRemarkDialog(context: Context, remark: String) {
        AlertDialog.Builder(context)
            .setTitle("Holiday Remark")
            .setMessage(remark)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(classes[position])
    }

    override fun getItemCount() = classes.size

    fun updateClasses(newClasses: List<CourseDay>) {
        classes.clear()
        classes.addAll(newClasses)
        notifyDataSetChanged()
    }
}



