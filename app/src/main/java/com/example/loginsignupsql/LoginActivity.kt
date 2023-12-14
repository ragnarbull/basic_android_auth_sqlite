package com.example.loginsignupsql

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey

import com.example.loginsignupsql.databinding.ActivityLoginBinding
import com.example.loginsignupsql.utils.Crypto
import javax.crypto.spec.IvParameterSpec

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.loginButton.setOnClickListener {
            val username = binding.loginUsername.text.toString()
            val password = binding.loginPassword.text.toString().toByteArray()
            loginDatabase(username, password)
        }
        binding.signupRedirect.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginDatabase(username: String, password: ByteArray) {
        val user = databaseHelper.readUser(username)

        if (user != null) {
            // Retrieve the encrypted salt and hashed password from the SQLite db
            val storedEncryptedSaltCiphertext = user.encryptedSaltCiphertext
            val storedEncryptedSaltIV = user.encryptedSaltIV
            val storedEncryptedHashedPasswordCiphertext = user.encryptedHashedPasswordCiphertext
            val storedEncryptedHashedPasswordIV = user.encryptedHashedPasswordIV

            // Decrypt using the keys from the Android KeyStore
            val decryptedSalt = decryptDataWithKeyStoreKeys(storedEncryptedSaltCiphertext, storedEncryptedSaltIV, "saltKey")
            val decryptedHashedPassword = decryptDataWithKeyStoreKeys(storedEncryptedHashedPasswordCiphertext, storedEncryptedHashedPasswordIV, "passwordKey")

            // Hash the password using Argon 2id and the decrypted salt
            val hashedPassword = Crypto.hashPassword(password, decryptedSalt)

            // Compare the (decrypted) stored hashed password with the hashed entered password
            if (hashedPassword.contentEquals(decryptedHashedPassword)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to decrypt data in the Keystore
    private fun decryptDataWithKeyStoreKeys(ciphertext: ByteArray, iv: ByteArray, alias: String): ByteArray {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val key = keyStore.getKey(alias, null) as SecretKey

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val ivParameterSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)

            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw RuntimeException("Error decrypting data", e)
        }
    }

}