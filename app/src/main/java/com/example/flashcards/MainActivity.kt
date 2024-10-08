package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
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
    private lateinit var buttonAdd: FloatingActionButton
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

        val sortedCollections = MyUtils.sortCollectionStrings(collections)

        for (collection in sortedCollections) {
            if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                addCollectionButtons(collection, false)
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
                        addCollectionButtons(collection, false, R.color.archived)
                    }
                }
            }   else {
                container.removeAllViews()
                for (collection in collections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                        addCollectionButtons(collection, false)
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

        val useDateAsCollectionIndex = sharedPref.getBoolean("use_date_as_collection_index", false)

        var indexOfTheNextCollectionToBeCreated = ""
        if (useDateAsCollectionIndex) {
            indexOfTheNextCollectionToBeCreated = MyUtils.getCurrentDate()
        }   else {
            indexOfTheNextCollectionToBeCreated =
                sharedPref.getString("index_of_next_collection_to_be_created", "Collection_0").toString()
        }

        val nativeLanguage = sharedPref.getString("native_language", "German").toString()
        val foreignLanguage = sharedPref.getString("foreign_language", "Spanish").toString()

        val folderName = indexOfTheNextCollectionToBeCreated
        val folderPath = collectionPath + "/" + folderName
        val propertiesFileName = "Properties.txt"
        val flashcardsFileName = "Flashcards.txt"

        if (MyUtils.createFolder(this, collectionPath, folderName, "Collection created successfully")) { //only do something if the folder does not exist already
            MyUtils.createTextFile(folderPath, flashcardsFileName)
            MyUtils.writeTextFile(folderPath + "/" + flashcardsFileName, 0, "-")

            MyUtils.createTextFile(folderPath, propertiesFileName)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 0, indexOfTheNextCollectionToBeCreated)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 1, nativeLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 2, foreignLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 3, "-")
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 5, "false")

            addCollectionButtons(indexOfTheNextCollectionToBeCreated, true)

            editor.putInt(folderPath, 0)
        }

        if (!useDateAsCollectionIndex) {
            var currentIndexNumber = indexOfTheNextCollectionToBeCreated.removePrefix("Collection_").toInt()
            currentIndexNumber++
            editor.putString("index_of_next_collection_to_be_created", "Collection_$currentIndexNumber")
            editor.apply()
        }
    }

    private fun addCollectionButtons(collectionId:String, isNewCreated:Boolean, collectionButtonColor:Int = R.color.primary) {

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

        val flashcardPath = appPath + "/Cards"
        val collectionName = MyUtils.readLineFromFile(collectionPath + "/$collectionId/Properties.txt", 0)

        // Create the selection button (square)
        val selectionButton = Button(this).apply {

            contentDescription = collectionId
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                collectionDisplayHeight.dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(20, 0, 0, 4)
            }

            setBackgroundColor(ContextCompat.getColor(this@MainActivity, collectionButtonColor))

            // Set the image on the button (without text)
            contentDescription = ""
            val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.checkbox_unchecked) // Replace with your drawable
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)  // Set image (bottom)

            // Apply color filter to the drawable
            drawable?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.highlight), PorterDuff.Mode.SRC_IN)

            // Resize the drawable to fit the button
            drawable?.setBounds(0, 0, (collectionDisplayHeight*3).toInt(), (collectionDisplayHeight*3).toInt())

            // Set the scaled drawable on the button (e.g., centered without text)
            setCompoundDrawables(null, null, null, drawable)  // Set image (bottom)

            // Optionally adjust padding
            setPadding(0, 0, 0, -5)

            // Apply a vertical offset to manually adjust the position (negative to move up)
            translationY = -27f  // Adjust this value to get the perfect alignment

            setOnClickListener {
                if (contentDescription == "") {
                    contentDescription = "selected"

                    val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.checkbox_checked) // Replace with your drawable
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)  // Set image (bottom)

                    // Apply color filter to the drawable
                    drawable?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.highlight), PorterDuff.Mode.SRC_IN)

                    // Resize the drawable to fit the button
                    drawable?.setBounds(0, 0, (collectionDisplayHeight*3).toInt(), (collectionDisplayHeight*3).toInt())

                    // Set the scaled drawable on the button (e.g., centered without text)
                    setCompoundDrawables(null, null, null, drawable)
                }   else {
                    contentDescription = ""

                    val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.checkbox_unchecked) // Replace with your drawable
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)  // Set image (bottom)

                    // Apply color filter to the drawable
                    drawable?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.highlight), PorterDuff.Mode.SRC_IN)

                    // Resize the drawable to fit the button
                    drawable?.setBounds(0, 0, (collectionDisplayHeight*3).toInt(), (collectionDisplayHeight*3).toInt())

                    // Set the scaled drawable on the button (e.g., centered without text)
                    setCompoundDrawables(null, null, null, drawable)
                }
            }
        }

        //create the collection button (wider)
        val collectionButton = Button(this).apply {

            if (isNewCreated) {
                text = collectionId
            }   else {
                text = collectionName
            }

            isAllCaps = false

            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                (collectionDisplayWidth - (2*collectionDisplayHeight)).dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(0,0, 0, 4)
            }
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, collectionButtonColor))
            setOnClickListener {
                intent = Intent(this@MainActivity, TrainingActivity::class.java)
                val b = Bundle()
                b.putString("collectionId", collectionId)
                intent.putExtras(b)
                startActivity(intent)
                finish()
            }
        }

        // Create the delete button (square)
        val deleteCollectionButton = Button(this).apply {
            contentDescription = collectionId
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                collectionDisplayHeight.dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(0, 0, 20, 4)
            }

            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.delete_highlight))

            // Set the image on the button (without text)
            text = ""  // Optional: If you want no text
            val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.delete) // Replace with your drawable
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)  // Set image (bottom)

            // Resize the drawable to fit the button
            drawable?.setBounds(0, 0, (collectionDisplayHeight*2).toInt(), (collectionDisplayHeight*2).toInt())

            // Set the scaled drawable on the button (e.g., centered without text)
            setCompoundDrawables(null, null, null, drawable)  // Set image (bottom)

            // Optionally adjust padding
            setPadding(0, 0, 0, 13)

            // Apply a vertical offset to manually adjust the position (negative to move up)
            translationY = -27f  // Adjust this value to get the perfect alignment

            setOnClickListener { button ->
                MyUtils.showConfirmationDialog(this@MainActivity,"Delete Collection: $collectionName", "Are you sure you want to delete this collection?") {userChoice ->
                    if (userChoice) {
                        container.removeView(rowLayout)
                        MyUtils.deleteCollection(context=this@MainActivity, folderOfCollection =collectionPath + "/" + button.contentDescription.toString(), flashcardPath=flashcardPath)
                    }
                }
            }
        }

        // Add buttons to the horizontal layout
        rowLayout.addView(selectionButton)
        rowLayout.addView(collectionButton)
        rowLayout.addView(deleteCollectionButton)

        // Add the horizontal layout to the container
        container.addView(rowLayout)
    }
}