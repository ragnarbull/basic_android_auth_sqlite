package com.example.loginsignupsql

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import java.util.UUID
import java.security.SecureRandom
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

import com.example.loginsignupsql.databinding.ActivitySignupBinding
import com.example.loginsignupsql.utils.Crypto

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.signupButton.setOnClickListener {
            val signupId = UUID.randomUUID().toString()
            val signupUsername = binding.signupUsername.text.toString()
            val password = binding.signupPassword.text.toString().toByteArray()

            // Generate a random salt (16 bytes)
            val salt = generateRandomSalt()

            // Hash the password using Argon 2id
            val hashedPassword = Crypto.hashPassword(password, salt)

            // Store the salt and hashed password in the Keystore
            val saltAlias = "saltKey"
            val passwordAlias = "passwordKey"

            val encryptedSaltPair = encryptDataInKeystore(saltAlias, salt)
            val encryptedSaltCiphertext = encryptedSaltPair.first
            val encryptedSaltIV = encryptedSaltPair.second

            val encryptedHashedPasswordPair = encryptDataInKeystore(passwordAlias, hashedPassword)
            val encryptedHashedPasswordCiphertext = encryptedHashedPasswordPair.first
            val encryptedHashedPasswordIV = encryptedHashedPasswordPair.second

            signupDatabase(signupId, signupUsername, encryptedSaltCiphertext, encryptedSaltIV, encryptedHashedPasswordCiphertext, encryptedHashedPasswordIV)
        }

        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signupDatabase(id: String, username: String, encryptedSaltCiphertext: ByteArray, encryptedSaltIV: ByteArray, encryptedHashedPasswordCiphertext: ByteArray, encryptedHashedPasswordIV: ByteArray) {
        val insertRowId = databaseHelper.insertUser(id, username, encryptedSaltCiphertext, encryptedSaltIV, encryptedHashedPasswordCiphertext, encryptedHashedPasswordIV)
        if (insertRowId != -1L) {
            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to generate a random salt
    private fun generateRandomSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    // Helper function to encrypt data in the Keystore
    private fun encryptDataInKeystore(alias: String, data: ByteArray): Pair<ByteArray, ByteArray> {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keySpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                setUserAuthenticationRequired(false)
            }.build()

            keyGenerator.init(keySpec)
            val secretKey = keyGenerator.generateKey()

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedData = cipher.doFinal(data)
            val iv = cipher.iv

            return Pair(encryptedData, iv)
        }  catch (e: Exception) {
            throw RuntimeException("Error encrypting data", e)
        }
    }
}

