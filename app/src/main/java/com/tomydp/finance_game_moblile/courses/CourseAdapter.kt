package com.tomydp.finance_game_moblile.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.network.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val onCourseClicked: (Int) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course, onCourseClicked)
    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        private val tvCourseDescription: TextView = itemView.findViewById(R.id.tvCourseDescription)
        private val tvCourseProgress: TextView = itemView.findViewById(R.id.tvCourseProgress)

        fun bind(course: Course, onCourseClicked: (Int) -> Unit) {
            tvCourseTitle.text = course.name
            tvCourseDescription.text = course.description

            val completedCount = course.completed_lessons_count ?: 0
            val totalCount = course.lessons_count ?: 0

            if (totalCount > 0) {
                tvCourseProgress.visibility = View.VISIBLE
                tvCourseProgress.text = "$completedCount/$totalCount"
            } else {
                tvCourseProgress.visibility = View.GONE
            }

            itemView.setOnClickListener { onCourseClicked(course.id) }
        }
    }
}