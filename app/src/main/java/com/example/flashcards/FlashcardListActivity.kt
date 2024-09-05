package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flashcards.databinding.ActivityMainBinding
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FlashcardListActivity : AppCompatActivity() {
    private lateinit var container: ViewGroup
    private lateinit var backButton: FloatingActionButton
    private lateinit var collectionNameHeader: TextView
    private lateinit var collectionIndexHeader: TextView

    private lateinit var buttonMoveCard: FloatingActionButton
    private lateinit var buttonEditCard: FloatingActionButton
    private lateinit var buttonAddCard: FloatingActionButton

    private var collectionDisplayHeight = 40

    override fun onCreate(savedInstanceState: Bundle?) {
        //boilerplate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_list)

        container = findViewById(R.id.container)
        backButton = findViewById(R.id.view_flashcards_back_button)
        collectionNameHeader = findViewById(R.id.show_flashcards_header)
        collectionIndexHeader = findViewById(R.id.collection_index_name)

        buttonMoveCard = findViewById(R.id.b_move_card)
        buttonEditCard = findViewById(R.id.b_edit_card)
        buttonAddCard = findViewById(R.id.b_add_card)

        val b = intent.extras
        val v:String? = b?.getString("collectionPath")
        val collectionPath:String = v ?:""
        val i:Int? = b?.getInt("collectionId")
        val collectionNumber:Int = i ?:0


        //scan for flashcards and add them to the list view
        val cards = MyUtils.getFoldersInDirectory(collectionPath)

        for (card in cards) {
            val frontSide:String = MyUtils.readLineFromFile(collectionPath + "/" + card + "/Content.txt", 0).toString()
            val backSide:String = MyUtils.readLineFromFile(collectionPath + "/" + card + "/Content.txt", 1).toString()
            addFlashcardButton(frontSide, backSide)


        }

        buttonMoveCard.setOnClickListener {
            TODO()
        }

        buttonEditCard.setOnClickListener {
            TODO()
        }

        buttonAddCard.setOnClickListener {
            TODO()
        }

        backButton.setOnClickListener {
            intent = Intent(this, TrainingActivity::class.java)
            val b = Bundle()
            b.putInt("collectionId", collectionNumber)
            intent.putExtras(b)
            startActivity(intent)
            finish()
        }
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun addFlashcardButton(frontSide: String, backSide: String) {

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

        // Create the left button
        val collectionButton = Button(this).apply {
            text = frontSide + backSide
            isAllCaps = false

            setTextColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.white))
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                (330).dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(20, 0, 0, 4)
            }

            // Create the stroke drawable with transparent center
            val strokeDrawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.transparent)) // Transparent center
                setStroke(40.dpToPx(), ContextCompat.getColor(this@FlashcardListActivity, R.color.white)) // Stroke width and color
            }

            // Get the existing drawable resource
            val existingDrawable = ContextCompat.getDrawable(this@FlashcardListActivity, R.drawable.split_button)?.mutate()

            // Combine stroke and existing drawable using LayerDrawable
            val layerDrawable = LayerDrawable(arrayOf(strokeDrawable, existingDrawable))

            // Set the combined drawable as the background
            background = layerDrawable

            setOnClickListener {
                this.setBackgroundColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.delete_highlight))
            }
        }

        // Add buttons to the horizontal layout
        rowLayout.addView(collectionButton)

        // Add the horizontal layout to the container
        container.addView(rowLayout)
    }
}