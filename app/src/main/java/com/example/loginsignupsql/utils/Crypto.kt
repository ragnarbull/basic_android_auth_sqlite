package com.example.loginsignupsql.utils

import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version

class Crypto {
    companion object {
        fun hashPassword(password: ByteArray, salt: ByteArray): ByteArray {
            val argon2 = Argon2.Builder(Version.V13)
                .type(Type.Argon2id)
                .memoryCost(MemoryCost.MiB(32))
                .parallelism(1) // cores
                .iterations(10) // time cost
                .build()

//            val startTime = System.currentTimeMillis()

            val result = argon2.hash(password, salt)

//            val endTime = System.currentTimeMillis()
//
//            val hashTime = endTime - startTime
//            println("Hashing time: $hashTime ms") // keep to around 500 ms

            return result.hash
        }
    }
}