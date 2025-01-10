package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
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
    private lateinit var buttonPlayScheduled: FloatingActionButton
    private lateinit var tabLayout: com.google.android.material.tabs.TabLayout
    private var collectionDisplayWidth = 320
    private var collectionDisplayHeight = 40
    lateinit var tabPath: String
    lateinit var collectionPath: String
    val scheduledCollections = mutableListOf<String>()
    var sortedCollections = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsButton = findViewById(R.id.b_settings)
        container = findViewById(R.id.container)
        buttonAdd = findViewById(R.id.button_add)
        showArchivedCollections = findViewById(R.id.show_archived_collections)
        buttonPlayScheduled = findViewById(R.id.b_play_scheduled)
        tabLayout = findViewById(R.id.tabLayout)

        resetQueues()

        val appPath = getExternalFilesDir(null).toString()
        val tabs = MyUtils.getFoldersInDirectory(appPath)

        if (tabs.isNotEmpty()) {

            for (tab in tabs) {
                addTabButton(tabLayout, MyUtils.readLineFromFile(appPath + "/" + tab + "/" + "/Settings.txt", 0)!!, appPath, tabs)
            }

            val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
            val tabIndex = sharedPref.getString("currentTabIndex", tabs[0])

            if (tabIndex in tabs) {
                tabPath = appPath + "/" + tabIndex
                setTab(tabIndex!!)
                setTabViewByName(tabLayout, MyUtils.readLineFromFile(tabPath + "/Settings.txt", 0)!!)
            }   else {
                tabPath = appPath + "/" + tabs[0]
                setTab(tabs[0])
            }
            collectionPath = tabPath + "/Collections"


            init()
        }   else {
            deactivateAllButtons()
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
                for (collection in sortedCollections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "true") {
                        addCollectionButtons(collection, false, R.color.archived)
                    }
                }
            }   else {
                container.removeAllViews()
                for (collection in sortedCollections) {
                    if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                        addCollectionButtons(collection, false)
                    }
                }
            }
        }
    }

    private fun init(){
        //scan for collections
        sortedCollections = MyUtils.getFoldersInDirectory(collectionPath, true)

        if (MyUtils.readLineFromFile(tabPath + "/Settings.txt",6).toBoolean() == false) {
            sortedCollections = MyUtils.sortCollectionStrings(sortedCollections)
        }

        container.removeAllViews()
        for (collection in sortedCollections) {
            if (MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "false" || MyUtils.readLineFromFile(collectionPath + "/" + collection + "/Properties.txt", 5) == "") {
                addCollectionButtons(collection, false)
            }
        }
        activateAllButtons()
    }

    fun setTabViewByName(tabLayout: com.google.android.material.tabs.TabLayout, tabName: String) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab?.text == tabName) {
                tab.select() // Select the tab
                break
            }
        }
    }

    private fun setTab(tabIndex: String) {
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("currentTabIndex", tabIndex)
        editor.apply()
    }

    private fun resetQueues() {
        //reset any queues and deactivate the queue play button
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("scheduledCollectionIndex", 0)
        editor.apply()
        deactivateSchedulePlayButton()
        scheduledCollections.clear()
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun deactivateSchedulePlayButton () {
        buttonPlayScheduled.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        buttonPlayScheduled.isEnabled = false
    }

    private fun activateSchedulePlayButton () {
        buttonPlayScheduled.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        buttonPlayScheduled.isEnabled = true
    }

    private fun deactivateAllButtons() {
        deactivateSchedulePlayButton()
        settingsButton.isEnabled = false
        settingsButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        buttonAdd.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        buttonAdd.isEnabled = false
        showArchivedCollections.isEnabled = false
        showArchivedCollections.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))
    }

    private fun activateAllButtons() {
        settingsButton.isEnabled = true
        settingsButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
        buttonAdd.isEnabled = true
        buttonAdd.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        showArchivedCollections.isEnabled = true
        showArchivedCollections.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
    }

    private fun addTabButton(tabLayout: com.google.android.material.tabs.TabLayout, tabName: String, appPath:String, tabs:List<String>) {
        val tab = tabLayout.newTab()
        tab.text = tabName

        tabLayout.addTab(tab)

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val tabIndex = tab?.position ?: 0

                tabPath = appPath + "/" + tabs[tabIndex]
                collectionPath = tabPath + "/Collections"
                init()
                setTab(tabs[tabIndex])

                //clear queued collections
                resetQueues()
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun addCollection() {
        val tabSettingsPath = tabPath + "/Settings.txt"

        val useDateAsCollectionIndexString = MyUtils.readLineFromFile(tabSettingsPath,3)
        var useDateAsCollectionIndex = false
        if (useDateAsCollectionIndexString == "true") {
            useDateAsCollectionIndex = true
        }

        var indexOfTheNextCollectionToBeCreated = ""
        if (useDateAsCollectionIndex) {
            indexOfTheNextCollectionToBeCreated = MyUtils.getCurrentDate()
        }   else {
            indexOfTheNextCollectionToBeCreated = MyUtils.readLineFromFile(tabSettingsPath, 4)!!
        }

        val nativeLanguage = MyUtils.readLineFromFile(tabSettingsPath, 1)!!
        val foreignLanguage = MyUtils.readLineFromFile(tabSettingsPath, 2)!!

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
        }

        if (!useDateAsCollectionIndex) {
            var currentIndexNumber = indexOfTheNextCollectionToBeCreated.removePrefix("Collection_").toInt()
            currentIndexNumber++
            MyUtils.writeTextFile(tabSettingsPath, 4,"Collection_$currentIndexNumber")
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

        val flashcardPath = tabPath + "/Cards"
        val collectionName = MyUtils.readLineFromFile(collectionPath + "/$collectionId/Properties.txt", 0)

        // Create the selection button (square)
        val selectionButton = Button(this).apply {

            contentDescription = ""
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                collectionDisplayHeight.dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(20, 0, 0, 4)
            }

            setBackgroundColor(ContextCompat.getColor(this@MainActivity, collectionButtonColor))

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

                    scheduledCollections.add(collectionId)

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

                    scheduledCollections.remove(collectionId)

                    val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.checkbox_unchecked) // Replace with your drawable
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)  // Set image (bottom)

                    // Apply color filter to the drawable
                    drawable?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.highlight), PorterDuff.Mode.SRC_IN)

                    // Resize the drawable to fit the button
                    drawable?.setBounds(0, 0, (collectionDisplayHeight*3).toInt(), (collectionDisplayHeight*3).toInt())

                    // Set the scaled drawable on the button (e.g., centered without text)
                    setCompoundDrawables(null, null, null, drawable)
                }

                if (scheduledCollections.isEmpty()) {
                    deactivateSchedulePlayButton()
                }   else if (scheduledCollections.size == 1) {
                    activateSchedulePlayButton()
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