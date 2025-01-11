package com.example.flashcards

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MyFloatingActionButton(context: Context, attrs: AttributeSet) : FloatingActionButton(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        // Add custom click handling here if necessary
        return true
    }
}

class AddCardActivity<IOException> : AppCompatActivity() {

    private lateinit var confirmCardButton: FloatingActionButton
    private lateinit var frontsideTextbox: EditText
    private lateinit var backsideTextbox: EditText
    private lateinit var frontRecordButton: MyFloatingActionButton
    private lateinit var frontPlayButton: FloatingActionButton
    private lateinit var frontDeleteButton: FloatingActionButton
    private lateinit var backRecordButton: MyFloatingActionButton
    private lateinit var backPlayButton: FloatingActionButton
    private lateinit var backDeleteButton: FloatingActionButton
    private lateinit var closeButton: FloatingActionButton
    private lateinit var addAnotherCardButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var header: TextView
    private lateinit var cardId: TextView
    private lateinit var cardCount: TextView
    private lateinit var actionBar: LinearLayout
    private lateinit var frontSideText: TextView
    private lateinit var backSideText: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var isRecording = false

    var pathOfTheCurrentFlashcard:String = ""

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        confirmCardButton = findViewById(R.id.button_finish_card)
        frontsideTextbox = findViewById(R.id.enter_front)
        backsideTextbox = findViewById(R.id.enter_back)
        frontRecordButton = findViewById(R.id.b_record_front_audio)
        frontPlayButton = findViewById(R.id.b_play_front_audio)
        frontDeleteButton = findViewById(R.id.b_delete_front_audio)
        backRecordButton = findViewById(R.id.b_record_back_audio)
        backPlayButton = findViewById(R.id.b_play_back_audio)
        backDeleteButton = findViewById(R.id.b_delete_back_audio)
        closeButton = findViewById(R.id.b_close_add_card)
        addAnotherCardButton = findViewById(R.id.button_add_card)
        deleteButton = findViewById(R.id.button_delete_card)
        header = findViewById(R.id.add_card_header)
        cardId = findViewById(R.id.card_id)
        cardCount = findViewById(R.id.card_count)
        actionBar = findViewById(R.id.button_area)
        frontSideText = findViewById(R.id.front_text)
        backSideText = findViewById(R.id.back_text)

