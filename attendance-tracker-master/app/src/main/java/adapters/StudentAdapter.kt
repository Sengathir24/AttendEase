package com.example.attendance_tracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance_tracker.R
import com.example.attendance_tracker.database.Student
import android.graphics.Color

class StudentAdapter(
    private val context: Context,
    private val students: MutableList<Student>, // Change to MutableList to modify it
    private val updateStudentStatus: (Student) -> Unit // Callback for updating the database
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    inner class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentCard: CardView = view.findViewById(R.id.studentCard)
        val textName: TextView = view.findViewById(R.id.textName)
        val toggleButton: ToggleButton = view.findViewById(R.id.toggleButton)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        // Set text and toggle button state
        holder.textName.text = "${student.name} (${student.rollNo})"

        // Remove listener before setting isChecked to prevent unnecessary updates
        holder.toggleButton.setOnCheckedChangeListener(null)

        holder.toggleButton.isChecked = student.present
        updateCardColor(holder, student.present)

        // Reattach listener AFTER setting state
        holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (student.present != isChecked) {  // Update only if changed
                student.present = isChecked
                updateCardColor(holder, isChecked)
                updateStudentStatus(student)  // Save to database
            }
        }
    }


    private fun updateCardColor(holder: StudentViewHolder, isPresent: Boolean) {
        val color = if (isPresent) "#C8E6C9" else "#FFCDD2" // Green for present, Red for absent
        holder.studentCard.setCardBackgroundColor(Color.parseColor(color))
    }



    override fun getItemCount() = students.size
}
