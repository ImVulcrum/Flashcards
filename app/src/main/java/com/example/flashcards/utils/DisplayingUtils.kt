package com.example.flashcards.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
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

    data class Collection(val collectionId: String, val collectionName: String, var isSelected: Boolean = false, var isArchived: Boolean = false)

    class CollectionAdapter(
        private val collections: List<Collection>,
        private val onCollectionSelected: (Collection, Int) -> Unit,
        private val onCollectionClicked: (Collection, Int) -> Unit,
        private val onCollectionDeleted: (Collection, Int) -> Unit
    ) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

        class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val selectionButton: Button = view.findViewById(R.id.selectionButton)
            val collectionButton: Button = view.findViewById(R.id.collectionButton)
            val deleteButton: Button = view.findViewById(R.id.deleteCollectionButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
            val layoutId = if (viewType == 1) R.layout.collection_list_view_selected else R.layout.collection_list_view_normal
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return CollectionViewHolder(view)
        }

        override fun getItemCount(): Int = collections.size

        override fun getItemViewType(position: Int): Int {
            return if (collections[position].isSelected) 1 else 0
        }

        override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
            val collection = collections[position]

            holder.collectionButton.text = collection.collectionName

            if (collection.isArchived) {
                val context = holder.itemView.context
                holder.collectionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button_color))
                holder.selectionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button_color))
            }   else {
                val context = holder.itemView.context
                holder.collectionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                holder.selectionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            }


            holder.selectionButton.setOnClickListener {
                collection.isSelected = !collection.isSelected
                notifyItemChanged(position) // Notify that this item has changed
                onCollectionSelected(collection, position)
            }
            holder.collectionButton.setOnClickListener {
                notifyItemChanged(position) // Notify that this item has changed
                onCollectionClicked(collection, position)
            }
            holder.deleteButton.setOnClickListener {
                notifyItemChanged(position) // Notify that this item has changed
                onCollectionDeleted(collection, position)
            }
        }
    }
}