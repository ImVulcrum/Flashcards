package com.example.flashcards

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcards.utils.MyDisplayingUtils
import com.example.flashcards.utils.MyUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FlashcardListActivity : AppCompatActivity() {
    private lateinit var backButton: FloatingActionButton
    private lateinit var collectionNameHeader: TextView
    private lateinit var collectionIndexHeader: TextView
    private lateinit var buttonMoveCard: FloatingActionButton
    private lateinit var buttonEditCard: FloatingActionButton
    private lateinit var buttonAddCard: FloatingActionButton

    private lateinit var flashcardAdapter: MyDisplayingUtils.FlashcardAdapter
    private lateinit var flashcardRecyclerView: RecyclerView

    private lateinit var nameOfCurrentCollection:String

    var currentCard:String = ""
    private var buttonsCurrentlySelected = mutableListOf<MyDisplayingUtils.Flashcard>()

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

        backButton = findViewById(R.id.view_flashcards_back_button)
        collectionNameHeader = findViewById(R.id.show_flashcards_header)
        collectionIndexHeader = findViewById(R.id.collection_index_name)
        buttonMoveCard = findViewById(R.id.b_move_card)
        buttonEditCard = findViewById(R.id.b_edit_card)
        buttonAddCard = findViewById(R.id.b_add_card)

        flashcardRecyclerView = findViewById(R.id.flashcard_recycler_view)

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
        val listOfCardsInCorrectFormat = mutableListOf<MyDisplayingUtils.Flashcard>()

        for (card in cards) {
            val frontSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 0).toString()
            val backSide:String = MyUtils.readLineFromFile(flashcardPath + "/" + card + "/Content.txt", 1).toString()
            listOfCardsInCorrectFormat.add(MyDisplayingUtils.Flashcard(frontSide, backSide, card, false))
        }

        // Initialize the adapter with the flashcards and click handler
        flashcardAdapter = MyDisplayingUtils.FlashcardAdapter(listOfCardsInCorrectFormat) { flashcard, position ->
            onCardPairClicked(flashcard, position)
        }

        flashcardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FlashcardListActivity)
            adapter = flashcardAdapter
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
                            val nameOfCurrentCard = buttonPair.cardName
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

    private fun onCardPairClicked(flashcard: MyDisplayingUtils.Flashcard, position: Int) {

        // Update buttonsCurrentlySelected
        if (flashcard.isSelected) {
            buttonsCurrentlySelected.add(flashcard)
        } else {
            buttonsCurrentlySelected.remove(flashcard)
        }

        // Notify the adapter to refresh the row layout for this item
        flashcardAdapter.notifyItemChanged(position)

        // Update move and edit buttons based on selection state
        if (buttonsCurrentlySelected.size >= 1) {
            activateMoveButton()
            if (buttonsCurrentlySelected.size == 1) {
                currentCard = buttonsCurrentlySelected[0].cardName
                activateEditButton()
            } else {
                deactivateEditButton()
            }
        } else {
            deactivateEditButton()
            deactivateMoveButton()
        }
    }
}