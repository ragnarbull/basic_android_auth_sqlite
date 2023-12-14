package com.example.loginsignupsql

data class User(
    val id: String,
    val username: String,
    val salt: ByteArray,
    val password: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return id == other.id &&
                username == other.username &&
                salt.contentEquals(other.salt) &&
                password.contentEquals(other.password)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + password.contentHashCode()
        return result
    }
}
