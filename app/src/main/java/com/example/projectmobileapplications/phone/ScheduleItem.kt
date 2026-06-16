package com.example.projectmobileapplications.phone

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToInt

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val task: String,
    val isDone: Boolean = false
)

data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTime: Float,
    val endTime: Float,
    val daysOfWeek: List<Int> = emptyList(),
    val specificDate: Calendar? = null,
    val isRepeating: Boolean = true,
    val bodyColor: Color = Color(0xFF8775DB),
    val headerColor: Color = Color(0xFFA79EDD),
    val comments: String = "",
    val todoList: List<TodoItem> = emptyList()
) {
    val duration: Float get() = endTime - startTime
    val timeLabel: String get() = "${formatTime(startTime)} - ${formatTime(endTime)}"

    fun getAmPmTimeLabel(): String {
        return "${formatAmPm(startTime)} - ${formatAmPm(endTime)}"
    }

    private fun formatTime(time: Float): String {
        val hours = time.toInt()
        val minutes = ((time - hours) * 60).roundToInt()
        return "%d:%02d".format(hours, minutes)
    }

    private fun formatAmPm(time: Float): String {
        val hours24 = time.toInt()
        val minutes = ((time - hours24) * 60).roundToInt()
        val amPm = if (hours24 < 12) "AM" else "PM"
        val hours12 = when {
            hours24 == 0 -> 12
            hours24 > 12 -> hours24 - 12
            else -> hours24
        }
        return "%d:%02d %s".format(hours12, minutes, amPm)
    }
}
