package com.virtualshape.gradule.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.Parity
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.Source
import com.virtualshape.gradule.domain.schedule.Subgroup
import com.virtualshape.gradule.domain.schedule.Timeslot
import java.time.DayOfWeek
import java.time.LocalDate

private const val DB_NAME = "gradule.db"
private const val DB_VERSION = 1
private const val TABLE_ENTRIES = "entries"
private const val TABLE_SUBGROUPS = "subgroups"

/**
 * Тонкая SQLite-реализация [ScheduleStore]. Записи + подгруппы в двух таблицах.
 * Upsert = update по ключу, затем insert с CONFLICT_IGNORE; REPLACE/CASCADE не используются,
 * иначе вместе с записью снесутся заметки (e04).
 * ponytail: в этом срезе только собирается; интеграционные тесты и заметки — вместе с e04.
 */
class SqliteScheduleStore(
    context: Context,
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION),
    ScheduleStore {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ENTRIES (
                key TEXT PRIMARY KEY NOT NULL,
                source TEXT NOT NULL,
                day INTEGER NOT NULL,
                start_minute INTEGER NOT NULL,
                end_minute INTEGER NOT NULL,
                parity TEXT NOT NULL,
                info TEXT NOT NULL DEFAULT '',
                once_date TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_SUBGROUPS (
                entry_key TEXT NOT NULL,
                ordinal INTEGER NOT NULL,
                id INTEGER NOT NULL,
                subnum INTEGER NOT NULL,
                subject_name TEXT NOT NULL,
                subject_abbr TEXT NOT NULL,
                teacher_name TEXT NOT NULL,
                room_name TEXT NOT NULL,
                PRIMARY KEY (entry_key, ordinal)
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) = Unit

    override fun entries(): List<ScheduleEntry> {
        val entries = mutableListOf<ScheduleEntry>()
        readableDatabase.query(TABLE_ENTRIES, null, null, null, null, null, "start_minute ASC").use { cursor ->
            while (cursor.moveToNext()) {
                val key = decodeKey(cursor.getString(0))
                val onceDate = cursor.getString(7)?.let { LocalDate.parse(it) }
                entries +=
                    ScheduleEntry(
                        key = key,
                        source = Source.valueOf(cursor.getString(1)),
                        timeslot =
                            Timeslot(
                                DayOfWeek.of(cursor.getInt(2)),
                                cursor.getInt(3),
                                cursor.getInt(4),
                                Parity.valueOf(cursor.getString(5)),
                            ),
                        subgroups = subgroupsOf(cursor.getString(0)),
                        info = cursor.getString(6),
                        onceDate = onceDate,
                    )
            }
        }
        return entries
    }

    override fun upsert(entries: List<ScheduleEntry>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (entry in entries) {
                val key = encodeKey(entry.key)
                val updated =
                    db.update(
                        TABLE_ENTRIES,
                        entry.toValues(),
                        "key = ?",
                        arrayOf(key),
                    )
                if (updated == 0) {
                    db.insertWithOnConflict(TABLE_ENTRIES, null, entry.toValues(), SQLiteDatabase.CONFLICT_IGNORE)
                }
                db.delete(TABLE_SUBGROUPS, "entry_key = ?", arrayOf(key))
                for ((ordinal, subgroup) in entry.subgroups.withIndex()) {
                    db.insertWithOnConflict(TABLE_SUBGROUPS, null, subgroup.toValues(key, ordinal), SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun delete(keys: List<EntryKey>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (key in keys) {
                val encoded = encodeKey(key)
                db.delete(TABLE_SUBGROUPS, "entry_key = ?", arrayOf(encoded))
                db.delete(TABLE_ENTRIES, "key = ?", arrayOf(encoded))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Подгруппы одной записи; сортировка по ordinal сохраняет порядок из domain-списка. */
    private fun subgroupsOf(entryKey: String): List<Subgroup> {
        val subgroups = mutableListOf<Subgroup>()
        readableDatabase.query(TABLE_SUBGROUPS, null, "entry_key = ?", arrayOf(entryKey), null, null, "ordinal ASC").use { cursor ->
            while (cursor.moveToNext()) {
                subgroups.add(
                    Subgroup(
                        id = cursor.getLong(2),
                        subnum = cursor.getInt(3),
                        subjectName = cursor.getString(4),
                        subjectAbbr = cursor.getString(5),
                        teacherName = cursor.getString(6),
                        roomName = cursor.getString(7),
                    ),
                )
            }
        }
        return subgroups
    }

    private fun ScheduleEntry.toValues() =
        ContentValues().apply {
            put("key", encodeKey(key))
            put("source", source.name)
            put("day", timeslot.day.value)
            put("start_minute", timeslot.startMinute)
            put("end_minute", timeslot.endMinute)
            put("parity", timeslot.parity.name)
            put("info", info)
            put("once_date", onceDate?.toString())
        }

    private fun Subgroup.toValues(
        entryKey: String,
        ordinal: Int,
    ) = ContentValues().apply {
        put("entry_key", entryKey)
        put("ordinal", ordinal)
        put("id", id)
        put("subnum", subnum)
        put("subject_name", subjectName)
        put("subject_abbr", subjectAbbr)
        put("teacher_name", teacherName)
        put("room_name", roomName)
    }

    private fun encodeKey(key: EntryKey): String =
        when (key) {
            is EntryKey.Server -> "S:$key.lessonId:$key.curriculumId"
            is EntryKey.Local -> "L:$key.uuid"
            is EntryKey.Derived -> "D:$key.day:${key.startMinute}:${key.parity}:${key.subjectAbbr}:${key.subnum}"
        }

    private fun decodeKey(encoded: String): EntryKey {
        val parts = encoded.split(':')
        return when (parts.first()) {
            "S" -> {
                EntryKey.Server(parts[1].toLong(), parts[2].toLong())
            }

            "L" -> {
                EntryKey.Local(parts.drop(1).joinToString(":"))
            }

            else -> {
                EntryKey.Derived(
                    DayOfWeek.of(parts[1].toInt()),
                    parts[2].toInt(),
                    Parity.valueOf(parts[3]),
                    parts[4],
                    parts[5].toInt(),
                )
            }
        }
    }
}
