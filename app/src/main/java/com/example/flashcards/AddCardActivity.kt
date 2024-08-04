package com.example.flashcards

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import android.Manifest
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

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
    private lateinit var foreignRecordButton: MyFloatingActionButton
    private lateinit var foreignPlayButton: FloatingActionButton
    private lateinit var closeButton: FloatingActionButton
    private lateinit var addAnotherCardButton: FloatingActionButton
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var header: TextView
    private lateinit var card_id: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var isRecording = false

    var flashcardPath:String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        confirmCardButton = findViewById(R.id.button_finish_card)
        nativeLanguageTexbox = findViewById(R.id.enter_native)
        foreignLanguageTextbox = findViewById(R.id.enter_foreign)
        nativeRecordButton = findViewById(R.id.b_record_native_audio)
        nativePlayButton = findViewById(R.id.b_play_native_audio)
        foreignRecordButton = findViewById(R.id.b_record_foreign_audio)
        foreignPlayButton = findViewById(R.id.b_play_foreign_audio)
        closeButton = findViewById(R.id.b_close_add_card)
        addAnotherCardButton = findViewById(R.id.button_add_card)
        deleteButton = findViewById(R.id.button_delete_card)
        header = findViewById(R.id.add_card_header)
        card_id = findViewById(R.id.card_id)

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

        if (calledFromAddCard) {
            deleteButton.visibility = View.INVISIBLE
            deleteButton.isClickable = false
        }   else {
            addAnotherCardButton.visibility = View.INVISIBLE
            addAnotherCardButton.isClickable = false
            closeButton.visibility = View.INVISIBLE
            closeButton.isClickable = false
            header.text = "Edit Card"
        }

        flashcardPath = createCard(collectionPath)

        closeButton.setOnClickListener {
            abortCard(flashcardPath)
            intent = Intent(this, TrainingActivity::class.java)
            val b = Bundle()
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        confirmCardButton.setOnClickListener {
            confirmCard(flashcardPath, nativeLanguageTexbox.text.toString(), foreignLanguageTextbox.text.toString())
            intent = Intent(this, TrainingActivity::class.java)
            val b = Bundle()
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }

        addAnotherCardButton.setOnClickListener {
            Log.e("§", nativeLanguageTexbox.text.toString())
            if (confirmCard(flashcardPath, nativeLanguageTexbox.text.toString(), foreignLanguageTextbox.text.toString()))   {
                nativeLanguageTexbox.text.clear()
                foreignLanguageTextbox.text.clear()

                flashcardPath = createCard(collectionPath)
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
                    view.performClick()
                }
            }
            true
        }

        nativePlayButton.setOnClickListener {
            MyUtils.playAudio("$flashcardPath/native.mp3")
        }

        foreignPlayButton.setOnClickListener {
            MyUtils.playAudio("$flashcardPath/foreign.mp3")
        }
    }

    private fun createCard(collectionPath: String):String{
        val sharedPref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        var flashcardCount = sharedPref.getInt(collectionPath, 0)
        card_id.text = "Card #$flashcardCount"



        val flashcardPath = "$collectionPath/Flashcard_$flashcardCount"

        MyUtils.createFolder(this, collectionPath, "Flashcard_$flashcardCount", "Flashcard created successfully")
        MyUtils.createTextFile(flashcardPath, "Content.txt")

        flashcardCount++
        editor.putInt(collectionPath, flashcardCount)
        editor.apply()

        return flashcardPath
    }

    private fun confirmCard(flashcardPath: String, nativeLanguagePrompt: String, foreignLanguagePrompt: String):Boolean {
        if (nativeLanguagePrompt.isNullOrEmpty() || foreignLanguagePrompt.isNullOrEmpty()) {
            return false
        } else {
            MyUtils.writeTextFile(flashcardPath + "/Content.txt", 0, nativeLanguagePrompt)
            MyUtils.writeTextFile(flashcardPath + "/Content.txt", 1, foreignLanguagePrompt)
            return true
        }
    }

    private fun abortCard(flashcardPath: String) {
        MyUtils.deleteFolder(this, flashcardPath, "Flashcard deleted successfully")
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