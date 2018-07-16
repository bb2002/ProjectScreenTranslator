package kr.saintdev.pst.models.libs.manager

import android.content.Context
import android.content.SharedPreferences
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-07
 */
class EnvSettingManager(context: Context) {
    private val readOnly: SharedPreferences = context.defaultSharedPreferences

    companion object {
        private var instance: EnvSettingManager? = null

        fun getInstance(context: Context): EnvSettingManager {
            if(instance == null) {
                instance = EnvSettingManager(context)
            }

            return instance!!
        }

        fun getQuickly(context: Context, key: EnvSettingKeys) : String {
            val envSetting = EnvSettingManager(context)
            return envSetting.read(key)
        }

        fun getQuicklyForBoolean(context: Context, key: EnvSettingKeys) : Boolean {
            val envSetting = EnvSettingManager(context)
            return envSetting.readBoolean(key)
        }
    }

    fun read(key: EnvSettingKeys) = readOnly.getString(key.getKey(), key.defValue)

    fun readBoolean(key: EnvSettingKeys) = readOnly.getBoolean(key.getKey(), key.defValue.toBoolean())

    fun forceWrite(key: String, data: String) {
        val writeOnly = readOnly.edit()
        writeOnly.putString(key, data)
        writeOnly.apply()
    }
}

enum class EnvSettingKeys(val keyVal: String, val defValue: String) {
    RUN_EVENT("settings_runtime_event", "device_shake"),
    GAMING_TEXT_COLOR("settings_gaming_textcolor", "ecf0f1"),
    GAMING_BG_COLOR("settings_gaming_bgcolor", "1f2b38"),
    USE_VIBRATION("settings_runtime_vib", "true"),
    SHAKE_SENS("settings_shake_sens", "2"),
    OVERLAY_BUTTON_BG_COLOR("settings_overlaybtn_bgcolor", "1f2b38"),
    OVERLAY_BUTTON_SIZE("settings_overlaybtn_size", "2");

    fun getKey() = keyVal
    fun getDefaultVal() = defValue
}