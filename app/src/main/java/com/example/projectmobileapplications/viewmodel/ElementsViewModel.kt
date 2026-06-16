package com.example.projectmobileapplications.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmobileapplications.data.AppDatabase
import com.example.projectmobileapplications.data.ScheduleEntity
import com.example.projectmobileapplications.notifications.NotificationHelper
import com.example.projectmobileapplications.phone.ScheduleItem
import com.example.projectmobileapplications.phone.TodoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToInt

class ElementsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val dao = AppDatabase.getDatabase(application).scheduleDao()
    private val gson = Gson()
    private val notificationHelper = NotificationHelper(application)

    var isDarkMode by mutableStateOf(prefs.getBoolean("isDarkMode", true))
    var is24HourFormat by mutableStateOf(prefs.getBoolean("is24HourFormat", true))
    var currentLanguage by mutableStateOf(prefs.getString("currentLanguage", "EN") ?: "EN")
    var currentScreen by mutableStateOf("schedule")
    
    var selectedEventForDetails by mutableStateOf<ScheduleItem?>(null)
    
    var eventComments by mutableStateOf("")
    val eventTodoList = mutableStateListOf<TodoItem>()

    val scheduleItems = mutableStateListOf<ScheduleItem>()
    var weekOffset by mutableIntStateOf(0)
    var selectedDayIndex by mutableIntStateOf(
        Calendar.getInstance().let { cal ->
            val d = cal.get(Calendar.DAY_OF_WEEK)
            if (d == Calendar.SUNDAY) 6 else d - 2
        }
    )

    var eventName by mutableStateOf("")
    var isRepeating by mutableStateOf(false)
    var selectedDate by mutableStateOf(Calendar.getInstance())
    var selectedDaysOfWeek by mutableStateOf(setOf<Int>())
    var startTimeHour by mutableIntStateOf(9)
    var startTimeMinute by mutableIntStateOf(0)
    var endTimeHour by mutableIntStateOf(10)
    var endTimeMinute by mutableIntStateOf(0)
    var showCreateEventDialog by mutableStateOf(false)

    var editingItemId by mutableStateOf<String?>(null)
    val isEditing get() = editingItemId != null

    // Conflict Warning States
    var showConflictWarning by mutableStateOf(false)
    var conflictingEvents = mutableStateListOf<ScheduleItem>()

    init {
        viewModelScope.launch {
            dao.getAllItems().collectLatest { entities ->
                scheduleItems.clear()
                scheduleItems.addAll(entities.map { entity ->
                    val todoListType = object : TypeToken<List<TodoItem>>() {}.type
                    val todoList: List<TodoItem> = try {
                        gson.fromJson(entity.todoListJson, todoListType) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    val item = ScheduleItem(
                        id = entity.id,
                        title = entity.title,
                        startTime = entity.startTime,
                        endTime = entity.endTime,
                        daysOfWeek = entity.daysOfWeek.split(",").filter { it.isNotBlank() }.map { it.toInt() },
                        specificDate = entity.specificDate?.let { 
                            Calendar.getInstance().apply { timeInMillis = it }
                        },
                        isRepeating = entity.isRepeating,
                        bodyColor = Color(entity.bodyColor),
                        headerColor = Color(entity.headerColor),
                        comments = entity.comments,
                        todoList = todoList
                    )
                    // Reschedule notifications to keep them up to date
                    notificationHelper.scheduleEventNotifications(item)
                    item
                })
            }
        }
    }

    fun openEventDetails(item: ScheduleItem) {
        selectedEventForDetails = item
    }

    fun toggleTodoItem(todoId: String) {
        selectedEventForDetails?.let { event ->
            val updatedList = event.todoList.map {
                if (it.id == todoId) it.copy(isDone = !it.isDone) else it
            }
            val updatedEvent = event.copy(todoList = updatedList)
            selectedEventForDetails = updatedEvent
            saveUpdatedEvent(updatedEvent)
        }
    }

    private fun saveUpdatedEvent(item: ScheduleItem) {
        val entity = ScheduleEntity(
            id = item.id,
            title = item.title,
            startTime = item.startTime,
            endTime = item.endTime,
            daysOfWeek = item.daysOfWeek.joinToString(","),
            specificDate = item.specificDate?.timeInMillis,
            isRepeating = item.isRepeating,
            bodyColor = item.bodyColor.toArgb(),
            headerColor = item.headerColor.toArgb(),
            comments = item.comments,
            todoListJson = gson.toJson(item.todoList)
        )
        viewModelScope.launch {
            dao.insert(entity)
            notificationHelper.scheduleEventNotifications(item)
        }
    }

    fun startEditing(item: ScheduleItem) {
        editingItemId = item.id
        eventName = item.title
        isRepeating = item.isRepeating
        item.specificDate?.let { selectedDate = it }
        selectedDaysOfWeek = item.daysOfWeek.toSet()
        startTimeHour = item.startTime.toInt()
        startTimeMinute = ((item.startTime - startTimeHour) * 60).roundToInt()
        endTimeHour = item.endTime.toInt()
        endTimeMinute = ((item.endTime - endTimeHour) * 60).roundToInt()
        eventComments = item.comments
        eventTodoList.clear()
        eventTodoList.addAll(item.todoList)
        
        showCreateEventDialog = true
    }

    fun saveEvent(force: Boolean = false) {
        if (eventName.isBlank()) return
        val start = startTimeHour + (startTimeMinute / 60f)
        val end = endTimeHour + (endTimeMinute / 60f)
        val id = editingItemId ?: UUID.randomUUID().toString()

        if (!force) {
            val conflicts = findConflicts(id, start, end)
            if (conflicts.isNotEmpty()) {
                conflictingEvents.clear()
                conflictingEvents.addAll(conflicts)
                showConflictWarning = true
                return
            }
        }

        val entity = ScheduleEntity(
            id = id,
            title = eventName,
            startTime = start,
            endTime = end,
            daysOfWeek = (if (isRepeating) selectedDaysOfWeek.toList() else emptyList()).joinToString(","),
            specificDate = if (!isRepeating) selectedDate.timeInMillis else null,
            isRepeating = isRepeating,
            bodyColor = Color(0xFF8775DB).toArgb(),
            headerColor = Color(0xFFA79EDD).toArgb(),
            comments = eventComments,
            todoListJson = gson.toJson(eventTodoList.toList())
        )

        viewModelScope.launch {
            dao.insert(entity)
            // The flow collector in init will handle scheduling after DB update,
            // but we can also do it here for immediate effect if needed.
            val item = ScheduleItem(
                id = id,
                title = eventName,
                startTime = start,
                endTime = end,
                daysOfWeek = if (isRepeating) selectedDaysOfWeek.toList() else emptyList(),
                specificDate = if (!isRepeating) selectedDate else null,
                isRepeating = isRepeating,
                comments = eventComments,
                todoList = eventTodoList.toList()
            )
            notificationHelper.scheduleEventNotifications(item)
        }

        resetEventForm()
        showCreateEventDialog = false
        showConflictWarning = false
        if (currentScreen == "create_event") {
            currentScreen = "schedule"
        }
    }

    private fun findConflicts(excludeId: String, start: Float, end: Float): List<ScheduleItem> {
        return scheduleItems.filter { item ->
            if (item.id == excludeId) return@filter false
            
            val timeOverlap = start < item.endTime && item.startTime < end
            if (!timeOverlap) return@filter false

            if (isRepeating && item.isRepeating) {
                // Both repeating: check if days overlap
                selectedDaysOfWeek.intersect(item.daysOfWeek.toSet()).isNotEmpty()
            } else if (!isRepeating && !item.isRepeating) {
                // Both specific date: check if same day
                isSameDay(selectedDate, item.specificDate)
            } else if (isRepeating && !item.isRepeating) {
                // New is repeating, existing is specific: check if specific date day matches repeating days
                val dayOfWeek = item.specificDate?.let { 
                    val d = it.get(Calendar.DAY_OF_WEEK)
                    if (d == Calendar.SUNDAY) 6 else d - 2
                }
                dayOfWeek != null && selectedDaysOfWeek.contains(dayOfWeek)
            } else {
                // New is specific, existing is repeating: check if specific date day matches existing repeating days
                val dayOfWeek = selectedDate.let { 
                    val d = it.get(Calendar.DAY_OF_WEEK)
                    if (d == Calendar.SUNDAY) 6 else d - 2
                }
                item.daysOfWeek.contains(dayOfWeek)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar?): Boolean {
        if (cal2 == null) return false
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun resetEventForm() {
        eventName = ""
        isRepeating = false
        selectedDate = Calendar.getInstance()
        selectedDaysOfWeek = emptySet()
        startTimeHour = 9
        startTimeMinute = 0
        endTimeHour = 10
        endTimeMinute = 0
        editingItemId = null
        eventComments = ""
        eventTodoList.clear()
        showConflictWarning = false
        conflictingEvents.clear()
    }

    fun addTodoItem(task: String) {
        eventTodoList.add(TodoItem(task = task))
    }

    fun removeTodoItem(id: String) {
        eventTodoList.removeAll { it.id == id }
    }

    fun toggleTheme(dark: Boolean) {
        isDarkMode = dark
        prefs.edit().putBoolean("isDarkMode", dark).apply()
    }

    fun toggleTimeFormat(is24H: Boolean) {
        is24HourFormat = is24H
        prefs.edit().putBoolean("is24HourFormat", is24H).apply()
    }

    fun changeLanguage(lang: String) {
        currentLanguage = lang
        prefs.edit().putString("currentLanguage", lang).apply()
    }

    fun navigateTo(screen: String) {
        if (screen == "create_event") resetEventForm()
        currentScreen = screen
    }

    fun updateWeekOffset(offset: Int) { weekOffset = offset }
    fun updateSelectedDayIndex(index: Int) { selectedDayIndex = index }
    
    fun deleteItem(id: String) { 
        viewModelScope.launch { 
            val itemToDelete = scheduleItems.find { it.id == id }
            itemToDelete?.let { notificationHelper.cancelNotifications(it) }
            dao.deleteById(id) 
        } 
    }

    fun exportToGoogleCalendar(context: Context, item: ScheduleItem) {
        val startCal = Calendar.getInstance()
        val hour = item.startTime.toInt()
        val minute = ((item.startTime - hour) * 60).toInt()
        
        if (item.isRepeating) {
            // For repeating events, just export today's occurrence or next one
            val dayOfWeek = if (item.daysOfWeek.isNotEmpty()) {
                val todayIdx = Calendar.getInstance().let { if (it.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 6 else it.get(Calendar.DAY_OF_WEEK) - 2 }
                if (todayIdx in item.daysOfWeek) todayIdx else item.daysOfWeek.first()
            } else 0
            val targetDay = if (dayOfWeek == 6) Calendar.SUNDAY else dayOfWeek + 2
            startCal.set(Calendar.DAY_OF_WEEK, targetDay)
        } else {
            item.specificDate?.let { startCal.timeInMillis = it.timeInMillis }
        }
        
        startCal.set(Calendar.HOUR_OF_DAY, hour)
        startCal.set(Calendar.MINUTE, minute)
        
        val endCal = startCal.clone() as Calendar
        val endHour = item.endTime.toInt()
        val endMinute = ((item.endTime - endHour) * 60).toInt()
        endCal.set(Calendar.HOUR_OF_DAY, endHour)
        endCal.set(Calendar.MINUTE, endMinute)

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, item.title)
            .putExtra(CalendarContract.Events.DESCRIPTION, item.comments)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.timeInMillis)
            .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

        context.startActivity(intent)
    }
}
