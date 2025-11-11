package com.tomydp.finance_game_moblile.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.network.Lesson

class LessonAdapter(private val lessons: List<Lesson>) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLessonTitle: TextView = itemView.findViewById(R.id.tvLessonTitle)

        fun bind(lesson: Lesson) {
            tvLessonTitle.text = lesson.title
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = android.content.Intent(context, com.tomydp.finance_game_moblile.exercises.ExerciseActivity::class.java)
                intent.putExtra("LESSON_ID", lesson.id)
                context.startActivity(intent)
            }
        }
    }
}