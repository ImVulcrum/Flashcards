package com.example.flashcards

import android.annotation.SuppressLint
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
    private lateinit var archiveButton: Button

    private lateinit var nameOfCurrentCollection:String

    lateinit var collectionPath: String

    private var queuedMode = false
    private var scheduledCollectionsString = ""

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToTraining()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_settings)

        backButton = findViewById(R.id.settings_back_button)
        nativeLanguageName = findViewById(R.id.enter_native_language_name)
        foreignLanguageName = findViewById(R.id.enter_foreign_language_name)
        collectionName = findViewById(R.id.enter_collection_name)
        archiveButton = findViewById(R.id.archive_collection)

        val b = intent.extras
        val v: String? = b?.getString("collectionPath")
        collectionPath = v ?: ""
        val i: String? = b?.getString("collectionId")

        val f:Boolean? = b?.getBoolean("queuedMode")
        queuedMode = f ?:false
        val c:String? = b?.getString("scheduledCollections")
        scheduledCollectionsString = c ?:""

        nameOfCurrentCollection = i ?:""

        collectionName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0))
        nativeLanguageName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 1))
        foreignLanguageName.setText(MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 2))

        if (MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 5) == "true") {
            archiveButton.text = "unarchive collection"
        } else {
            archiveButton.text = "archive collection"
        }

        //archive button
        archiveButton.setOnClickListener {
            if (MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 5) == "true") {
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 5, "false")
                archiveButton.text = "archive collection"
            } else {
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 5, "true")
                archiveButton.text = "unarchive collection"
            }
        }

        //back button
        backButton.setOnClickListener {
            backToTraining()
        }
    }

    private fun backToTraining() {
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
            b.putString("collectionId", nameOfCurrentCollection)
            b.putBoolean("queuedMode", queuedMode)
            b.putString("scheduledCollections", scheduledCollectionsString)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }
    }
}