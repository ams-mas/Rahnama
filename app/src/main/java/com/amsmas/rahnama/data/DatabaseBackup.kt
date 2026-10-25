package com.amsmas.rahnama.data

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** Copies the complete local Room database, including all tables and IDs. */
object DatabaseBackup {
    suspend fun export(context: Context, destination: Uri) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val database = RahnamaDatabase.getInstance(appContext)
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { }
        RahnamaDatabase.closeInstanceForFileOperation()
        try {
            val source = appContext.getDatabasePath("rahnama.db")
            require(source.isFile && source.length() > 0L) { "دیتابیس پیدا نشد." }
            appContext.contentResolver.openOutputStream(destination, "wt").use { output ->
                requireNotNull(output) { "فایل پشتیبان باز نشد." }
                FileInputStream(source).use { input -> input.copyTo(output) }
                output.flush()
            }
        } finally {
            RahnamaDatabase.getInstance(appContext)
        }
    }

    suspend fun restore(context: Context, sourceUri: Uri) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val stamp = System.currentTimeMillis()
        val temp = File(appContext.cacheDir, "rahnama-restore-" + stamp + ".db")
        val live = appContext.getDatabasePath("rahnama.db")
        val rollback = File(appContext.cacheDir, "rahnama-before-restore-" + stamp + ".db")
        try {
            appContext.contentResolver.openInputStream(sourceUri).use { input ->
                requireNotNull(input) { "فایل انتخاب‌شده خوانده نشد." }
                FileOutputStream(temp).use { output -> input.copyTo(output) }
            }
            require(temp.length() > 0L) { "فایل پشتیبان خالی است." }
            validateDatabase(temp)

            RahnamaDatabase.closeInstanceForFileOperation()
            try {
                if (live.exists()) live.copyTo(rollback, overwrite = true)
                live.parentFile?.mkdirs()
                temp.copyTo(live, overwrite = true)
                deleteSidecarFiles(live)
            } catch (error: Exception) {
                if (rollback.exists()) rollback.copyTo(live, overwrite = true)
                throw error
            } finally {
                RahnamaDatabase.getInstance(appContext)
            }
        } finally {
            temp.delete()
            rollback.delete()
        }
    }

    private fun validateDatabase(file: File) {
        val database = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            database.rawQuery("PRAGMA quick_check", null).use { cursor ->
                require(cursor.moveToFirst() && cursor.getString(0) == "ok") {
                    "فایل پشتیبان معتبر نیست یا دیتابیس آسیب دیده است."
                }
            }
            val requiredTables = setOf("persons", "properties", "client_requests")
            val foundTables = mutableSetOf<String>()
            database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { cursor ->
                while (cursor.moveToNext()) foundTables += cursor.getString(0)
            }
            require(foundTables.containsAll(requiredTables)) {
                "این فایل، پشتیبان معتبر اپلیکیشن رهنمای معاملات نیست."
            }
        } finally {
            database.close()
        }
    }

    private fun deleteSidecarFiles(databaseFile: File) {
        File(databaseFile.absolutePath + "-wal").delete()
        File(databaseFile.absolutePath + "-shm").delete()
        File(databaseFile.absolutePath + "-journal").delete()
    }
}
