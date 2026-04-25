package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RepetitionSettingsActivity: AppCompatActivity() {
    private lateinit var backButton: FloatingActionButton
    private lateinit var collectionsConsideredField: EditText
    private lateinit var flashcardsConsideredField: EditText
    private lateinit var excludeArchivedCheckBox: CheckBox
    private lateinit var autoUpdateCheckBox: CheckBox
    private lateinit var tabName: TextView
    private var repetitionSettingsPath = ""

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        saveSettingsAndQuit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repetition_settings)

        backButton = findViewById(R.id.repetition_back_button)
        collectionsConsideredField = findViewById(R.id.collections_considered)
        flashcardsConsideredField = findViewById(R.id.flashcards_considered)
        excludeArchivedCheckBox = findViewById(R.id.exclude_archived_collections)
        autoUpdateCheckBox = findViewById(R.id.auto_update)
        tabName = findViewById(R.id.tab_name)

        val b = intent.extras
        val t: String? = b?.getString("tabPath")
        val tabPath = t ?: ""
        val p: String? = b?.getString("repetitionSettingsPath")
        repetitionSettingsPath = p ?: ""

        //get tab name
        val tabNameString = MyUtils.readLineFromFile(tabPath + "/Settings.txt", 0)
        tabName.text = "Tab: " + tabNameString

        collectionsConsideredField.setText(MyUtils.readLineFromFile(repetitionSettingsPath, 3))
        flashcardsConsideredField.setText(MyUtils.readLineFromFile(repetitionSettingsPath, 4))

        if (MyUtils.readLineFromFile(repetitionSettingsPath, 2) == "true") {
            excludeArchivedCheckBox.isChecked = true
        }   else {
            excludeArchivedCheckBox.isChecked = false
        }

        if (MyUtils.readLineFromFile(repetitionSettingsPath, 7) == "true") {
            autoUpdateCheckBox.isChecked = true
        }   else {
            autoUpdateCheckBox.isChecked = false
        }

        backButton.setOnClickListener {
            saveSettingsAndQuit()
        }
    }

    private fun saveSettingsAndQuit() {
        MyUtils.writeTextFile(repetitionSettingsPath, 0, MyUtils.getCurrentDate())
        MyUtils.writeTextFile(repetitionSettingsPath, 2, excludeArchivedCheckBox.isChecked.toString())
        MyUtils.writeTextFile(repetitionSettingsPath, 3, collectionsConsideredField.text.toString())
        MyUtils.writeTextFile(repetitionSettingsPath, 4, flashcardsConsideredField.text.toString())
        MyUtils.writeTextFile(repetitionSettingsPath, 7, autoUpdateCheckBox.isChecked.toString())

        intent = Intent(this, TrainingActivity::class.java)
        val b = Bundle()
        b.putString("collectionId", "-")
        b.putString("mode", "r")
        intent.putExtras(b)
        startActivity(intent)
        finish()
    }
}