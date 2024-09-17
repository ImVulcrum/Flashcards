package com.example.flashcards.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.flashcards.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

object MyUtils {
    data class SpinnerItem(val text: String, val description: String)
    class CustomSpinnerAdapter(context: Context, items: List<SpinnerItem>) :
        ArrayAdapter<SpinnerItem>(context, android.R.layout.simple_spinner_item, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val item = getItem(position)
            view.text = item?.text
            view.setTextColor(ContextCompat.getColor(context, R.color.white))
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight))
            view.setPadding(16, 16, 16, 16)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent) as TextView
            val item = getItem(position)
            view.text = item?.text
            view.setTextColor(ContextCompat.getColor(context, R.color.white))
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight))
            view.setPadding(16, 16, 16, 16)

            // Add hidden description for accessibility purposes
            view.contentDescription = item?.description

            return view
        }
    }

    fun createShortToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 500)
    }

    fun createFolder(context: Context, location:String, folderName: String, toastMessage:String, showMessage:Boolean = true): Boolean{
        val newFolder = File(location, folderName)
        Log.e("folder", location)

        if (!newFolder.exists()) {
            val wasCreated = newFolder.mkdirs() // Creates the folder
            if (wasCreated) {
                if (showMessage) {
                    createShortToast(context, toastMessage)
                }
                return true
            } else {
                Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            //Toast.makeText(context, "Folder already exists", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun deleteFolder(context: Context, folderLocation:String, toastMessage: String) {
        val folderToDelete = File(folderLocation)

        if (folderToDelete.exists()) {
            val wasDeleted = folderToDelete.deleteRecursively() // Deletes the folder and its contents
            if (wasDeleted) {
                createShortToast(context, toastMessage)
            } else {
                Toast.makeText(context, "Failed to delete folder", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Folder does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(context: Context, fileLocation: String, toastMessage: String) {
        val fileToDelete = File(fileLocation)

        if (fileToDelete.exists()) {
            val wasDeleted = fileToDelete.delete() // Deletes the file
            if (wasDeleted) {
                createShortToast(context, toastMessage)
            } else {
                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun showDropdownDialog(context: Context, title: String, message: String, options: List<SpinnerItem>, callback: (Boolean, SpinnerItem?) -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val customTitleView = TextView(context).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.white))
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        val spinner = Spinner(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                300.dpToPx(context),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        val adapter = CustomSpinnerAdapter(context, options)
        spinner.adapter = adapter

        spinner.setPopupBackgroundResource(R.color.dropdown_color)

        val buttonsLayout = LayoutInflater.from(context).inflate(R.layout.dialog_buttons, null) as LinearLayout
        val positiveButton = buttonsLayout.findViewById<Button>(R.id.positiveButton)
        val negativeButton = buttonsLayout.findViewById<Button>(R.id.negativeButton)

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            addView(spinner)
            addView(buttonsLayout)
        }

        val dialog1 = AlertDialog.Builder(context)
            .setCustomTitle(customTitleView)
            .setMessage(message)
            .setView(mainLayout)
            .create()

        positiveButton.setOnClickListener {
            val selectedItem = spinner.selectedItem as SpinnerItem
            callback(true, selectedItem)
            dialog1.dismiss()
        }

        negativeButton.setOnClickListener {
            callback(false, null)
            dialog1.dismiss()
        }

        dialog1.show()
    }

    fun showConfirmationDialog(context: Context, title: String, message: String, callback: (Boolean) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_buttons, null)

        // Create a TextView programmatically for the title
        val customTitleView = TextView(context).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.white))
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val dialog1 = AlertDialog.Builder(context)
            .setCustomTitle(customTitleView)
            .setMessage(message)
            .setView(dialogView) // Set the custom layout as the dialog view
            .create()

        // Set the button click listeners
        dialogView.findViewById<Button>(R.id.positiveButton).setOnClickListener {
            callback(true)
            dialog1.dismiss()
        }

        dialogView.findViewById<Button>(R.id.negativeButton).setOnClickListener {
            callback(false)
            dialog1.dismiss()
        }

        dialog1.show()
    }

    fun createTextFile(folderLocation: String, fileName: String): File? {
        val folder = File(folderLocation)
        if (!folder.exists()) {
            Log.e("createTextFile", "Folder does not exist")
            return null
        }

        val file = File(folder, fileName)
        try {
            if (file.createNewFile()) {
                Log.e("createTextFile", "File created successfully")
                return file
            } else {
                Log.e("createTextFile", "File already exists")
                return file
            }
        } catch (e: IOException) {
            Log.e("createTextFile", "Error creating file: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    fun writeTextFile(fileLocation: String, lineToBeReplaced: Int, newText: String) {
        val file = File(fileLocation)
        if (!file.exists()) {
            Log.e("writeTextFile", "File does not exist")
            return
        }

        try {
            val lines = file.readLines().toMutableList()

            if (lineToBeReplaced in 0 until lines.size) {
                lines[lineToBeReplaced] = newText
            } else if (lineToBeReplaced == lines.size) {
                lines.add(newText)
            } else {
                for (i in lines.size..lineToBeReplaced) {
                    if (i == lineToBeReplaced) {
                        lines.add(newText)
                    } else {
                        lines.add("")
                    }
                }
            }

            file.bufferedWriter().use { writer ->
                for (line in lines) {
                    writer.write(line)
                    writer.newLine()
                }
            }
            Log.e("writeTextFile", "Line updated or added successfully")
        } catch (e: IOException) {
            Log.e("writeTextFile", "Error writing to file: ${e.message}")
            e.printStackTrace()
        }
    }

    // Function to convert dp to pixels
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(filePath: String): Boolean {
        val file = File(filePath)

        if (!file.exists()) {
            return false
        }

        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.setOnPreparedListener {
                it.start()
            }
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
            mediaPlayer?.setOnErrorListener { _, _, _ ->
                mediaPlayer?.release()
                mediaPlayer = null
                false
            }
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer?.release()
            mediaPlayer = null
            return false
        }
        return true
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    fun readLineFromFile(fileLocation: String, lineToBeReadFrom: Int): String? {
        val file = File(fileLocation)
        if (!file.exists()) {
            return "Error: File does not exist"
        }

        var line: String?
        var currentLine = 0
        BufferedReader(FileReader(file)).use { reader ->
            while (reader.readLine().also { line = it } != null) {
                if (currentLine == lineToBeReadFrom) {
                    return line
                }
                currentLine++
            }
        }
        return ""
    }

    fun getFoldersInDirectory (directoryToSearchIn: String): List<String> {
        val directory = File(directoryToSearchIn)
        val folderNames = mutableListOf<String>()

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        folderNames.add(file.name)
                    }
                }
            }
        } else {
            // Handle case where directory does not exist or is not a directory
            Log.e("getFoldersInDirectory", "Directory does not exist or is not a directory: $directoryToSearchIn")
        }

        return folderNames
    }

    fun getCardFolderNames(context: Context, collectionPath:String): List<String> {
        val cardString = readLineFromFile(collectionPath + "/Flashcards.txt", 0)
        var cardNames = listOf<String>()

        if (cardString != null) {
            if (cardString != "-") {
                cardNames = cardString.split(" ")
            }
        }   else {
            createShortToast(context,"No cards in this collection")
        }
        return cardNames
    }

    fun moveCardToCollection(context: Context, oldCollectionPath:String, newCollectionPath:String, cardName:String, pathOfTheFlashcard:String) {
        removeCardFromCollection(context, oldCollectionPath, cardName)

        addCardToCollectionAndReferenceCollection(newCollectionPath, cardName, pathOfTheFlashcard)
    }

    fun addCardToCollectionAndReferenceCollection(collectionPath:String, flashcardName:String, pathOfTheFlashcard:String) {
        //adding the collection reference to the flashcard
        writeTextFile(pathOfTheFlashcard + "/Content.txt", 2, collectionPath)

        //adding the card to the flashcards file or the collection
        var cardStringInTheCollection = readLineFromFile(collectionPath + "/Flashcards.txt", 0)
        var space = " "
        if (cardStringInTheCollection == "-") {
            cardStringInTheCollection = ""
            space = ""
        }
        writeTextFile(collectionPath + "/Flashcards.txt", 0, cardStringInTheCollection + space + flashcardName)

        //adding the card to the shuffle order line in the properties file of the corresponding collection
        var cardOrder = readLineFromFile(collectionPath + "/Properties.txt", 3)
        space = " "
        if (cardOrder == "-") {
            cardOrder = ""
            space = ""
        }
        writeTextFile(collectionPath + "/Properties.txt", 3, cardOrder + space + "n_" + flashcardName)
    }

    fun removeCardFromCollection(context: Context, collectionPath:String, cardName:String) {
        //remove the card from the flashcards file of the collection
        val cardStringInTheCollection = readLineFromFile(collectionPath + "/Flashcards.txt", 0)

        if (cardStringInTheCollection != null) {
            val flashcardsInTheCollection: MutableList<String> = cardStringInTheCollection.split(" ").toMutableList()
            flashcardsInTheCollection.remove(cardName)

            if (flashcardsInTheCollection.isEmpty()) {
                writeTextFile(collectionPath + "/Flashcards.txt", 0, "-")
            }   else {
                writeTextFile(collectionPath + "/Flashcards.txt", 0, flashcardsInTheCollection.joinToString(" "))
            }
        }   else {
            createShortToast(context, "Error: There are no cards in the collection for some reason")
        }

        //remove the card from the orderline in the properties file
        val cardOrder = readLineFromFile(collectionPath + "/Properties.txt", 3)
        if (cardOrder == "-") {
            //do nothing
        } else {
            if (cardOrder != null) {
                val cards: MutableList<String>
                cards = cardOrder.split(" ").toMutableList()

                cards.remove("n_" + cardName)
                cards.remove("f_" + cardName)

                if (cards.isNotEmpty()) {
                    writeTextFile(collectionPath + "/Properties.txt", 3, cards.joinToString(" "))

                    //set the index logic
                    var cardIndex = readLineFromFile(collectionPath + "/Properties.txt", 4)?.toInt()
                    if (cardIndex != null) {
                        cardIndex = cardIndex -1
                    }

                    if (cardIndex != null) {
                        if (cardIndex < 0) {
                            cardIndex = 0
                        }
                    }
                    writeTextFile(collectionPath + "/Properties.txt", 4, cardIndex.toString())

                }   else {
                    writeTextFile(collectionPath + "/Properties.txt", 3, "-")
                    writeTextFile(collectionPath + "/Properties.txt", 4, "0")
                }
            } else {
                Log.e("HUGE ERROR", "orderLine is null")
            }
        }
    }

    fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.isFile
    }
}