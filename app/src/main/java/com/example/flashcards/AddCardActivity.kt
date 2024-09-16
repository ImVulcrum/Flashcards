package com.example.flashcards

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
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
    private lateinit var nativeLanguageTexbox: EditText
    private lateinit var foreignLanguageTextbox: EditText
    private lateinit var nativeRecordButton: MyFloatingActionButton
    private lateinit var nativePlayButton: FloatingActionButton
    private lateinit var nativeDeleteButton: FloatingActionButton
    private lateinit var foreignRecordButton: MyFloatingActionButton
    private lateinit var foreignPlayButton: FloatingActionButton
    private lateinit var foreignDeleteButton: FloatingActionButton
    private lateinit var closeButton: FloatingActionButton
    private lateinit var addAnotherCardButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var header: TextView
    private lateinit var cardId: TextView
    private lateinit var actionBar: LinearLayout
    private lateinit var nativeLanguageText: TextView
    private lateinit var foreignLanguageText: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var isRecording = false

    var pathOfTheCurrentFlashcard:String = ""

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        confirmCardButton = findViewById(R.id.button_finish_card)
        nativeLanguageTexbox = findViewById(R.id.enter_native)
        foreignLanguageTextbox = findViewById(R.id.enter_collection_name)
        nativeRecordButton = findViewById(R.id.b_record_native_audio)
        nativePlayButton = findViewById(R.id.b_play_native_audio)
        nativeDeleteButton = findViewById(R.id.b_delete_native_audio)
        foreignRecordButton = findViewById(R.id.b_record_foreign_audio)
        foreignPlayButton = findViewById(R.id.b_play_foreign_audio)
        foreignDeleteButton = findViewById(R.id.b_delete_foreign_audio)
        closeButton = findViewById(R.id.b_close_add_card)
        addAnotherCardButton = findViewById(R.id.button_add_card)
        deleteButton = findViewById(R.id.button_delete_card)
        header = findViewById(R.id.add_card_header)
        cardId = findViewById(R.id.card_id)
        actionBar = findViewById(R.id.button_area)
        nativeLanguageText = findViewById(R.id.native_language)
        foreignLanguageText = findViewById(R.id.foreign_language)

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
        val i:Int? = b?.getInt("collectionId")
        val collectionNumber:Int = i ?:0
        val a:Boolean? = b?.getBoolean("calledFromAddCard")
        val calledFromAddCard:Boolean = a ?:false
        val g:Boolean? = b?.getBoolean("calledFromList")
        val calledFromList:Boolean = g ?:false

        var cardName:String = ""
        val flashcardPath = getExternalFilesDir(null).toString() + "/Cards"
        val propertiesPath = collectionPath + "/Properties.txt"

        //set the language labels
        nativeLanguageText.text = MyUtils.readLineFromFile(propertiesPath, 1)
        foreignLanguageText.text = MyUtils.readLineFromFile(propertiesPath, 2)

        //prepare the page for adding a new card
        if (calledFromAddCard) {
            actionBar.removeView(deleteButton)

            pathOfTheCurrentFlashcard = createCard(flashcardPath= flashcardPath, collectionNumber=collectionNumber, collectionPath=collectionPath)

            cardName = pathOfTheCurrentFlashcard.removePrefix(flashcardPath+"/")

            //grey out audio buttons
            nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))

        //prepare the page for editing an existing card
        }   else {
            actionBar.removeView(addAnotherCardButton)

            closeButton.visibility = View.INVISIBLE
            closeButton.isClickable = false
            header.text = "Edit Card"

            val c:String? = b?.getString("cardName")
            cardName = c ?:""

            //set card id text
            cardId.text = "Collection_" + collectionNumber.toString() + " (" + '"' + MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0) +'"'+ ")" + " - Card_#" + cardName.substring(5)

            pathOfTheCurrentFlashcard = "$flashcardPath/$cardName"

            nativeLanguageTexbox.setText(MyUtils.readLineFromFile(pathOfTheCurrentFlashcard + "/Content.txt", 0))
            foreignLanguageTextbox.setText(MyUtils.readLineFromFile(pathOfTheCurrentFlashcard + "/Content.txt", 1))

            if (!MyUtils.fileExists(pathOfTheCurrentFlashcard + "/native.mp3")) {
                nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
            if (!MyUtils.fileExists(pathOfTheCurrentFlashcard + "/foreign.mp3")) {
                foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
        }

        closeButton.setOnClickListener { //only visible when adding a new card
            abortCard(pathOfTheCurrentFlashcard)

            if (calledFromList) {
                intent = Intent(this, FlashcardListActivity::class.java)
                val bu = Bundle()
                bu.putString("collectionPath", collectionPath)
                bu.putInt("collectionId", collectionNumber)
                intent.putExtras(bu)
                startActivity(intent)
                finish()
            } else {
                intent = Intent(this, TrainingActivity::class.java)
                val b = Bundle()
                b.putInt("collectionId", collectionNumber)
                intent.putExtras(b)
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
                bu.putInt("collectionId", collectionNumber)
                intent.putExtras(bu)
                startActivity(intent)
                finish()
            } else {
                intent = Intent(this, TrainingActivity::class.java)
                val b = Bundle()
                b.putInt("collectionId", collectionNumber)
                intent.putExtras(b)
                startActivity(intent)
                finish()
            }
        }

        confirmCardButton.setOnClickListener { //visble when adding a new card AND editing an old one
            if (confirmCard(creatingNewCard = calledFromAddCard,collectionPath= collectionPath, flashcardName = cardName, nativeLanguagePrompt = nativeLanguageTexbox.text.toString(), foreignLanguagePrompt = foreignLanguageTextbox.text.toString()))   {
                if (calledFromList) {
                    intent = Intent(this, FlashcardListActivity::class.java)
                    val bu = Bundle()
                    bu.putString("flashcardPath", flashcardPath)
                    bu.putString("collectionPath", collectionPath)
                    bu.putInt("collectionId", collectionNumber)
                    intent.putExtras(bu)
                    startActivity(intent)
                    finish()
                } else {
                    intent = Intent(this, TrainingActivity::class.java)
                    val bun = Bundle()
                    bun.putInt("collectionId", collectionNumber)
                    intent.putExtras(bun)
                    startActivity(intent)
                    finish()
                }
            }   else {
                MyUtils.createShortToast(this,"native or foreign language cannot be empty")
            }
        }

        addAnotherCardButton.setOnClickListener { //only visible when adding a new card
            if (confirmCard(creatingNewCard = true, collectionPath=collectionPath, flashcardName = cardName, nativeLanguagePrompt = nativeLanguageTexbox.text.toString(), foreignLanguagePrompt = foreignLanguageTextbox.text.toString()))   {
                nativeLanguageTexbox.text.clear()
                foreignLanguageTextbox.text.clear()

                pathOfTheCurrentFlashcard = createCard(flashcardPath, collectionNumber=collectionNumber, collectionPath=collectionPath)
                cardName = pathOfTheCurrentFlashcard.removePrefix(flashcardPath+"/")

                nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
                foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }   else {
                MyUtils.createShortToast(this,"native or foreign language cannot be empty")
            }

        }

        nativeRecordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(pathOfTheCurrentFlashcard, "native", nativeRecordButton)
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording(nativeRecordButton)
                    nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.primary))
                    view.performClick()
                }
            }
            true
        }

        foreignRecordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(pathOfTheCurrentFlashcard, "foreign", foreignRecordButton)
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording(foreignRecordButton)
                    foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.primary))
                    view.performClick()
                }
            }
            true
        }

        nativePlayButton.setOnClickListener {
            if (!MyUtils.playAudio("$pathOfTheCurrentFlashcard/native.mp3")) {
                MyUtils.createShortToast(this, "Native audio does not exist")
            }
        }

        foreignPlayButton.setOnClickListener {
            if (!MyUtils.playAudio("$pathOfTheCurrentFlashcard/foreign.mp3")) {
                MyUtils.createShortToast(this, "Foreign audio does not exist")
            }
        }

        nativeDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$pathOfTheCurrentFlashcard/native.mp3", "Audio deleted successfully")
            nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }

        foreignDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$pathOfTheCurrentFlashcard/foreign.mp3", "Audio deleted successfully")
            foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }
    }

    private fun createCard(flashcardPath: String, showMessage:Boolean =false, collectionNumber:Int, collectionPath:String):String{
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var flashcardIndexOfNextCard = sharedPref.getInt(collectionPath, 0)
        cardId.text = "Collection_" + collectionNumber.toString() + " (" + '"' + MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0) +'"'+ ")" + " - Card_#$flashcardIndexOfNextCard"

        val pathOfCreatedFlashcard = "$flashcardPath/Flashcard_$flashcardIndexOfNextCard"

        MyUtils.createFolder(this, flashcardPath, "Flashcard_$flashcardIndexOfNextCard", "Flashcard created successfully", showMessage)
        MyUtils.createTextFile(pathOfCreatedFlashcard, "Content.txt")

        flashcardIndexOfNextCard++
        editor.putInt(collectionPath, flashcardIndexOfNextCard)
        editor.apply()

        return pathOfCreatedFlashcard
    }

    private fun confirmCard(creatingNewCard: Boolean, collectionPath: String, flashcardName: String, nativeLanguagePrompt: String, foreignLanguagePrompt: String):Boolean {
        if (nativeLanguagePrompt.isNullOrEmpty() || foreignLanguagePrompt.isNullOrEmpty()) {
            return false
        } else {
            MyUtils.writeTextFile(pathOfTheCurrentFlashcard + "/Content.txt", 0, nativeLanguagePrompt)
            MyUtils.writeTextFile(pathOfTheCurrentFlashcard + "/Content.txt", 1, foreignLanguagePrompt)

            //only when it is a new card it should be added to the orderline and the flashcards file and the collection reference should be added to the card
            if (creatingNewCard) {
                MyUtils.addCardToCollectionAndReferenceCollection(collectionPath, flashcardName, pathOfTheCurrentFlashcard)
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