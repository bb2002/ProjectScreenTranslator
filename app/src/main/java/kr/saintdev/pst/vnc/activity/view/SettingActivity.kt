package kr.saintdev.pst.vnc.activity.view

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toolbar
import kr.saintdev.pst.R

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-06
 */
class SettingActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var listPreferences: Array<Preference>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        this.listPreferences = arrayOf(
                findPreference("settings_runtime_event"),
                findPreference("settings_gaming_textcolor"),
                findPreference("settings_gaming_bgcolor"),
                findPreference("settings_shake_sens"),
                findPreference("settings_overlaybtn_bgcolor"),
                findPreference("settings_overlaybtn_size"))

        loadSummary()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun loadSummary() = listPreferences?.forEachIndexed { _, prep -> prep.summary = getListPrefEntryName(prep as ListPreference) }

    private fun getListPrefEntryName(prep: ListPreference) = prep.entry.toString()


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        loadSummary()
    }
}