package com.example.flashcards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: FloatingActionButton
    private lateinit var resetCollectionCountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.settings_back_button)
        resetCollectionCountButton = findViewById(R.id.button_reset_collection_count)

        //back button
        backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        resetCollectionCountButton.setOnClickListener {
            val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
            val editor = sharedPref.edit()

            editor.putInt("collection_count", 0)
            editor.apply()
        }
    }
}