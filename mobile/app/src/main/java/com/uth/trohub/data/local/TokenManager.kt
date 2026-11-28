package com.uth.trohub.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "trohub_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_BASE_URL = "base_url"
    const val DEFAULT_BASE_URL = "http://192.168.2.167:8080/api/v1/"
    private const val KEY_ACTIVE_PROPERTY_ID = "active_property_id"
    private const val KEY_ACTIVE_PROPERTY_NAME = "active_property_name"
    private const val KEY_BANK_NAME = "bank_name"
    private const val KEY_BANK_ACCOUNT = "bank_account"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? {
        return prefs?.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun getBaseUrl(): String {
        return prefs?.getString(KEY_BASE_URL, null) ?: DEFAULT_BASE_URL
    }

    fun saveBaseUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        prefs?.edit()?.putString(KEY_BASE_URL, cleanUrl)?.apply()
    }

    fun resetBaseUrl() {
        prefs?.edit()?.remove(KEY_BASE_URL)?.apply()
    }

    fun saveActiveProperty(id: String, name: String) {
        prefs?.edit()
            ?.putString(KEY_ACTIVE_PROPERTY_ID, id)
            ?.putString(KEY_ACTIVE_PROPERTY_NAME, name)
            ?.apply()
    }

    fun getActivePropertyId(): String? {
        return prefs?.getString(KEY_ACTIVE_PROPERTY_ID, null)
    }

    fun getActivePropertyName(): String? {
        return prefs?.getString(KEY_ACTIVE_PROPERTY_NAME, null)
    }

    fun saveBankInfo(bankName: String, accountNumber: String) {
        prefs?.edit()
            ?.putString(KEY_BANK_NAME, bankName)
            ?.putString(KEY_BANK_ACCOUNT, accountNumber)
            ?.apply()
    }

    fun getBankName(): String? {
        return prefs?.getString(KEY_BANK_NAME, null)
    }

    fun getBankAccount(): String? {
        return prefs?.getString(KEY_BANK_ACCOUNT, null)
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs?.getBoolean(KEY_ONBOARDING_COMPLETED, false) ?: false
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs?.edit()?.putBoolean(KEY_ONBOARDING_COMPLETED, completed)?.apply()
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }
}