        // Request necessary permissions
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                ),
                REQUEST_PERMISSION_CODE
            )
        }

        val b = intent.extras
        val v:String? = b?.getString("collectionPath")
        val collectionPath:String = v ?:""
        val i:String? = b?.getString("collectionId")
        val nameOfCurrentCollection:String = i ?:""
        val a:Boolean? = b?.getBoolean("calledFromAddCard")
        val calledFromAddCard:Boolean = a ?:false
        val g:Boolean? = b?.getBoolean("calledFromList")
        val calledFromList:Boolean = g ?:false

        val f:String? = b?.getString("mode")
        val mode = f ?:"n"
        val c:String? = b?.getString("scheduledCollections")
        val scheduledCollectionsString = c ?:""

        val propertiesPath = collectionPath + "/Properties.txt"
        var cardName:String

        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val currentTabIndex = sharedPref.getString("currentTabIndex", "ERROR")
        val tabPath = getExternalFilesDir(null).toString() + "/" + currentTabIndex
        val flashcardPath = tabPath + "/Cards"

        //set the language labels
        frontSideText.text = MyUtils.readLineFromFile(propertiesPath, 1)
        backSideText.text = MyUtils.readLineFromFile(propertiesPath, 2)

        if (mode != "r") {
            cardCount.text = "${MyUtils.getCardCountForCollection(collectionPath)} Card(s)"
        } else {
            cardCount.text = "${MyUtils.getCardCountForCollection(tabPath + "/Repetition_Properties.txt", true)} Card(s)"
        }

        //logic for textboxes to allow multiline text but dont go beyond 8 lines
        setupMultilineTextbox(frontsideTextbox, 8)
        setupMultilineTextbox(backsideTextbox, 8)

        //prepare the page for adding a new card
        if (calledFromAddCard) {
            actionBar.removeView(deleteButton)

            pathOfTheCurrentFlashcard = createCard(flashcardPath= flashcardPath, collectionName=nameOfCurrentCollection, collectionPath=collectionPath, tabPath = tabPath)

            cardName = pathOfTheCurrentFlashcard.removePrefix(flashcardPath+"/")

            //grey out audio buttons
            frontPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            backPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))

        //prepare the page for editing an existing card
        }   else {
            actionBar.removeView(addAnotherCardButton)

            closeButton.visibility = View.INVISIBLE
            closeButton.isClickable = false
            header.text = "Edit Card"

            val cc:String? = b?.getString("cardName")
            cardName = cc ?:""

            //set card id text
            cardId.text = nameOfCurrentCollection + " (" + '"' + MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0) +'"'+ ")" + " - Card_#" + cardName.substring(5)

            pathOfTheCurrentFlashcard = "$flashcardPath/$cardName"

            frontsideTextbox.setText(MyUtils.readLineFromFile(pathOfTheCurrentFlashcard + "/Content.txt", 0)!!.replace("$","\n"))
            backsideTextbox.setText(MyUtils.readLineFromFile(pathOfTheCurrentFlashcard + "/Content.txt", 1)!!.replace("$","\n"))

            if (!MyUtils.fileExists(pathOfTheCurrentFlashcard + "/native.mp3")) {
                frontPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
            if (!MyUtils.fileExists(pathOfTheCurrentFlashcard + "/foreign.mp3")) {
                backPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
        }

        closeButton.setOnClickListener { //only visible when adding a new card
            abortCard(pathOfTheCurrentFlashcard)

            if (calledFromList) {
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
            } else {
                intent = Intent(this, TrainingActivity::class.java)
                val bun = Bundle()
                bun.putString("collectionId", nameOfCurrentCollection)
                bun.putString("mode", mode)
                bun.putString("scheduledCollections", scheduledCollectionsString)
                intent.putExtras(bun)
                startActivity(intent)
                finish()
            }
        }

        deleteButton.setOnClickListener { //only visble when editing a card
            //removes it from the flashcards and properties file
            MyUtils.removeCardFromCollection(this, collectionPath, cardName)

            //delete the folder of the card
            abortCard(pathOfTheCurrentFlashcard)

            if (calledFromList) {
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
            } else {
                intent = Intent(this, TrainingActivity::class.java)
                val bun = Bundle()
                bun.putString("collectionId", nameOfCurrentCollection)
                bun.putString("mode", mode)
                bun.putString("scheduledCollections", scheduledCollectionsString)
                intent.putExtras(bun)
                startActivity(intent)
                finish()
            }
        }

        confirmCardButton.setOnClickListener { //visble when adding a new card AND editing an old one
            if (confirmCard(creatingNewCard = calledFromAddCard,collectionPath= collectionPath, flashcardName = cardName, nativeLanguagePrompt = frontsideTextbox.text.toString(), foreignLanguagePrompt = backsideTextbox.text.toString()))   {
                Log.e("output", backsideTextbox.text.toString())
                if (calledFromList) {
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
                } else {
                    intent = Intent(this, TrainingActivity::class.java)
                    val bun = Bundle()
                    bun.putString("collectionId", nameOfCurrentCollection)
                    bun.putString("mode", mode)
                    bun.putString("scheduledCollections", scheduledCollectionsString)
                    intent.putExtras(bun)
                    startActivity(intent)
                    finish()
                }
            }   else {
                MyUtils.createShortToast(this,"front or back side cannot be empty")
            }
        }

        addAnotherCardButton.setOnClickListener { //only visible when adding a new card
            if (confirmCard(creatingNewCard = true, collectionPath=collectionPath, flashcardName = cardName, nativeLanguagePrompt = frontsideTextbox.text.toString(), foreignLanguagePrompt = backsideTextbox.text.toString()))   {
                frontsideTextbox.text.clear()
                backsideTextbox.text.clear()

                pathOfTheCurrentFlashcard = createCard(flashcardPath, collectionName=nameOfCurrentCollection, collectionPath=collectionPath, tabPath = tabPath)
                cardName = pathOfTheCurrentFlashcard.removePrefix(flashcardPath+"/")

                //update card count
                cardCount.text = "${MyUtils.getCardCountForCollection(collectionPath)} Card(s)"

                frontPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
                backPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }   else {
                MyUtils.createShortToast(this,"front or back side cannot be empty")
            }

        }

        frontRecordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(pathOfTheCurrentFlashcard, "native", frontRecordButton)
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording(frontRecordButton)
                    frontPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.primary))
                    view.performClick()
                }
            }
            true
        }

        backRecordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(pathOfTheCurrentFlashcard, "foreign", backRecordButton)
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording(backRecordButton)
                    backPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.primary))
                    view.performClick()
                }
            }
            true
        }

        frontPlayButton.setOnClickListener {
            if (!MyUtils.playAudio("$pathOfTheCurrentFlashcard/native.mp3")) {
                MyUtils.createShortToast(this, "Front audio does not exist")
            }
        }

        backPlayButton.setOnClickListener {
            if (!MyUtils.playAudio("$pathOfTheCurrentFlashcard/foreign.mp3")) {
                MyUtils.createShortToast(this, "Back audio does not exist")
            }
        }

        frontDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$pathOfTheCurrentFlashcard/native.mp3", "Audio deleted successfully")
            frontPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }

        backDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$pathOfTheCurrentFlashcard/foreign.mp3", "Audio deleted successfully")
            backPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }
    }

    private fun setupMultilineTextbox(textBox:EditText, maxLines:Int) {
        textBox.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val newText = dest.subSequence(0, dstart).toString() +
                    source.subSequence(start, end).toString() +
                    dest.subSequence(dend, dest.length).toString()

            if (newText.lines().size > maxLines) {
                ""
            } else {
                null
            }
        })
        textBox.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {
                if (textBox.lineCount >= maxLines) {
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createCard(flashcardPath: String, showMessage:Boolean =false, collectionName:String, collectionPath:String, tabPath:String):String{
        val tabSettingsPath = tabPath + "/Settings.txt"

        var flashcardIndexOfNextCard = MyUtils.readLineFromFile(tabSettingsPath, 5)!!.toInt()
        cardId.text = collectionName + " (" + '"' + MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0) +'"'+ ")" + " - Card_#$flashcardIndexOfNextCard"

        val pathOfCreatedFlashcard = "$flashcardPath/Card_$flashcardIndexOfNextCard"

        MyUtils.createFolder(this, flashcardPath, "Card_$flashcardIndexOfNextCard", "Flashcard created successfully", showMessage)
        MyUtils.createTextFile(pathOfCreatedFlashcard, "Content.txt")

        flashcardIndexOfNextCard++
        MyUtils.writeTextFile(tabSettingsPath, 5, flashcardIndexOfNextCard.toString())

        return pathOfCreatedFlashcard
    }

    private fun confirmCard(creatingNewCard: Boolean, collectionPath: String, flashcardName: String, nativeLanguagePrompt: String, foreignLanguagePrompt: String):Boolean {
        if (nativeLanguagePrompt.isNullOrEmpty() || foreignLanguagePrompt.isNullOrEmpty()) {
            return false
        } else {
            MyUtils.writeTextFile(pathOfTheCurrentFlashcard + "/Content.txt", 0, nativeLanguagePrompt.replace("\n", "$"))
            MyUtils.writeTextFile(pathOfTheCurrentFlashcard + "/Content.txt", 1, foreignLanguagePrompt.replace("\n", "$"))

            //only when it is a new card it should be added to the orderline and the flashcards file and the collection reference should be added to the card
            if (creatingNewCard) {
                MyUtils.addCardToCollectionAndReferenceCollection(collectionPath, flashcardName, pathOfTheCurrentFlashcard, true)
            }

            MyUtils.createShortToast(this, "Card saved successfully")
            return true
        }
    }

    private fun abortCard(flashcardPath: String) {
        MyUtils.deleteFolder(this, flashcardPath, "Flashcard aborted successfully")
    }

    private fun hasPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun startRecording(storageDir: String, name:String, recordButton: MyFloatingActionButton) {
        if (isRecording) return
        recordButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.recording))

        // Create file to save recording
        recordFile = File(storageDir, "${name}.mp3")

        // Initialize MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)  // Set high-quality sampling rate
            setAudioEncodingBitRate(128000)  // Set high-quality encoding bit rate
            setOutputFile(recordFile!!.absolutePath)

            try {
                prepare()
                start()
                isRecording = true
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording(recordButton: MyFloatingActionButton) {
        if (!isRecording) return

        // Add a small delay before stopping the recorder
        recordButton.postDelayed({
            // Stop recording
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                recordButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.primary))
            }
            isRecording = false
            mediaRecorder = null
        }, 300) // 200 milliseconds delay
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 100
    }
}