package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcards.databinding.ActivityMainBinding
import com.example.flashcards.utils.MyUtils
import com.example.flashcards.utils.MyDisplayingUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var showArchivedCollections: CheckBox
    private lateinit var buttonPlayScheduled: FloatingActionButton
    private var collectionDisplayWidth = 320
    private var collectionDisplayHeight = 40
    lateinit var appPath: String
    lateinit var collectionPath: String
    lateinit var flashcardPath: String

    private lateinit var collectionAdapter: MyDisplayingUtils.CollectionAdapter
    private lateinit var collectionRecyclerView: RecyclerView
    private var scheduledCollections = mutableListOf<String>()
    private lateinit var listOfCollectionsInCorrectFormat: MutableList<MyDisplayingUtils.Collection>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsButton = findViewById(R.id.b_settings)
        buttonAdd = findViewById(R.id.button_add)
        showArchivedCollections = findViewById(R.id.show_archived_collections)
        buttonPlayScheduled = findViewById(R.id.b_play_scheduled)

        collectionRecyclerView = findViewById(R.id.collection_recycler_view)

        appPath = getExternalFilesDir(null).toString()
        collectionPath = appPath + "/Collections"
        flashcardPath = appPath + "/Flashcards"

        //reset any queues and deactivate the queue play button
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("scheduledCollectionIndex", 0)
        editor.apply()
        deactivateSchedulePlayButton()

        //scan for collections
        val collections = MyUtils.getFoldersInDirectory(collectionPath)
        val sortedCollections = MyUtils.sortCollectionStrings(collections)

        listOfCollectionsInCorrectFormat = mutableListOf<MyDisplayingUtils.Collection>()

        for (collectionId in sortedCollections) {
            if (MyUtils.readLineFromFile(collectionPath + "/" + collectionId + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collectionId + "/Properties.txt", 5) == "") {
                val collectionName = MyUtils.readLineFromFile(collectionPath + "/$collectionId/Properties.txt", 0)
                listOfCollectionsInCorrectFormat.add(MyDisplayingUtils.Collection(collectionId, collectionName!!, false, false))
            }
        }

        collectionAdapter = MyDisplayingUtils.CollectionAdapter(
            listOfCollectionsInCorrectFormat,
            { collection, position -> onCollectionSelected(collection, position) },
            { collection, position -> onCollectionClicked(collection, position) },
            { collection, position -> onCollectionDeleted(collection, position) }
        )

        collectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = collectionAdapter
        }

        buttonAdd.setOnClickListener {
            addCollection()
        }

        buttonPlayScheduled.setOnClickListener {
            intent = Intent(this@MainActivity, TrainingActivity::class.java)
            val b = Bundle()
            b.putString("collectionId", "-")
            b.putBoolean("queuedMode", true)
            b.putString("scheduledCollections", scheduledCollections.joinToString(" "))
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        settingsButton.setOnClickListener {
            intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        showArchivedCollections.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (collectionId in sortedCollections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collectionId + "/Properties.txt", 5) == "true") {
                        val collectionName = MyUtils.readLineFromFile(collectionPath + "/$collectionId/Properties.txt", 0)
                        listOfCollectionsInCorrectFormat.add(MyDisplayingUtils.Collection(collectionId, collectionName!!, false, true))

                        collectionRecyclerView.apply {
                            layoutManager = LinearLayoutManager(this@MainActivity)
                            adapter = collectionAdapter
                        }
                    }
                }
            } else {
                val archivedCollections = mutableListOf<MyDisplayingUtils.Collection>()
                for (collection in listOfCollectionsInCorrectFormat) {
                    if (collection.isArchived) {
                        archivedCollections.add(collection)
                    }
                }
                listOfCollectionsInCorrectFormat.removeAll(archivedCollections)
                collectionRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = collectionAdapter
                }
            }
        }
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun deactivateSchedulePlayButton() {
        buttonPlayScheduled.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        buttonPlayScheduled.isEnabled = false
    }

    private fun activateSchedulePlayButton() {
        buttonPlayScheduled.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        buttonPlayScheduled.isEnabled = true
    }

    private fun onCollectionSelected(collection: MyDisplayingUtils.Collection, position: Int) {
        // Update buttonsCurrentlySelected
        if (collection.isSelected) {
            scheduledCollections.add(collection.collectionId)
        } else {
            scheduledCollections.remove(collection.collectionId)
        }

        // Notify the adapter to refresh the row layout for this item
        collectionAdapter.notifyItemChanged(position)

        // Update move and edit buttons based on selection state
        if (scheduledCollections.size >= 2) {
            activateSchedulePlayButton()
        } else {
            deactivateSchedulePlayButton()
        }
    }

    private fun onCollectionClicked(collection: MyDisplayingUtils.Collection, position: Int) {
        intent = Intent(this@MainActivity, TrainingActivity::class.java)
        val b = Bundle()
        b.putString("collectionId", collection.collectionId)
        intent.putExtras(b)
        startActivity(intent)
        finish()
    }

    private fun onCollectionDeleted(collection: MyDisplayingUtils.Collection, position: Int) {
        val collectionName = collection.collectionName
        MyUtils.showConfirmationDialog(this@MainActivity,"Delete Collection: $collectionName", "Are you sure you want to delete this collection?") {userChoice ->
            if (userChoice) {
                listOfCollectionsInCorrectFormat.removeAt(position)
                MyUtils.deleteCollection(context=this@MainActivity, folderOfCollection =collectionPath + "/" + collection.collectionId, flashcardPath=flashcardPath)
                collectionRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = collectionAdapter
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addCollection() {
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val useDateAsCollectionIndex = sharedPref.getBoolean("use_date_as_collection_index", false)

        var indexOfTheNextCollectionToBeCreated = ""
        if (useDateAsCollectionIndex) {
            indexOfTheNextCollectionToBeCreated = MyUtils.getCurrentDate()
        } else {
            indexOfTheNextCollectionToBeCreated =
                sharedPref.getString("index_of_next_collection_to_be_created", "Collection_0")
                    .toString()
        }

        val nativeLanguage = sharedPref.getString("native_language", "German").toString()
        val foreignLanguage = sharedPref.getString("foreign_language", "Spanish").toString()

        val folderName = indexOfTheNextCollectionToBeCreated
        val folderPath = collectionPath + "/" + folderName
        val propertiesFileName = "Properties.txt"
        val flashcardsFileName = "Flashcards.txt"

        if (MyUtils.createFolder(
                this,
                collectionPath,
                folderName,
                "Collection created successfully"
            )
        ) { //only do something if the folder does not exist already
            MyUtils.createTextFile(folderPath, flashcardsFileName)
            MyUtils.writeTextFile(folderPath + "/" + flashcardsFileName, 0, "-")

            MyUtils.createTextFile(folderPath, propertiesFileName)
            MyUtils.writeTextFile(
                folderPath + "/" + propertiesFileName,
                0,
                indexOfTheNextCollectionToBeCreated
            )
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 1, nativeLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 2, foreignLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 3, "-")
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 5, "false")

            editor.putInt(folderPath, 0)
        }

        if (!useDateAsCollectionIndex) {
            var currentIndexNumber =
                indexOfTheNextCollectionToBeCreated.removePrefix("Collection_").toInt()
            currentIndexNumber++
            editor.putString(
                "index_of_next_collection_to_be_created",
                "Collection_$currentIndexNumber"
            )
            editor.apply()
        }
    }
}
