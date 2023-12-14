package com.example.loginsignupsql

data class User(
    val id: String,
    val username: String,
    val encryptedSaltCiphertext: ByteArray,
    val encryptedSaltIV: ByteArray,
    val encryptedHashedPasswordCiphertext: ByteArray,
    val encryptedHashedPasswordIV: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return id == other.id &&
                username == other.username &&
                encryptedSaltCiphertext.contentEquals(other.encryptedSaltCiphertext) &&
                encryptedSaltIV.contentEquals(other.encryptedSaltIV) &&
                encryptedHashedPasswordCiphertext.contentEquals(other.encryptedHashedPasswordCiphertext) &&
                encryptedHashedPasswordIV.contentEquals(other.encryptedHashedPasswordIV)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + encryptedSaltCiphertext.contentHashCode()
        result = 31 * result + encryptedSaltIV.contentHashCode()
        result = 31 * result + encryptedHashedPasswordCiphertext.contentHashCode()
        result = 31 * result + encryptedHashedPasswordIV.contentHashCode()
        return result
    }
}
