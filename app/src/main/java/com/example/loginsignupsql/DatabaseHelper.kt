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
        private const val COLUMN_ENCRYPTED_SALT_CIPHERTEXT = "encryptedSaltCiphertext"
        private const val COLUMN_ENCRYPTED_SALT_IV = "encryptedSaltIV"
        private const val COLUMN_ENCRYPTED_HASHED_PASSWORD_CIPHERTEXT = "encryptedHashedPasswordCiphertext"
        private const val COLUMN_ENCRYPTED_HASHED_PASSWORD_IV = "encryptedHashedPasswordIV"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID TEXT PRIMARY KEY, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_ENCRYPTED_SALT_CIPHERTEXT BLOB, " +
                "$COLUMN_ENCRYPTED_SALT_IV BLOB, " +
                "$COLUMN_ENCRYPTED_HASHED_PASSWORD_CIPHERTEXT BLOB, " +
                "$COLUMN_ENCRYPTED_HASHED_PASSWORD_IV BLOB)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertUser(id: String, username: String, encryptedSaltCiphertext: ByteArray, encryptedSaltIV: ByteArray, encryptedHashedPasswordCiphertext: ByteArray, encryptedHashedPasswordIV: ByteArray): Long {
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_USERNAME, username)
            put(COLUMN_ENCRYPTED_SALT_CIPHERTEXT, encryptedSaltCiphertext)
            put(COLUMN_ENCRYPTED_SALT_IV, encryptedSaltIV)
            put(COLUMN_ENCRYPTED_HASHED_PASSWORD_CIPHERTEXT, encryptedHashedPasswordCiphertext)
            put(COLUMN_ENCRYPTED_HASHED_PASSWORD_IV, encryptedHashedPasswordIV)
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
            val saltCiphertextIndex = cursor.getColumnIndex(COLUMN_ENCRYPTED_SALT_CIPHERTEXT)
            val saltIVIndex = cursor.getColumnIndex(COLUMN_ENCRYPTED_SALT_IV)
            val passwordCiphertextIndex = cursor.getColumnIndex(COLUMN_ENCRYPTED_HASHED_PASSWORD_CIPHERTEXT)
            val passwordIVIndex = cursor.getColumnIndex(COLUMN_ENCRYPTED_HASHED_PASSWORD_IV)

            val id = cursor.getString(idIndex)
            val encryptedSaltCiphertext = cursor.getBlob(saltCiphertextIndex)
            val encryptedSaltIV = cursor.getBlob(saltIVIndex)
            val encryptedHashedPasswordCiphertext = cursor.getBlob(passwordCiphertextIndex)
            val encryptedHashedPasswordIV = cursor.getBlob(passwordIVIndex)

            user = User(id, username, encryptedSaltCiphertext, encryptedSaltIV, encryptedHashedPasswordCiphertext, encryptedHashedPasswordIV)
        }

        cursor.close()
        return user
    }
}