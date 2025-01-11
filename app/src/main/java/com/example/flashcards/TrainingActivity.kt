package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.flashcards.utils.MyUtils
import com.google.android.material.button.MaterialButton

class TrainingActivity: AppCompatActivity() {
    private lateinit var backButton: FloatingActionButton
    private lateinit var flashcardButton: Button
    private lateinit var collectionTitle: TextView
    private lateinit var collectionIndex: TextView
    private lateinit var modeInfo: TextView
    private lateinit var frontToBackButton: Button
    private lateinit var backToFrontButton: Button
    private lateinit var addCardButton: FloatingActionButton
    private lateinit var editCardButton: FloatingActionButton
    private lateinit var shuffleButton: MaterialButton
    private lateinit var autoShuffleButton: MaterialButton
    private lateinit var flashcardCounter: TextView
    private lateinit var flashcardId: TextView
    private lateinit var muteAudioButton: FloatingActionButton
    private lateinit var moveCardButton: FloatingActionButton
    private lateinit var collectionSettingButton: FloatingActionButton
    private lateinit var showAllCardsButton: FloatingActionButton
    private lateinit var repetitionReshuffleButton: FloatingActionButton

    private lateinit var tabPath:String
    private lateinit var flashcardPath:String

    private lateinit var screenReceiver: MyUtils.ScreenReceiver

    private var cardOrder = listOf<String>()
    private var cardIndex: Int = 0
    private var autoShuffle: Boolean = false

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToMainMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        backButton = findViewById(R.id.b_back_flashcards)
        flashcardButton = findViewById(R.id.b_flashcard)
        collectionTitle = findViewById(R.id.collection_title)
        collectionIndex = findViewById(R.id.collection_index_name)
        modeInfo = findViewById(R.id.mode_info)
        frontToBackButton = findViewById(R.id.front_to_back)
        backToFrontButton = findViewById(R.id.back_to_front)
        addCardButton = findViewById(R.id.b_add_card_flashcards)
        editCardButton = findViewById(R.id.b_edit_flashcards)
        shuffleButton = findViewById(R.id.b_shuffle_flashcards)
        autoShuffleButton = findViewById(R.id.b_auto_shuffle)
        flashcardCounter = findViewById(R.id.flashcard_counter)
        flashcardId = findViewById(R.id.flashcard_id)
        muteAudioButton = findViewById(R.id.b_mute_flashcards)
        moveCardButton = findViewById(R.id.b_move_flashcards)
        collectionSettingButton = findViewById(R.id.b_settings_flashcards)
        showAllCardsButton = findViewById(R.id.b_show_flashcards)
        repetitionReshuffleButton = findViewById(R.id.b_repetition_reshuffle)

