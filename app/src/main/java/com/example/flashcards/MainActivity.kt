package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flashcards.databinding.ActivityMainBinding
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var buttonAdd: Button
    private lateinit var container: ViewGroup
    private lateinit var showArchivedCollections: CheckBox
    private var collectionDisplayWidth = 320
    private var collectionDisplayHeight = 40
    lateinit var appPath: String
    lateinit var collectionPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsButton = findViewById(R.id.b_settings)
        container = findViewById(R.id.container)
        buttonAdd = findViewById(R.id.button_add)
        showArchivedCollections = findViewById(R.id.show_archived_collections)

        appPath = getExternalFilesDir(null).toString()
        collectionPath = appPath + "/Collections"

        //scan for collections
        val collections = MyUtils.getFoldersInDirectory(collectionPath)

        for (collection in collections) {
            if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                addCollectionButtons(collection.removePrefix("Collection_").toInt(), false)
            }
        }

        buttonAdd.setOnClickListener {
            addCollection()
        }

        settingsButton.setOnClickListener {
            intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        showArchivedCollections.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (collection in collections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "true") {
                        addCollectionButtons(collection.removePrefix("Collection_").toInt(), false, R.color.button_color)
                    }
                }
            }   else {
                container.removeAllViews()
                for (collection in collections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                        addCollectionButtons(collection.removePrefix("Collection_").toInt(), false)
                    }
                }

            }
        }
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun addCollection() {
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var collectionCount = sharedPref.getInt("collection_count", 0)
        val nativeLanguage = sharedPref.getString("native_language", "German").toString()
        val foreignLanguage = sharedPref.getString("foreign_language", "Spanish").toString()

        collectionCount++
        val folderName = "Collection_$collectionCount"
        val folderPath = collectionPath + "/" + folderName
        val fileName = "Properties.txt"

        if (MyUtils.createFolder(this, collectionPath, folderName, "Collection created successfully")) { //only do something if the folder does not exist
            MyUtils.createTextFile(folderPath, fileName)
            MyUtils.writeTextFile(folderPath + "/" + fileName, 0, "Collection_$collectionCount")
            MyUtils.writeTextFile(folderPath + "/" + fileName, 1, nativeLanguage)
            MyUtils.writeTextFile(folderPath + "/" + fileName, 2, foreignLanguage)
            MyUtils.writeTextFile(folderPath + "/" + fileName, 3, "-")
            MyUtils.writeTextFile(folderPath + "/" + fileName, 5, "false")

            addCollectionButtons(collectionCount, true)
        }

        editor.putInt(folderPath, 0)

        editor.putInt("collection_count", collectionCount)
        editor.apply()
    }

    private fun addCollectionButtons(collectionId:Int, isNewCreated:Boolean, collectionButtonColor:Int = R.color.primary) {

        // Create a horizontal LinearLayout to hold the two buttons
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0)
            }
        }

        // Create the left button (wider)
        val collectionButton = Button(this).apply {

            if (isNewCreated) {
                text = "Collection_$collectionId"
            }   else {
                text = MyUtils.readLineFromFile(collectionPath + "/Collection_$collectionId/Properties.txt", 0)
            }

            isAllCaps = false

            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                (collectionDisplayWidth - collectionDisplayHeight).dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(20, 0, 0, 4)
            }
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, collectionButtonColor))
            setOnClickListener {
                intent = Intent(this@MainActivity, TrainingActivity::class.java)
                val b = Bundle()
                b.putInt("collectionId", collectionId)
                intent.putExtras(b)
                startActivity(intent)
                finish()
            }
        }

        // Create the right button (square)
        val deleteCollectionButton = Button(this).apply {
            contentDescription = "Collection_$collectionId"
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                collectionDisplayHeight.dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(0, 0, 20, 4)
            }
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.delete_highlight))
            setOnClickListener { button ->
                MyUtils.showConfirmationDialog(this@MainActivity,"Delete Collection", "Are you sure you want to delete this collection?") {userChoice ->
                    if (userChoice) {
                        container.removeView(rowLayout)
                        MyUtils.deleteFolder(this@MainActivity, collectionPath + "/" + button.contentDescription.toString(), "Collection deleted successfully")
                    }
                }
            }
        }

        // Add buttons to the horizontal layout
        rowLayout.addView(collectionButton)
        rowLayout.addView(deleteCollectionButton)

        // Add the horizontal layout to the container
        container.addView(rowLayout)
    }
}