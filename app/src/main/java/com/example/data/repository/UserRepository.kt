package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_USERNAME = "last_username"
    }

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun authenticate(credential: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanCred = credential.trim()
        val cleanPass = password.trim()

        if (cleanCred.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your Username or Email / برائے مہربانی یوزر نیم درج کریں"))
        }
        if (cleanPass.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your Password / پاس ورڈ درج کریں"))
        }

        val user = userDao.authenticate(cleanCred, cleanPass)
        if (user != null) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Invalid username/email or password / غلط یوزر نیم یا پاس ورڈ"))
        }
    }

    suspend fun registerUser(
        username: String,
        fullName: String,
        email: String,
        password: String,
        role: String,
        phone: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val cleanName = fullName.trim()
        val cleanMail = email.trim()
        val cleanPass = password.trim()
        val cleanRole = role.trim().ifBlank { "Yard Master" }
        val cleanPhone = phone.trim()

        if (cleanUser.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters / یوزر نیم کم از کم 3 حروف پر مشتمل ہو"))
        }
        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your Full Name / پورا نام درج کریں"))
        }
        if (cleanPass.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 4 characters / پاس ورڈ کم از کم 4 حروف کا ہو"))
        }

        // Check if username already exists
        val existingUser = userDao.getUserByUsername(cleanUser)
        if (existingUser != null) {
            return@withContext Result.failure(IllegalArgumentException("Username '$cleanUser' is already taken / یہ یوزر نیم پہلے سے موجود ہے"))
        }

        if (cleanMail.isNotBlank()) {
            val existingEmail = userDao.getUserByEmail(cleanMail)
            if (existingEmail != null) {
                return@withContext Result.failure(IllegalArgumentException("Email is already registered / یہ ای میل پہلے سے رجسٹرڈ ہے"))
            }
        }

        val newUser = UserEntity(
            username = cleanUser,
            fullName = cleanName,
            email = cleanMail,
            password = cleanPass,
            role = cleanRole,
            phone = cleanPhone,
            createdAt = System.currentTimeMillis()
        )

        val insertedId = userDao.insertUser(newUser)
        val created = newUser.copy(id = insertedId)
        Result.success(created)
    }

    suspend fun getUserById(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    suspend fun seedDefaultUsersIfEmpty() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            val defaultUsers = listOf(
                UserEntity(
                    username = "admin",
                    fullName = "Terminal Admin Officer",
                    email = "admin@terminal.pk",
                    password = "password123",
                    role = "Admin",
                    phone = "0300-1234567"
                ),
                UserEntity(
                    username = "officer",
                    fullName = "Majid Ali (Gate Officer)",
                    email = "gate@terminal.pk",
                    password = "pass123",
                    role = "Gate Incharge",
                    phone = "0321-9876543"
                ),
                UserEntity(
                    username = "yardmaster",
                    fullName = "Muhammad Tariq (Yard Master)",
                    email = "yard@terminal.pk",
                    password = "pass123",
                    role = "Yard Master",
                    phone = "0333-7778899"
                )
            )

            for (user in defaultUsers) {
                userDao.insertUser(user)
            }
        }
    }

    fun saveSession(userId: Long, rememberMe: Boolean, username: String) {
        prefs.edit()
            .putLong(KEY_LOGGED_IN_USER_ID, userId)
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_LAST_USERNAME, username)
            .apply()
    }

    fun clearSession() {
        val remember = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val lastUser = prefs.getString(KEY_LAST_USERNAME, "")
        prefs.edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .putBoolean(KEY_REMEMBER_ME, remember)
            .putString(KEY_LAST_USERNAME, lastUser)
            .apply()
    }

    fun getSavedUserId(): Long? {
        val id = prefs.getLong(KEY_LOGGED_IN_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun getLastSavedUsername(): String {
        return prefs.getString(KEY_LAST_USERNAME, "") ?: ""
    }

    fun isRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }
}
