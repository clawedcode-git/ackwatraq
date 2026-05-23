package com.ackwatraq

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.ackwatraq.domain.model.*
import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class ExportImportTest {
    @Test
    fun testGsonLocalDateTimeSerialization() {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, object : com.google.gson.JsonSerializer<LocalDateTime>, com.google.gson.JsonDeserializer<LocalDateTime> {
                override fun serialize(src: LocalDateTime, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
                    return com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                }
                override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): LocalDateTime {
                    return LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                }
            })
            .create()

        val time = LocalDateTime.of(2024, 1, 1, 12, 0)
        val intake = IntakeRecord(id = 1, amountMl = 250, timestamp = time)
        val exportData = ExportData(
            preferences = UserPreferences(),
            intakeRecords = listOf(intake),
            achievements = emptyList(),
            notifications = emptyList()
        )

        val json = gson.toJson(exportData)
        val parsed = gson.fromJson(json, ExportData::class.java)

        assertEquals(exportData.intakeRecords.first().amountMl, parsed.intakeRecords.first().amountMl)
        assertEquals(exportData.intakeRecords.first().timestamp, parsed.intakeRecords.first().timestamp)
    }
}