        //visual
        shuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.background)))
        autoShuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.background)))

        // Register the screen off receiver
        screenReceiver = MyUtils.ScreenReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)

        //define vars
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var frontToBackActive = true
        var backToFrontActive = false
        var showSwitchDialog = false
        var scheduledCollections: List<String> = listOf("")
        var scheduledCollectionIndex = 0
        var scheduledCollectionsString = ""
        var flashcardShowsQuestion = false
        var audioMuted = sharedPref.getBoolean("audio_muted", false)

        val currentTabIndex = sharedPref.getString("currentTabIndex", "ERROR")!!
        tabPath = getExternalFilesDir(null).toString() + "/" + currentTabIndex
        flashcardPath = "$tabPath/Cards"
        val collectionsPath = "$tabPath/Collections"
        var propertiesPath = ""
        var repetitionPropertiesPath = ""
        var collectionPath = ""

        val b = intent.extras
        val v:String? = b?.getString("collectionId")
        val mode:String = b?.getString("mode") ?: "n"
        var nameOfCurrentCollection = v ?:""

        if (mode == "q") {
            scheduledCollectionsString = b?.getString("scheduledCollections").toString()
            scheduledCollections = scheduledCollectionsString.split(" ")
            val p:Int = sharedPref.getInt("scheduledCollectionIndex",0)
            scheduledCollectionIndex = p

            nameOfCurrentCollection = scheduledCollections[scheduledCollectionIndex]
            modeInfo.text = "scheduled mode"
        }   else if (mode == "r") {
            editCardButton.isEnabled = false
            editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))

            repetitionPropertiesPath = tabPath + "/Repetition_Properties.txt"
            
            if (MyUtils.readLineFromFile(repetitionPropertiesPath, 0) != MyUtils.getCurrentDate()) {
                MyUtils.writeTextFile(repetitionPropertiesPath, 0, MyUtils.getCurrentDate())
                cardOrder = getRepetitionQueue(collectionsPath, frontToBackActive, backToFrontActive, repetitionPropertiesPath)
            }   else {
                cardOrder = MyUtils.readLineFromFile(repetitionPropertiesPath, 4)?.split(" ") ?: emptyList()
                if (cardOrder.isEmpty()) {
                    cardOrder = getRepetitionQueue(collectionsPath, frontToBackActive, backToFrontActive, repetitionPropertiesPath)
                }
            }

            collectionTitle.text = "Repetition"
            collectionIndex.text = "Repetition Collection"
            modeInfo.text = "repetition mode"
            setCardCounter()
            nameOfCurrentCollection = "Repetition"

            moveCardButton.isEnabled = false
            moveCardButton.isVisible = false
            addCardButton.isEnabled = false
            addCardButton.isVisible = false
        }

        if (mode != "r") {
            repetitionReshuffleButton.isEnabled = false
            repetitionReshuffleButton.isVisible = false

            collectionPath = "$collectionsPath/$nameOfCurrentCollection"
            propertiesPath = "$collectionPath/Properties.txt"

            setupNewCollection(propertiesPath, collectionPath, frontToBackActive, backToFrontActive, nameOfCurrentCollection, currentTabIndex)

            if (MyUtils.readLineFromFile(propertiesPath, 4) != "" && mode != "q") {
                cardIndex = MyUtils.readLineFromFile(propertiesPath, 4)!!.toInt()
            }
        }

        //setup mute button
        if (audioMuted) {
            muteAudioButton.setImageResource(R.drawable.muted)
        } else {
            muteAudioButton.setImageResource(R.drawable.unmuted)
        }

        backToFrontButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))

        flashcardButton.setOnClickListener{
            if (flashcardShowsQuestion) {
                flashcardShowsQuestion = false

                showSwitchDialog = showAnswer(audioMuted = audioMuted, mode = mode, propertiesPath = propertiesPath, collectionPath = collectionPath, frontToBackActive = frontToBackActive, backToFrontActive = backToFrontActive)
            } else{
                if (showSwitchDialog) {
                    showSwitchDialog = false

                    MyUtils.showConfirmationDialog(context = this, "Queue Next Collection", "You want to switch to the next collection?") { userChoice ->
                        if (userChoice) {
                            scheduledCollectionIndex += 1

                            if (scheduledCollectionIndex == scheduledCollections.size) {
                                scheduledCollectionIndex = 0
                            }

                            nameOfCurrentCollection = scheduledCollections[scheduledCollectionIndex]
                            collectionPath = "$collectionsPath/$nameOfCurrentCollection"
                            propertiesPath = "$collectionPath/Properties.txt"

                            setupNewCollection(propertiesPath, collectionPath, frontToBackActive, backToFrontActive, nameOfCurrentCollection, currentTabIndex)

                            editor.putInt("scheduledCollectionIndex", scheduledCollectionIndex)
                            editor.apply()
                        } else {
                            flashcardShowsQuestion = true
                            showQuestion(audioMuted = audioMuted)
                        }
                    }
                } else {
                    flashcardShowsQuestion = true
                    showQuestion(audioMuted = audioMuted)
                }
            }
        }

        muteAudioButton.setOnClickListener {
            if (audioMuted) {
                audioMuted = false
                editor.putBoolean("audio_muted", false)
                muteAudioButton.setImageResource(R.drawable.unmuted)
            } else {
                audioMuted = true
                editor.putBoolean("audio_muted", true)
                MyUtils.stopAudio()
                muteAudioButton.setImageResource(R.drawable.muted)
            }
            editor.apply()
        }

        moveCardButton.setOnClickListener {
            MyUtils.stopAudio()

            val currentCardIndex = getCorrectCardIndex(flashcardShowsQuestion, propertiesPath)

            val cardString = flashcardButton.text.toString()

            val collectionIds = MyUtils.getFoldersInDirectory(collectionsPath)
            val sortedCollectionIds = MyUtils.sortCollectionStrings(collectionIds)

            val collectionTuple = mutableListOf<MyUtils.SpinnerItem>()
            for (collectionId in sortedCollectionIds) {
                val collectionName = MyUtils.readLineFromFile("$collectionsPath/$collectionId/Properties.txt", 0)
                collectionName?.let { it1 -> MyUtils.SpinnerItem(it1, collectionId) }
                    ?.let { it2 -> collectionTuple.add(it2) }
            }

            MyUtils.showDropdownDialog(this,"Move Card: $cardString", "Choose the collection this card should be moved to", collectionTuple) { isConfirmed, selectedItem ->
                if (isConfirmed) {
                    if (selectedItem != null) {
                        MyUtils.moveCardToCollection(context=this, oldCollectionPath = collectionPath, newCollectionPath = collectionsPath + "/" + selectedItem.description, cardName = currentCardIndex, pathOfTheFlashcard = "$flashcardPath/$currentCardIndex")

                        MyUtils.createShortToast(this, "Moved to collection: ${selectedItem.description}")

                        //reload the activity
                        val intent = intent
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        finish()
                        startActivity(intent)
                    }
                } else {
                    MyUtils.createShortToast(this, "Cancelled")
                }
            }
        }

        frontToBackButton.setOnClickListener {
            if (frontToBackActive) { //deactivate button
                frontToBackActive = false
                frontToBackButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))
                if (!backToFrontActive) {
                    backToFrontActive = true
                    backToFrontButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                }
            }   else {
                frontToBackActive = true
                frontToBackButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            }
        }

        backToFrontButton.setOnClickListener {
            if (backToFrontActive) { //deactivate button
                backToFrontActive = false
                backToFrontButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))
                if (!frontToBackActive) {
                    frontToBackActive = true
                    frontToBackButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                }
            }   else {
                backToFrontActive = true
                backToFrontButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            }
        }

        //back button
        backButton.setOnClickListener {
            backToMainMenu()
        }

        editCardButton.setOnClickListener { //activity switch
            MyUtils.stopAudio()

            val currentCard = getCorrectCardIndex(flashcardShowsQuestion, propertiesPath)

            intent = Intent(this, AddCardActivity::class.java)
            val bu = Bundle()
            bu.putString("collectionPath", collectionPath)
            bu.putString("collectionId", nameOfCurrentCollection)
            bu.putBoolean("calledFromAddCard", false)
            bu.putString("cardName", currentCard)
            bu.putBoolean("calledFromList", false)
            bu.putString("mode", mode)
            bu.putString("scheduledCollections", scheduledCollectionsString)
            intent.putExtras(bu)
            startActivity(intent)
            finish()
        }

        addCardButton.setOnClickListener { //activity switch
            MyUtils.stopAudio()
            intent = Intent(this, AddCardActivity::class.java)
            val bu = Bundle()
            bu.putString("collectionPath", collectionPath)
            bu.putString("collectionId", nameOfCurrentCollection)
            bu.putBoolean("calledFromAddCard", true)
            bu.putBoolean("calledFromList", false)

            bu.putString("mode", mode)
            bu.putString("scheduledCollections", scheduledCollectionsString)
            intent.putExtras(bu)
            startActivity(intent)
            finish()
        }

        showAllCardsButton.setOnClickListener { //activity switch
            MyUtils.stopAudio()
            intent = Intent(this, FlashcardListActivity::class.java)
            val bu = Bundle()
            bu.putString("flashcardPath", flashcardPath)
            bu.putString("collectionPath", collectionPath)
            bu.putString("collectionId", nameOfCurrentCollection)
            bu.putString("mode", mode)
            bu.putString("scheduledCollections", scheduledCollectionsString)
            intent.putExtras(bu)
            startActivity(intent)
            finish()
        }

        shuffleButton.setOnClickListener {
            //visuals
            shuffleButton.setTextColor(ContextCompat.getColor(this, R.color.strong_highlight))
            shuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.strong_highlight)))
            Handler(Looper.getMainLooper()).postDelayed({
                shuffleButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                shuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.background)))
            }, 400)

            //logic
            MyUtils.stopAudio()
            MyUtils.showConfirmationDialog(this,"Shuffle Cards", "Are you sure you want to shuffle?") {userChoice ->
                if (userChoice) {
                    if (mode != "r") {
                        cardOrder = shuffleCards(collectionPath, frontToBackActive, backToFrontActive)
                        cardIndex = 0
                        MyUtils.writeTextFile(propertiesPath, 4, "0")
                    }   else {
                        cardOrder = shuffleRepetitionQueue(cardOrder, repetitionPropertiesPath)
                    }

                    flashcardShowsQuestion = false

                    setFlashcardText("Start")
                    flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                    editCardButton.isEnabled = false
                    editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
                }
            }
        }

        autoShuffleButton.setOnClickListener {
            autoShuffleButton.setTextColor(ContextCompat.getColor(this, R.color.strong_highlight))
            Handler(Looper.getMainLooper()).postDelayed({
                autoShuffleButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            }, 200)
            if (autoShuffle) {
                autoShuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.background)))
                shuffleButton.isEnabled = true
                shuffleButton.setTextColor(ContextCompat.getColor(this, R.color.white))

                if (mode == "q") {
                    modeInfo.text = "scheduled mode"
                }
            } else {
                autoShuffleButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
                shuffleButton.isEnabled = false
                shuffleButton.setTextColor(ContextCompat.getColor(this, R.color.button_color))

                if (mode == "q") {
                    modeInfo.text = "scheduled mode deactivated"
                }
            }
            autoShuffle = !autoShuffle
        }

        repetitionReshuffleButton.setOnClickListener {
            MyUtils.showConfirmationDialog(this,"Get New Repetition Cards", "Are you sure you want to get new repetition cards?") {userChoice ->
                if (userChoice) {
                    cardOrder = getRepetitionQueue(collectionsPath, frontToBackActive, backToFrontActive, repetitionPropertiesPath)
                    flashcardShowsQuestion = false

                    setFlashcardText("Start")
                    flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                    editCardButton.isEnabled = false
                    editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
                }
            }
        }

        collectionSettingButton.setOnClickListener {
            MyUtils.stopAudio()
            intent = Intent(this, CollectionSettingsActivity::class.java)
            val bu = Bundle()
            bu.putString("collectionPath", collectionPath)
            bu.putString("collectionId", nameOfCurrentCollection)

            bu.putString("mode", mode)
            bu.putString("scheduledCollections", scheduledCollectionsString)
            intent.putExtras(bu)
            startActivity(intent)
            finish()
        }
    }

    private fun getRepetitionQueue(collectionsPath: String, frontToBack: Boolean, backToFront: Boolean, repetitionPropertiesPath: String):List<String> {
        val excludedCollectionsString = MyUtils.readLineFromFile(repetitionPropertiesPath, 1)
        val numberOfCollections = MyUtils.readLineFromFile(repetitionPropertiesPath, 2)!!.toInt()
        val numberOfCards = MyUtils.readLineFromFile(repetitionPropertiesPath, 3)!!.toInt()

        val excludedCollections = excludedCollectionsString!!.split(" ")

        val collectionIds = MyUtils.getFoldersInDirectory(collectionsPath, false).toMutableList()
        collectionIds.removeAll(excludedCollections)

        collectionIds.shuffle()

        var usedCollections = collectionIds.take(numberOfCollections).toMutableList()

        var usedCards = mutableListOf<String>()
        for (collection in usedCollections) {
            val cardsInCollection = prepareCards(collectionsPath + "/" + collection, frontToBack, backToFront)
            val usedCardsInCollection = cardsInCollection.take(numberOfCards)
            usedCards.addAll(usedCardsInCollection)
        }
        usedCards = shuffleRepetitionQueue(usedCards, repetitionPropertiesPath)
        return usedCards.toList()
    }

    private fun shuffleRepetitionQueue(cards:List<String>, repetitionPropertiesPath:String):MutableList<String> {
        val cardsm = cards.toMutableList()
        cardsm.shuffle()
        MyUtils.writeTextFile(repetitionPropertiesPath, 4, cardsm.joinToString(" "))
        return cardsm
    }

    private fun showQuestion(audioMuted: Boolean) {

        flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        if (!editCardButton.isEnabled) {
            editCardButton.isEnabled = true
            editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
        }
        if (!moveCardButton.isEnabled) {
            moveCardButton.isEnabled = true
            moveCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
        }

        var line = 1
        var audioFile = "/foreign.mp3"
        if (cardOrder[cardIndex].first() == 'n') {
            line = 0
            audioFile = "/native.mp3"
        }

        if (!audioMuted) {
            MyUtils.stopAudio()
            MyUtils.playAudio(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + audioFile)
        }

        setFlashcardText(MyUtils.readLineFromFile(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + "/Content.txt", line)!!)
        flashcardId.text = cardOrder[cardIndex].substring(2)
    }

    private fun showAnswer(audioMuted:Boolean, mode:String, propertiesPath: String, collectionPath: String, frontToBackActive: Boolean, backToFrontActive: Boolean): Boolean {
        var showSwitchDialog = false

        flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))

        var line = 0
        var audioFile = "/native.mp3"
        if (cardOrder[cardIndex].first() == 'n') {
            line = 1
            audioFile = "/foreign.mp3"
        }

        if (!audioMuted) {
            MyUtils.stopAudio()
            MyUtils.playAudio(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + audioFile)
        }

        setFlashcardText(MyUtils.readLineFromFile(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + "/Content.txt", line)!!)
        flashcardId.text = cardOrder[cardIndex].substring(2)

        if (cardIndex+1 == cardOrder.size) { //reseting the card index to zero when the user skipped through the whole orderline
            if (autoShuffle) {
                if (mode == "r") {

                }   else {
                    cardOrder = shuffleCards(collectionPath, frontToBackActive, backToFrontActive)
                    MyUtils.writeTextFile(propertiesPath, 4, "0")
                }
            } else if (mode == "q") {
                showSwitchDialog = true
            }
            cardIndex = 0
        }   else {
            cardIndex++
        }

        if (mode != "r") {
            MyUtils.writeTextFile(propertiesPath, 4, cardIndex.toString())
        }
        return showSwitchDialog
    }

    private fun setFlashcardText(textString:String) {
        val modifiedTextString = textString.replace("$","\n")
        if (textString == modifiedTextString) {
            flashcardButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
        }   else {
            flashcardButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START)
        }
        flashcardButton.text = modifiedTextString
    }

    private fun setCardCounter() {
        flashcardCounter.text = cardOrder.size.toString() + " Cards"
    }

    @SuppressLint("SetTextI18n")
    private fun setupNewCollection(propertiesPath: String, collectionPath: String, frontToBackActive: Boolean, backToFrontActive: Boolean, nameOfCurrentCollection: String, currentTabIndex:String) {
        //read card order from file if exists, otherwise shuffle
        getOrderLine(propertiesPath, collectionPath, frontToBackActive, backToFrontActive)

        moveCardButton.isEnabled = false
        moveCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        editCardButton.isEnabled = false
        editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        setCardCounter()

        flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        setFlashcardText("Start")

        if (cardOrder.isEmpty()) {
            flashcardButton.isEnabled = false
            flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
            shuffleButton.isEnabled = false
            shuffleButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        }


        collectionTitle.text = MyUtils.readLineFromFile(propertiesPath, 0)

        collectionIndex.text = "$nameOfCurrentCollection from $currentTabIndex"

        frontToBackButton.text = MyUtils.readLineFromFile(propertiesPath, 1) + " - " + MyUtils.readLineFromFile(propertiesPath, 2)
        backToFrontButton.text = MyUtils.readLineFromFile(propertiesPath, 2) + " - " + MyUtils.readLineFromFile(propertiesPath, 1)
    }

    private fun getOrderLine(propertiesPath: String, collectionPath: String, frontToBackActive: Boolean, backToFrontActive: Boolean) {
        //read card order from file if exists, otherwise shuffle
        val orderLine = MyUtils.readLineFromFile(propertiesPath, 3)
        if (orderLine == "-") {
            cardOrder = shuffleCards(collectionPath, frontToBackActive, backToFrontActive)
            cardIndex = 0
        }   else {
            if (orderLine != null) {
                cardOrder = orderLine.split(" ")
            }   else {
                Log.e("HUGE ERROR", "orderLine is null")
            }
        }
    }

    private fun getCorrectCardIndex(flashcardShowsQuestion:Boolean, propertiesPath:String):String {
        var currentCard = ""
        if (!flashcardShowsQuestion && cardIndex != 0) {
            currentCard = cardOrder[cardIndex-1]
            MyUtils.writeTextFile(propertiesPath, 4, (cardIndex-1).toString())
        }   else if (!flashcardShowsQuestion && cardIndex == 0) {
            currentCard = cardOrder[cardOrder.size-1]
            MyUtils.writeTextFile(propertiesPath, 4, (cardOrder.size-1).toString())
        }   else if (flashcardShowsQuestion) {
            currentCard = cardOrder[cardIndex]
        }
        return currentCard.substring(2)
    }

    private fun backToMainMenu() {
        MyUtils.stopAudio()
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun shuffleCards(collectionPath: String, frontToBack: Boolean, backToFront: Boolean): List<String> {
        val shuffledCards = prepareCards(collectionPath, frontToBack, backToFront)

        if (shuffledCards.isNotEmpty()) {
            MyUtils.writeTextFile("$collectionPath/Properties.txt", 3, shuffledCards.joinToString(" "))
        }   else {
            MyUtils.writeTextFile("$collectionPath/Properties.txt", 3, "-")
        }

        //set the index
        MyUtils.writeTextFile("$collectionPath/Properties.txt", 4, "0")

        return shuffledCards
    }
    
    private fun prepareCards(collectionPath:String, frontToBack:Boolean, backToFront: Boolean):MutableList<String> {
        val cards = MyUtils.getCardFolderNames(collectionPath)
        val prefixes = listOf("n_", "f_")
        val cardlist = mutableListOf<String>()

        if (frontToBack && !backToFront) {
            val prefix = prefixes[0]
            for (card in cards) {
                cardlist.add("$prefix$card")
            }
        }  else if (backToFront && !frontToBack) {
            val prefix = prefixes[1]
            for (card in cards) {
                cardlist.add("$prefix$card")
            }
        }   else {
            for (card in cards) {
                val prefix = prefixes.random()
                cardlist.add("$prefix$card")
            }
        }
        cardlist.shuffle()
        return cardlist
    }
}