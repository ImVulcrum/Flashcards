package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
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

    var currentCard:String = ""
    lateinit var currentButtonPair:LinearLayout
    var collectionNumber = 0

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToTraining()
    }

    @SuppressLint("SetTextI18n")
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
        val k:String? = b?.getString("flashcardPath")
        val flashcardPath:String = k ?:""
        val v:String? = b?.getString("collectionPath")
        val collectionPath:String = v ?:""
        val i:Int? = b?.getInt("collectionId")
        collectionNumber = i ?:0

        //set title and index hint
        collectionNameHeader.text = MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0)
        collectionIndexHeader.text = "Collection_$collectionNumber"

        //deactivate the edit and move buttons cuz nothing can be selected yet
        deactivateEditAndMoveButtons()

        //scan for flashcards and add them to the list view
        val cards = MyUtils.getCardFolderNames(this, collectionPath)

        for (card in cards) {
            val frontSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 0).toString()
            val backSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 1).toString()
            addFlashcardButton(frontSide, backSide, card)
        }

        buttonMoveCard.setOnClickListener {
            TODO()
        }

        buttonEditCard.setOnClickListener {
            intent = Intent(this, AddCardActivity::class.java)
            val n = Bundle()
            n.putString("collectionPath", collectionPath)
            n.putInt("collectionId", collectionNumber)
            n.putBoolean("calledFromAddCard", false)
            n.putString("cardName", currentCard)
            n.putBoolean("calledFromList", true)
            intent.putExtras(n)
            startActivity(intent)
            finish()
        }

        buttonAddCard.setOnClickListener {
            intent = Intent(this, AddCardActivity::class.java)
            val bun = Bundle()
            bun.putString("collectionPath", collectionPath)
            bun.putInt("collectionId", collectionNumber)
            bun.putBoolean("calledFromAddCard", true)
            bun.putBoolean("calledFromList", true)
            intent.putExtras(bun)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            backToTraining()
        }
    }

    private fun backToTraining() {
        intent = Intent(this, TrainingActivity::class.java)
        val b = Bundle()
        b.putInt("collectionId", collectionNumber)
        intent.putExtras(b)
        startActivity(intent)
        finish()
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun addFlashcardButton(frontSide: String, backSide: String, cardName:String) {

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
        val frontSideButton = Button(this).apply {
            text = frontSide
            contentDescription = cardName
            isAllCaps = false
            setTextColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.white))
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                (170).dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(20, 0, 0, 4)
            }

            // Create a drawable and set it as the background
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.primary))
                setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.primary))
            }
            background = drawable
        }

        // Create the right button
        val backSideButton = Button(this).apply {

            text = backSide
            isAllCaps = false
            setTextColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.white))
            maxLines = 1

            layoutParams = LinearLayout.LayoutParams(
                (170).dpToPx(),  // Width in pixels
                collectionDisplayHeight.dpToPx()   // Height in pixels
            ).apply {
                setMargins(0, 0, 20, 4)
            }

            // Create a drawable and set it as the background
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(this@FlashcardListActivity, R.color.highlight))
                setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.highlight))
            }
            background = drawable
        }

        frontSideButton.setOnClickListener{
            onCardPairClicked(frontSideButton, backSideButton, rowLayout)
        }

        backSideButton.setOnClickListener{
            onCardPairClicked(frontSideButton, backSideButton, rowLayout)
        }

        // Add buttons to the horizontal layout
        rowLayout.addView(frontSideButton)
        rowLayout.addView(backSideButton)

        // Add the horizontal layout to the container
        container.addView(rowLayout)
    }

    private fun deactivateEditAndMoveButtons () {
        buttonMoveCard.isEnabled = false
        buttonMoveCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
        buttonEditCard.isEnabled = false
        buttonEditCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
    }

    private fun activateEditAndMoveButtons () {
        buttonMoveCard.isEnabled = true
        buttonMoveCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
        buttonEditCard.isEnabled = true
        buttonEditCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
    }

    private fun onCardPairClicked (frontSideButton:Button, backSideButton:Button, rowLayout:LinearLayout) {

        if (currentCard != frontSideButton.contentDescription && currentCard != "") { //if another button pair is currently selected

            //reset the old button pair selected state visually
            val oldFrontSideButton: Button = currentButtonPair.getChildAt(0) as Button
            val oldBackSideButton: Button = currentButtonPair.getChildAt(1) as Button
            (oldFrontSideButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.primary))
            (oldBackSideButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.highlight))

            //select the clicked button pair visually
            val newStrokeColor = ContextCompat.getColor(this@FlashcardListActivity, R.color.white)
            (frontSideButton.background as GradientDrawable).setStroke(4, newStrokeColor)
            (backSideButton.background as GradientDrawable).setStroke(4, newStrokeColor)

            //select the clicked button pair
            currentButtonPair = rowLayout
            currentCard = frontSideButton.contentDescription.toString()

        } else if (currentCard == "") { //if nothing is selected currently

            //select the clicked button pair visually
            val newStrokeColor = ContextCompat.getColor(this@FlashcardListActivity, R.color.white)
            (frontSideButton.background as GradientDrawable).setStroke(4, newStrokeColor)
            (backSideButton.background as GradientDrawable).setStroke(4, newStrokeColor)

            //select the clicked button pair
            currentButtonPair = rowLayout
            currentCard = frontSideButton.contentDescription.toString()

            activateEditAndMoveButtons()
        } else { //if the button currently selected is clicked again

            //reset the current button pair selected state visually
            (frontSideButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.primary))
            (backSideButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.highlight))

            //reset the current button pair selected state
            currentCard = ""

            deactivateEditAndMoveButtons()
        }

    }
}