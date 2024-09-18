package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var nameOfCurrentCollection:String

    var currentCard:String = ""
    private var buttonsCurrentlySelected = mutableListOf<LinearLayout>()

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
        val i:String? = b?.getString("collectionId")
        nameOfCurrentCollection = i ?:""

        //set title and index hint
        collectionNameHeader.text = MyUtils.readLineFromFile(collectionPath + "/Properties.txt", 0)
        collectionIndexHeader.text = nameOfCurrentCollection

        //deactivate the edit and move buttons cuz nothing can be selected yet
        deactivateEditButton()
        deactivateMoveButton()

        //scan for flashcards and add them to the list view
        val cards = MyUtils.getCardFolderNames(this, collectionPath)

        for (card in cards) {
            val frontSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 0).toString()
            val backSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 1).toString()
            addFlashcardButton(frontSide, backSide, card)
        }

        buttonMoveCard.setOnClickListener {
            val collectionsPath = collectionPath.removeSuffix("/$nameOfCurrentCollection")

            val collectionIds = MyUtils.getFoldersInDirectory(collectionsPath)
            val collectionTuple = mutableListOf<MyUtils.SpinnerItem>()
            for (collectionId in collectionIds) {
                val collectionName = MyUtils.readLineFromFile(collectionsPath + "/" + collectionId + "/Properties.txt", 0)
                collectionName?.let { it1 -> MyUtils.SpinnerItem(it1, collectionId) }
                    ?.let { it2 -> collectionTuple.add(it2) }
            }

            MyUtils.showDropdownDialog(this,"Move Cards", "Chose the collection this card should be moved to", collectionTuple) { isConfirmed, selectedItem ->
                if (isConfirmed) {
                    if (selectedItem != null) {
                        for (buttonPair in buttonsCurrentlySelected) {
                            val nameOfCurrentCard = buttonPair.getChildAt(1).contentDescription.toString()
                            MyUtils.moveCardToCollection(context=this, oldCollectionPath = collectionPath, newCollectionPath = collectionsPath + "/" + selectedItem.description, cardName = nameOfCurrentCard, pathOfTheFlashcard = flashcardPath + "/" + nameOfCurrentCard)
                        }

                        MyUtils.createShortToast(this, "Moved Cards to collection: ${selectedItem.description}")

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

        buttonEditCard.setOnClickListener {
            intent = Intent(this, AddCardActivity::class.java)
            val n = Bundle()
            n.putString("collectionPath", collectionPath)
            n.putString("collectionId", nameOfCurrentCollection)
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
            bun.putString("collectionId", nameOfCurrentCollection)
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
        b.putString("collectionId", nameOfCurrentCollection)
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
        val leftButton = Button(this).apply {
            text = frontSide
            contentDescription = ""
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
        val rightButton = Button(this).apply {

            text = backSide
            contentDescription = cardName
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

        leftButton.setOnClickListener{
            onCardPairClicked(leftButton, rightButton, rowLayout)
        }

        rightButton.setOnClickListener{
            onCardPairClicked(leftButton, rightButton, rowLayout)
        }

        // Add buttons to the horizontal layout
        rowLayout.addView(leftButton)
        rowLayout.addView(rightButton)

        // Add the horizontal layout to the container
        container.addView(rowLayout)
    }

    private fun deactivateEditButton () {
        buttonEditCard.isEnabled = false
        buttonEditCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
    }

    private fun deactivateMoveButton () {
        buttonMoveCard.isEnabled = false
        buttonMoveCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
    }

    private fun activateMoveButton () {
        buttonMoveCard.isEnabled = true
        buttonMoveCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
    }

    private fun activateEditButton () {
        buttonEditCard.isEnabled = true
        buttonEditCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.highlight))
    }

    private fun onCardPairClicked (leftButton:Button, rightButton:Button, rowLayout:LinearLayout) {
        if (leftButton.contentDescription == ("selected")) {
            leftButton.contentDescription = ""
            buttonsCurrentlySelected.remove(rowLayout)

            (leftButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.primary))
            (rightButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.highlight))
        }   else {
            leftButton.contentDescription = "selected"
            buttonsCurrentlySelected.add(rowLayout)

            (leftButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.white))
            (rightButton.background as GradientDrawable).setStroke(4, ContextCompat.getColor(this@FlashcardListActivity, R.color.white))
        }

        if (buttonsCurrentlySelected.size >= 1) {
            activateMoveButton()
            if (buttonsCurrentlySelected.size == 1) {
                val buttonCurrentlySelected = buttonsCurrentlySelected[0].getChildAt(1) as Button
                    currentCard = buttonCurrentlySelected.contentDescription.toString()
                activateEditButton()
            }   else {
                deactivateEditButton()
            }
        }   else {
            deactivateEditButton()
            deactivateMoveButton()
        }
    }
}