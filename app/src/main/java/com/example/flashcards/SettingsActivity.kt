package com.example.flashcards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: FloatingActionButton
    private lateinit var setCollectionIndex: EditText
    private lateinit var nativeLanguagePrompt: EditText
    private lateinit var foreignLanguagePrompt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.settings_back_button)
        setCollectionIndex = findViewById(R.id.collection_index)
        nativeLanguagePrompt = findViewById(R.id.enter_native_language_name)
        foreignLanguagePrompt = findViewById(R.id.enter_foreign_language_name)

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val nativeLanguage = sharedPref.getString("native_language", "German").toString()
        val foreignLanguage = sharedPref.getString("foreign_language", "Spanish").toString()
        val collectionCount = (sharedPref.getInt("collection_count", 0)+1).toString()

        nativeLanguagePrompt.setText(nativeLanguage)
        foreignLanguagePrompt.setText(foreignLanguage)
        setCollectionIndex.setText(collectionCount)

        //back button
        backButton.setOnClickListener {
            if (nativeLanguagePrompt.text.isNullOrEmpty() || foreignLanguagePrompt.text.isNullOrEmpty()) {
                MyUtils.createShortToast(this, "native or foreign language name cannot be empty")
            }else if (setCollectionIndex.text.isNullOrEmpty()){
                MyUtils.createShortToast(this, "collection index cannot be empty")
            } else {
                editor.putString("native_language", nativeLanguagePrompt.text.toString())
                editor.putString("foreign_language", foreignLanguagePrompt.text.toString())
                editor.putInt("collection_count", setCollectionIndex.text.toString().toInt()-1)
                editor.apply()

                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}