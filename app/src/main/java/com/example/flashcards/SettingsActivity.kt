package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: FloatingActionButton
    private lateinit var tabNameEditText: EditText
    private lateinit var setCollectionIndex: EditText
    private lateinit var collectionIndexText: TextView
    private lateinit var setFlashcardIndex: EditText
    private lateinit var nativeLanguagePrompt: EditText
    private lateinit var foreignLanguagePrompt: EditText
    private lateinit var orderAscendinglyCheckbox: CheckBox
    private lateinit var useDateCheckbox: CheckBox
    private lateinit var divider: View
    private lateinit var tabSettingsPath: String

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
        setCollectionIndex = findViewById(R.id.collection_index)
        collectionIndexText = findViewById(R.id.collection_index_helper_text)
        setFlashcardIndex = findViewById(R.id.flashcard_index)
        nativeLanguagePrompt = findViewById(R.id.enter_native_language_name)
        foreignLanguagePrompt = findViewById(R.id.enter_foreign_language_name)
        orderAscendinglyCheckbox = findViewById(R.id.order_collections_asc)
        useDateCheckbox = findViewById(R.id.use_date_as_collection_index)
        divider = findViewById(R.id.collection_index_underscore)

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val currentTabIndex = sharedPref.getString("currentTabIndex", "ERROR")
        val tabPath = getExternalFilesDir(null).toString() + "/" + currentTabIndex
        tabSettingsPath = "$tabPath/Settings.txt"

        val tabName = MyUtils.readLineFromFile(tabSettingsPath, 0)
        val nativeLanguage = MyUtils.readLineFromFile(tabSettingsPath, 1)
        val foreignLanguage = MyUtils.readLineFromFile(tabSettingsPath, 2)
        val useDateAsCollectionIndexString = MyUtils.readLineFromFile(tabSettingsPath, 3)
        var useDateAsCollectionIndex = false
        if (useDateAsCollectionIndexString == "true") {
            useDateAsCollectionIndex = true
        }
        val orderAscString = MyUtils.readLineFromFile(tabSettingsPath, 6)
        var orderAsc = false
        if (orderAscString == "true") {
            orderAsc = true
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
        orderAscendinglyCheckbox.isChecked = orderAsc

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

        orderAscendinglyCheckbox.setOnClickListener {
            MyUtils.writeTextFile(tabSettingsPath, 6, orderAscendinglyCheckbox.isChecked.toString())
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

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}