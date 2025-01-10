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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcards.databinding.ActivityMainBinding
import com.example.flashcards.utils.MyDisplayingUtils
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var buttonAdd: FloatingActionButton
    private lateinit var showArchivedCollections: CheckBox
    private lateinit var buttonPlayScheduled: FloatingActionButton
    private lateinit var tabLayout: com.google.android.material.tabs.TabLayout

    private lateinit var collectionAdapter: MyDisplayingUtils.CollectionAdapter
    private lateinit var collectionRecyclerView: RecyclerView
    private lateinit var listOfCollectionsInCorrectFormat: MutableList<MyDisplayingUtils.Collection>

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
        buttonAdd = findViewById(R.id.button_add)
        showArchivedCollections = findViewById(R.id.show_archived_collections)
        buttonPlayScheduled = findViewById(R.id.b_play_scheduled)
        tabLayout = findViewById(R.id.tabLayout)

        collectionRecyclerView = findViewById(R.id.collection_recycler_view)
        listOfCollectionsInCorrectFormat = mutableListOf<MyDisplayingUtils.Collection>()

        resetQueues() //REVIEW probably not neccesary

        val appPath = getExternalFilesDir(null).toString()
        val tabs = MyUtils.getFoldersInDirectory(appPath)


        if (tabs.isNotEmpty()) {
            val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
            val tabIndex = sharedPref.getString("currentTabIndex", tabs[0])

            setupTabLayout(tabLayout, appPath, tabs)

            for (tab in tabs) {
                addTabButton(tabLayout, MyUtils.readLineFromFile(appPath + "/" + tab + "/" + "/Settings.txt", 0)!!)
            }

            if (tabIndex in tabs) {
                tabPath = appPath + "/" + tabIndex
                setTabToSharedPref(tabIndex!!)
                setTabViewByName(tabLayout, MyUtils.readLineFromFile(tabPath + "/Settings.txt", 0)!!)
            }   else {
                tabPath = appPath + "/" + tabs[0]
                setTabToSharedPref(tabs[0])
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
                addToCollectionList(true)
                updateCollectionView()
            } else {
                removeArchivedFromCollectionList()
                updateCollectionView()
            }
        }
    }

    private fun init(){
        //scan for collections
        if (MyUtils.readLineFromFile(tabPath + "/Settings.txt",6).toBoolean() == false) {
            sortedCollections = MyUtils.sortCollectionStrings(MyUtils.getFoldersInDirectory(collectionPath, false))
        }   else {
            sortedCollections = MyUtils.getFoldersInDirectory(collectionPath, true)
        }

        listOfCollectionsInCorrectFormat.clear()
        addToCollectionList(false)

        createCollectionAdapter()
        updateCollectionView()
        activateAllButtons()
    }

    private fun createCollectionAdapter() {
        collectionAdapter = MyDisplayingUtils.CollectionAdapter(
            listOfCollectionsInCorrectFormat,
            { collection, position -> onCollectionSelected(collection, position) },
            { collection, _ -> onCollectionClicked(collection) },
            { collection, position -> onCollectionDeleted(collection, position) }
        )
    }

    private fun addToCollectionList(addArchivedCollections: Boolean) {
//        var i = 0
        for (collectionId in sortedCollections) {
//            i = i + 1
//            if (i < 5) {
                if (MyUtils.readLineFromFile(collectionPath + "/" + collectionId + "/Properties.txt", 5) == addArchivedCollections.toString()) {
                    listOfCollectionsInCorrectFormat.add(MyDisplayingUtils.Collection(collectionId, MyUtils.readLineFromFile(collectionPath + "/$collectionId/Properties.txt", 0)!!, false, addArchivedCollections))
                }
//            }
        }
    }

    private fun removeArchivedFromCollectionList() {
        val archivedCollections = mutableListOf<MyDisplayingUtils.Collection>()
        for (collection in listOfCollectionsInCorrectFormat) {
            if (collection.isArchived) {
                archivedCollections.add(collection)
            }
        }
        listOfCollectionsInCorrectFormat.removeAll(archivedCollections)
    }

    private fun updateCollectionView() {
        collectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = collectionAdapter
        }
    }

    private fun onCollectionSelected(collection: MyDisplayingUtils.Collection, position: Int) {
        if (collection.isSelected) {
            scheduledCollections.add(collection.collectionId)
        } else {
            scheduledCollections.remove(collection.collectionId)
        }

        collectionAdapter.notifyItemChanged(position)

        if (scheduledCollections.size >= 2) { // Update move and edit buttons based on selection state
            activateSchedulePlayButton()
        } else {
            deactivateSchedulePlayButton()
        }
    }

    private fun onCollectionClicked(collection: MyDisplayingUtils.Collection) {
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
                MyUtils.deleteCollection(context=this@MainActivity, folderOfCollection =collectionPath + "/" + collection.collectionId, flashcardPath= tabPath + "/Cards")
                updateCollectionView()
            }
        }
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

    private fun setTabToSharedPref(tabIndex: String) {
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("currentTabIndex", tabIndex)
        editor.apply()
    }

    private fun resetQueues() { //reset any queues and deactivate the queue play button
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("scheduledCollectionIndex", 0)
        editor.apply()
        deactivateSchedulePlayButton()
        scheduledCollections.clear()
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

    private fun setupTabLayout(tabLayout: com.google.android.material.tabs.TabLayout, appPath: String, tabs: List<String>) {
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val s = MyUtils.startTimer()
                val tabIndex = tab?.position ?: 0

                tabPath = appPath + "/" + tabs[tabIndex]
                collectionPath = tabPath + "/Collections"
                init()
                setTabToSharedPref(tabs[tabIndex])

                // Clear queued collections
                resetQueues()
                MyUtils.endTimer(s)
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
            }
        })
    }

    private fun addTabButton(tabLayout: com.google.android.material.tabs.TabLayout, tabName: String) {
        val tab = tabLayout.newTab()
        tab.text = tabName
        tabLayout.addTab(tab)
    }

