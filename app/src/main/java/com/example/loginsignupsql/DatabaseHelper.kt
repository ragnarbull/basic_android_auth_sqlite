package com.example.loginsignupsql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context):
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_SALT = "salt"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID TEXT PRIMARY KEY, " +  // UUID
//                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_SALT BLOB, " +
                "$COLUMN_PASSWORD BLOB)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertUser(id: String, username: String, salt: ByteArray, hashedPassword: ByteArray): Long {
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_USERNAME, username)
            put(COLUMN_SALT, salt)
            put(COLUMN_PASSWORD, hashedPassword)
        }
        val db = writableDatabase
        return db.insertOrThrow(TABLE_NAME, null, values)
    }

    fun readUser(username: String): User? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
        var user: User? = null

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID)
            val saltIndex = cursor.getColumnIndex(COLUMN_SALT)
            val passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD)

            val id = cursor.getString(idIndex)
            val salt = cursor.getBlob(saltIndex)
            val password = cursor.getBlob(passwordIndex)

            user = User(id, username, salt, password)
        }

        cursor.close()
        return user
    }
}