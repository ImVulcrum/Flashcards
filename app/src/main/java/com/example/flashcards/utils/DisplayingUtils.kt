package com.example.flashcards.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcards.R

object MyDisplayingUtils {

    data class Flashcard(val frontSide: String, val backSide: String, val cardName: String, var isSelected: Boolean = false )

    class FlashcardAdapter(
        private val flashcards: List<Flashcard>,
        private val onCardPairClicked: (Flashcard, Int) -> Unit
    ) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

        class FlashcardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val frontButton: Button = view.findViewById(R.id.btnFront)
            val backButton: Button = view.findViewById(R.id.btnBack)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
            val layoutId = if (viewType == 1) R.layout.flashcard_list_view_selected else R.layout.flashcard_list_view_normal
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return FlashcardViewHolder(view)
        }

        override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
            val flashcard = flashcards[position]

            holder.frontButton.text = flashcard.frontSide
            holder.backButton.text = flashcard.backSide

            holder.frontButton.setOnClickListener {
                flashcard.isSelected = !flashcard.isSelected
                notifyItemChanged(position) // Notify that this item has changed
                onCardPairClicked(flashcard, position)
            }
            holder.backButton.setOnClickListener {
                flashcard.isSelected = !flashcard.isSelected
                notifyItemChanged(position) // Notify that this item has changed
                onCardPairClicked(flashcard, position)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (flashcards[position].isSelected) 1 else 0
        }

        override fun getItemCount(): Int = flashcards.size
    }
}