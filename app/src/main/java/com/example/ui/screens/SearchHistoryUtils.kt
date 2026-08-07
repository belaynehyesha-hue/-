package com.example.ui.screens

import android.content.Context

object SearchHistoryUtils {
    fun getRecentSearches(context: Context): List<String> {
        val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        val historyString = prefs.getString("history", "") ?: ""
        return if (historyString.isEmpty()) emptyList() else historyString.split(";;;")
    }

    fun saveRecentSearch(context: Context, query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        val historyString = prefs.getString("history", "") ?: ""
        val currentList = if (historyString.isEmpty()) emptyList() else historyString.split(";;;")
        // Remove if already exists to move to top, limit to 8 items
        val newList = listOf(trimmed) + currentList.filter { it != trimmed }
        val cappedList = newList.take(8)
        prefs.edit().putString("history", cappedList.joinToString(";;;")).apply()
    }

    fun clearRecentSearches(context: Context) {
        val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("history").apply()
    }
}
