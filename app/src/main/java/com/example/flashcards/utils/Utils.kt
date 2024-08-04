package com.example.flashcards.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.flashcards.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

object MyUtils {

    fun createShortToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 500)
    }

    fun createFolder(context: Context, location:String, folderName: String, toastMessage:String): Boolean{
        val newFolder = File(location, folderName)
        Log.e("folder", location)

        if (!newFolder.exists()) {
            val wasCreated = newFolder.mkdirs() // Creates the folder
            if (wasCreated) {
                createShortToast(context, toastMessage)
                return true
            } else {
                Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            Toast.makeText(context, "Folder already exists", Toast.LENGTH_SHORT).show()
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

    fun showConfirmationDialog(context: Context, title: String, message: String, callback: (Boolean) -> Unit) {
        val dialog1 = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, which ->
                callback(true)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                callback(false)
                dialog.dismiss()
            }
            .create()

        // Apply styles to the dialog buttons
        dialog1.setOnShowListener {
            dialog1.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setBackgroundColor(ContextCompat.getColor(context, R.color.delete_highlight))
            }
            dialog1.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setBackgroundColor(ContextCompat.getColor(context, R.color.highlight))
            }
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

    fun playAudio(filePath: String) {
        val mediaPlayer = MediaPlayer()

        try {
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                mediaPlayer.release()
                false
            }
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer.release()
        }
    }

    fun readLineFromFile(fileLocation: String, lineToBeReadFrom: Int): String? {
        val file = File(fileLocation)
        if (!file.exists()) {
            return "Error:File does not exist"
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
        return "Error: Line not found"
    }

    fun getCollections(appPath: String): List<String> {
        val directory = File(appPath)
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
            // You can log an error or return an empty list depending on your application's logic
            println("Directory does not exist or is not a directory: $appPath")
        }

        return folderNames
    }
}