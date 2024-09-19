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
    private lateinit var setCollectionIndex: EditText
    private lateinit var collectionIndexText: TextView
    private lateinit var setFlashcardIndex: EditText
    private lateinit var nativeLanguagePrompt: EditText
    private lateinit var foreignLanguagePrompt: EditText
    private lateinit var useDateCheckbox: CheckBox
    private lateinit var divider: View

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        saveSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.settings_back_button)
        setCollectionIndex = findViewById(R.id.collection_index)
        collectionIndexText = findViewById(R.id.collection_index_helper_text)
        setFlashcardIndex = findViewById(R.id.flashcard_index)
        nativeLanguagePrompt = findViewById(R.id.enter_native_language_name)
        foreignLanguagePrompt = findViewById(R.id.enter_foreign_language_name)
        useDateCheckbox = findViewById(R.id.use_date_as_collection_index)
        divider = findViewById(R.id.collection_index_underscore)

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)

        val nativeLanguage = sharedPref.getString("native_language", "German").toString()
        val foreignLanguage = sharedPref.getString("foreign_language", "Spanish").toString()
        val flashcardIndex = (sharedPref.getInt("flashcard_index", 0)).toString()
        val collectionIndexName = (sharedPref.getString("index_of_next_collection_to_be_created", "Collection_0"))
        val useDateAsCollectionIndex = sharedPref.getBoolean("use_date_as_collection_index", false)

        val collectionIndexNumber = collectionIndexName?.removePrefix("Collection_")

        if (useDateAsCollectionIndex) {
            disableCollectionIndexEditing()
        } else {
            enableCollectionIndexEditing()
        }

        nativeLanguagePrompt.setText(nativeLanguage)
        foreignLanguagePrompt.setText(foreignLanguage)
        setCollectionIndex.setText(collectionIndexNumber)
        setFlashcardIndex.setText(flashcardIndex)

        useDateCheckbox.isChecked = useDateAsCollectionIndex

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


            val editor = sharedPref.edit()
            editor.putBoolean("use_date_as_collection_index", useDateCheckbox.isChecked)
            editor.apply()
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
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        if (nativeLanguagePrompt.text.isNullOrEmpty() || foreignLanguagePrompt.text.isNullOrEmpty()) {
            MyUtils.createShortToast(this, "native or foreign language name cannot be empty")
        }else if (setCollectionIndex.text.isNullOrEmpty() || setFlashcardIndex.text.isNullOrEmpty()){
            MyUtils.createShortToast(this, "collection or flashcard index cannot be empty")
        } else {
            editor.putString("native_language", nativeLanguagePrompt.text.toString())
            editor.putString("foreign_language", foreignLanguagePrompt.text.toString())
            editor.putString("index_of_next_collection_to_be_created", "Collection_" + setCollectionIndex.text.toString())
            editor.putInt("flashcard_index", setFlashcardIndex.text.toString().toInt())
            editor.apply()

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}