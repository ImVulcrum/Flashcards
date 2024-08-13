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

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var isRecording = false

    var flashcardPath:String = ""

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

        var cardName:String = ""

        if (calledFromAddCard) {
            actionBar.removeView(deleteButton)

            flashcardPath = createCard(collectionPath)

            //grey out audio buttons
            nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }   else {
            actionBar.removeView(addAnotherCardButton)

            closeButton.visibility = View.INVISIBLE
            closeButton.isClickable = false
            header.text = "Edit Card"

            val c:String? = b?.getString("cardName")
            cardName = c ?:""

            flashcardPath = "$collectionPath/$cardName"

            nativeLanguageTexbox.setText(MyUtils.readLineFromFile(flashcardPath + "/Content.txt", 0))
            foreignLanguageTextbox.setText(MyUtils.readLineFromFile(flashcardPath + "/Content.txt", 1))

            if (!MyUtils.fileExists(flashcardPath + "/native.mp3")) {
                nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
            if (!MyUtils.fileExists(flashcardPath + "/foreign.mp3")) {
                foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }
        }

        closeButton.setOnClickListener {
            abortCard(flashcardPath)
            intent = Intent(this, TrainingActivity::class.java)
            val b = Bundle()
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        deleteButton.setOnClickListener {
            val cardOrder = MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 3)
            if (cardOrder == "-") {
                //do nothing
            } else {
                if (cardOrder != null) {
                    val cards: MutableList<String>
                    cards = cardOrder.split(" ").toMutableList()

                    cards.remove("n_" + cardName)
                    cards.remove("f_" + cardName)

                    if (cards.isNotEmpty()) {
                        MyUtils.writeTextFile(collectionPath + "/Properties.txt", 3, cards.joinToString(" "))

                        //set the index logic
                        var cardIndex = MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 4)?.toInt()
                        if (cardIndex != null) {
                            cardIndex = cardIndex -1
                        }

                        if (cardIndex != null) {
                            if (cardIndex < 0) {
                                cardIndex = 0
                            }
                        }
                        MyUtils.writeTextFile(collectionPath + "/Properties.txt", 4, cardIndex.toString())

                    }   else {
                        MyUtils.writeTextFile(collectionPath + "/Properties.txt", 3, "-")
                        MyUtils.writeTextFile(collectionPath + "/Properties.txt", 4, "0")
                    }
                } else {
                    Log.e("HUGE ERROR", "orderLine is null")
                }
            }

            abortCard(flashcardPath)
            intent = Intent(this, TrainingActivity::class.java)
            val b = Bundle()
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        confirmCardButton.setOnClickListener {
            if (confirmCard(cardName, collectionPath,flashcardPath, nativeLanguageTexbox.text.toString(), foreignLanguageTextbox.text.toString()))   {
                intent = Intent(this, TrainingActivity::class.java)
                val b = Bundle()
                b.putInt("collectionId", collectionNumber)
                intent.putExtras(b)
                startActivity(intent)
                finish()
            }   else {
                MyUtils.createShortToast(this,"native or foreign language cannot be empty")
            }
        }

        addAnotherCardButton.setOnClickListener {
            if (confirmCard("", collectionPath, flashcardPath, nativeLanguageTexbox.text.toString(), foreignLanguageTextbox.text.toString()))   {
                nativeLanguageTexbox.text.clear()
                foreignLanguageTextbox.text.clear()

                flashcardPath = createCard(collectionPath)

                nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
                foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
            }   else {
                MyUtils.createShortToast(this,"native or foreign language cannot be empty")
            }

        }

        nativeRecordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(flashcardPath, "native", nativeRecordButton)
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
                    startRecording(flashcardPath, "foreign", foreignRecordButton)
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
            if (!MyUtils.playAudio("$flashcardPath/native.mp3")) {
                MyUtils.createShortToast(this, "Native audio does not exist")
            }
        }

        foreignPlayButton.setOnClickListener {
            if (!MyUtils.playAudio("$flashcardPath/foreign.mp3")) {
                MyUtils.createShortToast(this, "Foreign audio does not exist")
            }
        }

        nativeDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$flashcardPath/native.mp3", "Audio deleted successfully")
            nativePlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }

        foreignDeleteButton.setOnClickListener {
            MyUtils.deleteFile(this, "$flashcardPath/foreign.mp3", "Audio deleted successfully")
            foreignPlayButton.backgroundTintList = (ContextCompat.getColorStateList(this@AddCardActivity, R.color.button_color))
        }
    }

    private fun createCard(collectionPath: String, showMessage:Boolean =false):String{
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var flashcardCount = sharedPref.getInt(collectionPath, 0)
        cardId.text = "Card_#$flashcardCount"



        val flashcardPath = "$collectionPath/Flashcard_$flashcardCount"

        MyUtils.createFolder(this, collectionPath, "Flashcard_$flashcardCount", "Flashcard created successfully", showMessage)
        MyUtils.createTextFile(flashcardPath, "Content.txt")

        flashcardCount++
        editor.putInt(collectionPath, flashcardCount)
        editor.apply()

        return flashcardPath
    }

    private fun confirmCard(cardName: String, collectionPath: String, flashcardPath: String, nativeLanguagePrompt: String, foreignLanguagePrompt: String):Boolean {
        if (nativeLanguagePrompt.isNullOrEmpty() || foreignLanguagePrompt.isNullOrEmpty()) {
            return false
        } else {
            val flashcardId = flashcardPath.removePrefix(collectionPath+"/")

            MyUtils.writeTextFile(flashcardPath + "/Content.txt", 0, nativeLanguagePrompt)
            MyUtils.writeTextFile(flashcardPath + "/Content.txt", 1, foreignLanguagePrompt)

            if (cardName == "") {
                var cardOrder = MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 3)
                var space = " "
                if (cardOrder == "-") {
                    cardOrder = ""
                    space = ""
                }
                MyUtils.writeTextFile(collectionPath + "/Properties.txt", 3, cardOrder + space + "n_" + flashcardId)
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