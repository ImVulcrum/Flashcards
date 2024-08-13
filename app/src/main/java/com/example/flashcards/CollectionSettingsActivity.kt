package com.example.flashcards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CollectionSettingsActivity : AppCompatActivity() {

    private lateinit var backButton: FloatingActionButton
    private lateinit var nativeLanguageName: EditText
    private lateinit var foreignLanguageName: EditText
    private lateinit var collectionName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_settings)

        backButton = findViewById(R.id.settings_back_button)
        nativeLanguageName = findViewById(R.id.enter_native_language_name)
        foreignLanguageName = findViewById(R.id.enter_foreign_language_name)
        collectionName = findViewById(R.id.enter_collection_name)

        val b = intent.extras
        val v: String? = b?.getString("collectionPath")
        val collectionPath: String = v ?: ""
        val i: Int? = b?.getInt("collectionId")
        val collectionNumber: Int = i ?: 0

        collectionName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0))
        nativeLanguageName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 1))
        foreignLanguageName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 2))

        //back button
        backButton.setOnClickListener {
            if (nativeLanguageName.text.isNullOrEmpty() || foreignLanguageName.text.isNullOrEmpty()) {
                MyUtils.createShortToast(this, "native or foreign language name cannot be empty")
            } else if (collectionName.text.isNullOrEmpty()) {
                MyUtils.createShortToast(this, "collection name cannot be empty")
            } else {
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 0, collectionName.text.toString())
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 1, nativeLanguageName.text.toString())
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 2, foreignLanguageName.text.toString())

                intent = Intent(this, TrainingActivity::class.java)
                val b = Bundle()
                b.putInt("collectionId", collectionNumber)
                intent.putExtras(b)
                startActivity(intent)
                finish()
            }

        }
    }
}