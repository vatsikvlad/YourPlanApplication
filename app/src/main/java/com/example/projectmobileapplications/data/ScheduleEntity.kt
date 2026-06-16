package com.example.projectmobileapplications.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startTime: Float,
    val endTime: Float,
    val daysOfWeek: String,
    val specificDate: Long?,
    val isRepeating: Boolean,
    val bodyColor: Int,
    val headerColor: Int,
    val comments: String = "",
    val todoListJson: String = "[]"
)
