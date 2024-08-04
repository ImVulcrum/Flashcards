package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.flashcards.utils.MyUtils

class TrainingActivity: AppCompatActivity() {
    private lateinit var backButton: FloatingActionButton
    private lateinit var flashcardButton: Button
    private lateinit var collectionTitle: TextView
    private lateinit var collectionIndex: TextView
    private lateinit var baseToForeignButton: Button
    private lateinit var foreignToBaseButton: Button
    private lateinit var addCardButton: FloatingActionButton

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        backButton = findViewById(R.id.b_back_flashcards)
        flashcardButton = findViewById(R.id.b_flashcard)
        collectionTitle = findViewById(R.id.collection_title)
        collectionIndex = findViewById(R.id.collection_index_name)
        baseToForeignButton = findViewById(R.id.base_to_foreign)
        foreignToBaseButton = findViewById(R.id.foreign_to_base)
        addCardButton = findViewById(R.id.b_add_card_flashcards)

        var firstClick = false
        val b = intent.extras
        val v:Int? = b?.getInt("key")
        val collectionNumber:Int = v ?:0

        val collectionPath = getExternalFilesDir(null).toString() + "/Collection_$collectionNumber"
        val propertiesPath = collectionPath + "/Properties.txt"

        collectionTitle.text = MyUtils.readLineFromFile(propertiesPath, 0)
        collectionIndex.text = "Collection_$collectionNumber"

        baseToForeignButton.text = MyUtils.readLineFromFile(propertiesPath, 1) + " - " + MyUtils.readLineFromFile(propertiesPath, 2)
        foreignToBaseButton.text = MyUtils.readLineFromFile(propertiesPath, 2) + " - " + MyUtils.readLineFromFile(propertiesPath, 1)

        flashcardButton.setOnClickListener{
            if (firstClick) {
                firstClick = false

                flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
                flashcardButton.text = "backside"

            } else{
                firstClick = true

                flashcardButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            }
        }

        //back button
        backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        addCardButton.setOnClickListener {
            intent = Intent(this, AddCardActivity::class.java)
            startActivity(intent)
        }


    }
}