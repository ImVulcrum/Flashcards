package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flashcards.utils.MyUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: FloatingActionButton
    private lateinit var tabNameEditText: EditText
    private lateinit var tabIndex: TextView
    private lateinit var setCollectionIndex: EditText
    private lateinit var collectionIndexText: TextView
    private lateinit var setFlashcardIndex: EditText
    private lateinit var nativeLanguagePrompt: EditText
    private lateinit var foreignLanguagePrompt: EditText
    private lateinit var useSmallCheckbox: CheckBox
    private lateinit var useDateCheckbox: CheckBox
    private lateinit var divider: View
    private lateinit var tabSettingsPath: String
    private lateinit var order_ascendingly: MaterialButton
    private lateinit var order_chronological: MaterialButton
    private var orderAsc: Boolean = false

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        saveSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.settings_back_button)
        tabNameEditText = findViewById(R.id.tab_name)
        tabIndex = findViewById(R.id.tab_index)
        setCollectionIndex = findViewById(R.id.collection_index)
        collectionIndexText = findViewById(R.id.collection_index_helper_text)
        setFlashcardIndex = findViewById(R.id.flashcard_index)
        nativeLanguagePrompt = findViewById(R.id.enter_native_language_name)
        foreignLanguagePrompt = findViewById(R.id.enter_foreign_language_name)

        order_ascendingly = findViewById((R.id.order_ascendingly))
        order_chronological = findViewById((R.id.order_chronological))

        useSmallCheckbox = findViewById(R.id.use_small_font_size)
        useDateCheckbox = findViewById(R.id.use_date_as_collection_index)
        divider = findViewById(R.id.collection_index_underscore)

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val currentTabIndex = sharedPref.getString("currentTabIndex", "ERROR")
        val tabPath = getExternalFilesDir(null).toString() + "/" + currentTabIndex
        tabSettingsPath = "$tabPath/Settings.txt"

        tabIndex.text = currentTabIndex

        val tabName = MyUtils.readLineFromFile(tabSettingsPath, 0)
        val nativeLanguage = MyUtils.readLineFromFile(tabSettingsPath, 1)
        val foreignLanguage = MyUtils.readLineFromFile(tabSettingsPath, 2)
        val useDateAsCollectionIndexString = MyUtils.readLineFromFile(tabSettingsPath, 3)

        var useDateAsCollectionIndex = false
        if (useDateAsCollectionIndexString == "true") {
            useDateAsCollectionIndex = true
        }
        val orderAscString = MyUtils.readLineFromFile(tabSettingsPath, 6)
        if (orderAscString == "true") {
            orderAsc = true
        }

        val useSmallFlashcardFontSize = MyUtils.readLineFromFile(tabSettingsPath, 7)
        useSmallCheckbox.isChecked = false
        if (useSmallFlashcardFontSize == "true") {
            useSmallCheckbox.isChecked = true
        }

        val collectionIndexName = MyUtils.readLineFromFile(tabSettingsPath, 4)
        val flashcardIndex = MyUtils.readLineFromFile(tabSettingsPath, 5)

        val collectionIndexNumber = collectionIndexName?.removePrefix("Collection_")

        if (useDateAsCollectionIndex) {
            disableCollectionIndexEditing()
        } else {
            enableCollectionIndexEditing()
        }

        tabNameEditText.setText(tabName)
        nativeLanguagePrompt.setText(nativeLanguage)
        foreignLanguagePrompt.setText(foreignLanguage)
        setCollectionIndex.setText(collectionIndexNumber)
        setFlashcardIndex.setText(flashcardIndex)

        useDateCheckbox.isChecked = useDateAsCollectionIndex

        if (orderAsc) {
            order_ascendingly.setStrokeWidth(12)
            order_chronological.setStrokeWidth(4)
            order_ascendingly.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight)))
            order_chronological.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
        }   else {
            order_chronological.setStrokeWidth(12)
            order_ascendingly.setStrokeWidth(4)
            order_chronological.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight)))
            order_ascendingly.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
        }

        //back button
        backButton.setOnClickListener {
            saveSettings()
        }

        useDateCheckbox.setOnClickListener {
            if (useDateCheckbox.isChecked) {
                disableCollectionIndexEditing()
            } else {
                enableCollectionIndexEditing()
            }

            MyUtils.writeTextFile(tabSettingsPath, 3, useDateCheckbox.isChecked.toString())
        }

        order_ascendingly.setOnClickListener {
            order_ascendingly.setStrokeWidth(12)
            order_chronological.setStrokeWidth(4)
            order_ascendingly.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight)))
            order_chronological.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
            orderAsc = true
        }

        order_chronological.setOnClickListener {
            order_chronological.setStrokeWidth(12)
            order_ascendingly.setStrokeWidth(4)
            order_chronological.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight)))
            order_ascendingly.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
            orderAsc = false
        }
    }

    private fun enableCollectionIndexEditing() {
        setCollectionIndex.isEnabled = true
        setCollectionIndex.setTextColor(getColor(R.color.white))
        collectionIndexText.setTextColor(getColor(R.color.white))
        divider.backgroundTintList = getColorStateList(R.color.white)
    }

    private fun disableCollectionIndexEditing() {
        setCollectionIndex.isEnabled = false
        setCollectionIndex.setTextColor(getColor(R.color.button_color))
        collectionIndexText.setTextColor(getColor(R.color.button_color))
        divider.backgroundTintList = getColorStateList(R.color.button_color)
    }

    private fun saveSettings() {
        if (nativeLanguagePrompt.text.isNullOrEmpty() || foreignLanguagePrompt.text.isNullOrEmpty()) {
            MyUtils.createShortToast(this, "native or foreign language name cannot be empty")
        }else if (setCollectionIndex.text.isNullOrEmpty() || setFlashcardIndex.text.isNullOrEmpty()){
            MyUtils.createShortToast(this, "collection or flashcard index cannot be empty")
        } else {
            MyUtils.writeTextFile(tabSettingsPath, 0, tabNameEditText.text.toString())
            MyUtils.writeTextFile(tabSettingsPath, 1, nativeLanguagePrompt.text.toString())
            MyUtils.writeTextFile(tabSettingsPath, 2, foreignLanguagePrompt.text.toString())
            MyUtils.writeTextFile(tabSettingsPath, 4,"Collection_" + setCollectionIndex.text.toString())
            MyUtils.writeTextFile(tabSettingsPath, 5, setFlashcardIndex.text.toString())
            MyUtils.writeTextFile(tabSettingsPath, 6, orderAsc.toString())
            MyUtils.writeTextFile(tabSettingsPath, 7, useSmallCheckbox.isChecked.toString())

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}