//    private fun addTabButton(tabLayout: com.google.android.material.tabs.TabLayout, tabName: String, appPath:String, tabs:List<String>) {
//        val tab = tabLayout.newTab()
//        tab.text = tabName
//
//        tabLayout.addTab(tab)
//
//        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
//                Log.e("f", "tab selected")
//                val tabIndex = tab?.position ?: 0
//
//                tabPath = appPath + "/" + tabs[tabIndex]
//                collectionPath = tabPath + "/Collections"
//                init()
//                setTabToSharedPref(tabs[tabIndex])
//
//                //clear queued collections
//                resetQueues()
//            }
//
//            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
//            }
//
//            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
//            }
//        })
//    }

    private fun addCollection() {
        val tabSettingsPath = tabPath + "/Settings.txt"

        var indexOfCollection: String
        val useDateAsCollectionIndex = MyUtils.readLineFromFile(tabSettingsPath,3).toBoolean()
        if (useDateAsCollectionIndex) {
            indexOfCollection = MyUtils.getCurrentDate()
        }   else {
            indexOfCollection = MyUtils.readLineFromFile(tabSettingsPath, 4)!!

            var currentIndexNumber = indexOfCollection.removePrefix("Collection_").toInt()
            currentIndexNumber++
            MyUtils.writeTextFile(tabSettingsPath, 4,"Collection_$currentIndexNumber")
        }

        val nativeLanguage = MyUtils.readLineFromFile(tabSettingsPath, 1)!!
        val foreignLanguage = MyUtils.readLineFromFile(tabSettingsPath, 2)!!

        val folderName = indexOfCollection
        val folderPath = collectionPath + "/" + folderName
        val propertiesFileName = "Properties.txt"
        val flashcardsFileName = "Flashcards.txt"

        if (MyUtils.createFolder(this, collectionPath, folderName, "Collection created successfully")) { //only do something if the folder does not exist already
            MyUtils.createTextFile(folderPath, flashcardsFileName)
            MyUtils.writeTextFile(folderPath + "/" + flashcardsFileName, 0, "-")

            MyUtils.createTextFile(folderPath, propertiesFileName)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 0, indexOfCollection)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 1, nativeLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 2, foreignLanguage)
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 3, "-")
            MyUtils.writeTextFile(folderPath + "/" + propertiesFileName, 5, "false")

            listOfCollectionsInCorrectFormat.add(0, MyDisplayingUtils.Collection(indexOfCollection, indexOfCollection, false, false))
            updateCollectionView()
        }
    }
}