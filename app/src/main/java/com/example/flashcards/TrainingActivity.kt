package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.flashcards.utils.MyUtils
import java.io.File

class TrainingActivity: AppCompatActivity() {
    private lateinit var backButton: FloatingActionButton
    private lateinit var flashcardButton: Button
    private lateinit var collectionTitle: TextView
    private lateinit var collectionIndex: TextView
    private lateinit var nativeToForeignButton: Button
    private lateinit var foreignToNativeButton: Button
    private lateinit var addCardButton: FloatingActionButton
    private lateinit var editCardButton: FloatingActionButton
    private lateinit var shuffleButton: FloatingActionButton
    private lateinit var flashcardCounter: TextView
    private lateinit var flashcardId: TextView
    private lateinit var muteAudioButton: FloatingActionButton
    private lateinit var moveCardButton: FloatingActionButton
    private lateinit var collectionSettingButton: FloatingActionButton
    private lateinit var showAllCardsButton: FloatingActionButton

    private lateinit var appPath:String
    private lateinit var flashcardPath:String

    var cardOrder = listOf<String>()
    var cardIndex: Int = 0

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToMainMenu()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        backButton = findViewById(R.id.b_back_flashcards)
        flashcardButton = findViewById(R.id.b_flashcard)
        collectionTitle = findViewById(R.id.collection_title)
        collectionIndex = findViewById(R.id.collection_index_name)
        nativeToForeignButton = findViewById(R.id.native_to_foreign)
        foreignToNativeButton = findViewById(R.id.foreign_to_native)
        addCardButton = findViewById(R.id.b_add_card_flashcards)
        editCardButton = findViewById(R.id.b_edit_flashcards)
        shuffleButton = findViewById(R.id.b_shuffle_flashcards)
        flashcardCounter = findViewById(R.id.flashcard_counter)
        flashcardId = findViewById(R.id.flashcard_id)
        muteAudioButton = findViewById(R.id.b_mute_flashcards)
        moveCardButton = findViewById(R.id.b_move_flashcards)
        collectionSettingButton = findViewById(R.id.b_settings_flashcards)
        showAllCardsButton = findViewById(R.id.b_show_flashcards)

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var nativeToForeignActive = true
        var foreignToNativeActive = false

        var flashcardShowsQuestion = false
        var audioMuted = sharedPref.getBoolean("audio_muted", false)

        val b = intent.extras
        val v:Int? = b?.getInt("collectionId")
        val collectionNumber:Int = v ?:0

        appPath = getExternalFilesDir(null).toString()
        flashcardPath = appPath + "/Cards"
        val collectionsPath = appPath + "/Collections"
        val collectionPath = collectionsPath + "/Collection_$collectionNumber"
        val propertiesPath = collectionPath + "/Properties.txt"

        if (MyUtils.readLineFromFile(propertiesPath, 4) != "") {
            cardIndex = MyUtils.readLineFromFile(propertiesPath, 4)!!.toInt()
        }

        //setup mute button
        if (audioMuted) {
            muteAudioButton.setImageResource(R.drawable.muted)
        } else {
            muteAudioButton.setImageResource(R.drawable.unmuted)
        }

        //read card order from file if exists, otherwise shuffle
        val orderLine = MyUtils.readLineFromFile(propertiesPath, 3)
        if (orderLine == "-") {
            cardOrder = shuffleCards(collectionPath, nativeToForeignActive, foreignToNativeActive)
            cardIndex = 0
        }   else {
            if (orderLine != null) {
                cardOrder = orderLine.split(" ")
            }   else {
                Log.e("HUGE ERROR", "orderLine is null")
            }
        }

        moveCardButton.isEnabled = false
        moveCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        editCardButton.isEnabled = false
        editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        flashcardCounter.text = cardOrder.size.toString() + " Cards"

        if (cardOrder.isEmpty()) {
            flashcardButton.isEnabled = false
            flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
            shuffleButton.isEnabled = false
            shuffleButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        }

        collectionTitle.text = MyUtils.readLineFromFile(propertiesPath, 0)
        collectionIndex.text = "Collection_$collectionNumber"

        nativeToForeignButton.text = MyUtils.readLineFromFile(propertiesPath, 1) + " - " + MyUtils.readLineFromFile(propertiesPath, 2)
        foreignToNativeButton.text = MyUtils.readLineFromFile(propertiesPath, 2) + " - " + MyUtils.readLineFromFile(propertiesPath, 1)
        foreignToNativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))

        flashcardButton.setOnClickListener{ //die reihenfolge kann nicht verändert werden während man in dieser activity ist!!!
            if (flashcardShowsQuestion) {
                flashcardShowsQuestion = false

                flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
                //flashcardButton.text = "backside"

                var line = 0
                var audioFile = "/native.mp3"
                if (cardOrder[cardIndex].first() == 'n') {
                    line = 1
                    audioFile = "/foreign.mp3"
                }

                if (!audioMuted) {
                    MyUtils.stopAudio()
                    MyUtils.playAudio(collectionPath + "/" + cardOrder[cardIndex].substring(2) + audioFile)
                }

                flashcardButton.text = MyUtils.readLineFromFile(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + "/Content.txt", line)
                flashcardId.text = cardOrder[cardIndex].substring(2)

                if (cardIndex+1 == cardOrder.size) {
                    cardIndex = 0
                }   else {
                    cardIndex++
                }

                MyUtils.writeTextFile(propertiesPath, 4, cardIndex.toString())

            } else{
                flashcardShowsQuestion = true

                flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                //flashcardButton.text = "frontside"
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

                flashcardButton.text = MyUtils.readLineFromFile(flashcardPath + "/" + cardOrder[cardIndex].substring(2) + "/Content.txt", line)
                flashcardId.text = cardOrder[cardIndex].substring(2)
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

            val cardString = flashcardButton.text.toString()

            val collectionIds = MyUtils.getFoldersInDirectory(collectionsPath)
            val collectionTupel = mutableListOf<MyUtils.SpinnerItem>()
            for (collectionId in collectionIds) {
                val collectionName = MyUtils.readLineFromFile(collectionsPath + "/" + collectionId + "/Properties.txt", 0)
                collectionName?.let { it1 -> MyUtils.SpinnerItem(it1, collectionId) }
                    ?.let { it2 -> collectionTupel.add(it2) }
            }

            MyUtils.showDropdownDialog(this,"Move Card: $cardString", "Chose the collection this card should be moved to", collectionTupel) { isConfirmed, selectedItem ->
                if (isConfirmed) {
                    if (selectedItem != null) {
                        MyUtils.moveCardToCollection(context=this, oldCollectionPath = collectionPath, newCollectionPath = collectionsPath + "/" + selectedItem.description, cardName = cardOrder[cardIndex].substring(2), pathOfTheFlashcard = flashcardPath + "/" + cardOrder[cardIndex].substring(2))
                        MyUtils.createShortToast(this, "Moved to collection: ${selectedItem.description}")
                        // Create an intent to restart the current activity
                        val intent = intent
                        // Optional: You can add flags to clear the activity stack if needed
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        // Finish the current activity
                        finish()
                        // Start the activity again
                        startActivity(intent)
                    }
                } else {
                    MyUtils.createShortToast(this, "Cancelled")
                }
            }
        }

        nativeToForeignButton.setOnClickListener {
            if (nativeToForeignActive) { //deactivate button
                nativeToForeignActive = false
                nativeToForeignButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))
                if (!foreignToNativeActive) {
                    foreignToNativeActive = true
                    foreignToNativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                }
            }   else {
                nativeToForeignActive = true
                nativeToForeignButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            }
        }

        foreignToNativeButton.setOnClickListener {
            if (foreignToNativeActive) { //deactivate button
                foreignToNativeActive = false
                foreignToNativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.button_color))
                if (!nativeToForeignActive) {
                    nativeToForeignActive = true
                    nativeToForeignButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                }
            }   else {
                foreignToNativeActive = true
                foreignToNativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            }
        }

        //back button
        backButton.setOnClickListener {
            backToMainMenu()
        }

        editCardButton.setOnClickListener {
            MyUtils.stopAudio()
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

            intent = Intent(this, AddCardActivity::class.java)
            val b = Bundle()
            b.putString("collectionPath", collectionPath)
            b.putInt("collectionId", collectionNumber)
            b.putBoolean("calledFromAddCard", false)
            b.putString("cardName", currentCard.substring(2))
            b.putBoolean("calledFromList", false)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        showAllCardsButton.setOnClickListener {
            MyUtils.stopAudio()
            intent = Intent(this, FlashcardListActivity::class.java)
            val b = Bundle()
            b.putString("flashcardPath", flashcardPath)
            b.putString("collectionPath", collectionPath)
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        addCardButton.setOnClickListener {
            MyUtils.stopAudio()
            intent = Intent(this, AddCardActivity::class.java)
            val b = Bundle()
            b.putString("collectionPath", collectionPath)
            b.putInt("collectionId", collectionNumber)
            b.putBoolean("calledFromAddCard", true)
            b.putBoolean("calledFromList", false)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        shuffleButton.setOnClickListener {
            MyUtils.stopAudio()
            MyUtils.showConfirmationDialog(this,"Shuffle Cards", "Are you sure you want to shuffle?") {userChoice ->
                if (userChoice) {
                    cardOrder = shuffleCards(collectionPath, nativeToForeignActive, foreignToNativeActive)
                    cardIndex = 0
                    MyUtils.writeTextFile(propertiesPath, 4, "0")
                    flashcardButton.text = "Start"
                    flashcardShowsQuestion = false
                    flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                    editCardButton.isEnabled = false
                    editCardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
                }
            }
        }

        collectionSettingButton.setOnClickListener {
            MyUtils.stopAudio()
            intent = Intent(this, CollectionSettingsActivity::class.java)
            val b = Bundle()
            b.putString("collectionPath", collectionPath)
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }
    }

    private fun backToMainMenu() {
        MyUtils.stopAudio()
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun shuffleCards(collectionPath: String, nativeToForeign: Boolean, foreignToNative: Boolean): List<String> {
        val cards = MyUtils.getCardFolderNames(this, collectionPath)
        Log.e("cards", cards.size.toString())
        val prefixes = listOf("n_", "f_")
        var shuffledCards = mutableListOf<String>()

        if (nativeToForeign && !foreignToNative) {
            val prefix = prefixes[0]
            for (card in cards) {
                shuffledCards.add("$prefix$card")
            }
        }  else if (foreignToNative && !nativeToForeign) {
            val prefix = prefixes[1]
            for (card in cards) {
                shuffledCards.add("$prefix$card")
            }
        }   else {
            for (card in cards) {
                val prefix = prefixes.random()
                shuffledCards.add("$prefix$card")
            }
        }

        shuffledCards.shuffle()

        if (shuffledCards.isNotEmpty()) {
            MyUtils.writeTextFile(collectionPath + "/Properties.txt", 3, shuffledCards.joinToString(" "))
        }   else {
            MyUtils.writeTextFile(collectionPath + "/Properties.txt", 3, "-")
        }

        //set the index
        MyUtils.writeTextFile(collectionPath + "/Properties.txt", 4, "0")

        return shuffledCards
    }
}