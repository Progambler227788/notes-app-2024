package com.talhaatif.notesapplication

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.talhaatif.notesapplication.databinding.ActivityEditBinding
import com.talhaatif.notesapplication.firebase.Util
import com.talhaatif.notesapplication.firebase.Variables
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null
    private val storageRef = Variables.storageRef
    private val db = Variables.db
    private val util = Util()
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteId = intent.getStringExtra("noteId")
        if (noteId != null) {
            loadNoteData(noteId!!)
        }

        // Open image picker when clicking on the image view
        binding.selectedImageView.setOnClickListener {
            openImagePicker()
        }

        // Open date picker
        binding.deadlineIcon.setOnClickListener {
            openDatePicker()
        }

        // Update note
        binding.addTodoButton.setOnClickListener {
            updateNote()
        }

        // Delete note
        binding.deleteTodoButton.setOnClickListener {
            deleteNote()
        }
    }

    private fun loadNoteData(noteId: String) {
        db.collection("notes").document(noteId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    populateFields(document)
                } else {
                    Toast.makeText(this, "No such note!", Toast.LENGTH_SHORT).show()
                    finish() // Close activity if no note is found
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditActivity", "Error getting note: ${e.message}", e)
            }
    }

    private fun populateFields(document: DocumentSnapshot) {
        val title = document.getString("noteTitle") ?: ""
        val description = document.getString("noteDescription") ?: ""
        val deadline = document.getDate("noteDate")?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
        } ?: ""
        val imageUrl = document.getString("noteImage") ?: ""

        binding.titleEditText.setText(title)
        binding.descriptionEditText.setText(description)
        binding.deadlineEditText.setText(deadline)

        // Load image using Glide
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.cartoon_happy_eyes)
                .into(binding.selectedImageView)
        }

        binding.addTodoButton.text = "EDIT TODO"
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data

            // Load selected image into the ImageView
            Glide.with(this)
                .load(imageUri)
                .into(binding.selectedImageView)
        }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.deadlineEditText.setText(dateFormat.format(selectedDate))
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateNote() {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val deadline = binding.deadlineEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and Description are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating note...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val noteData = hashMapOf(
            "noteTitle" to title,
            "noteDescription" to description,
            "noteDate" to if (deadline.isNotEmpty()) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(deadline) else null,
            "noteImage" to (imageUri?.let { getFileNameFromUri(it) } ?: "")
        )

        if (imageUri != null) {
            val imageRef = storageRef.child("notesImages/${UUID.randomUUID()}")
            imageRef.putFile(imageUri!!)
                .addOnCompleteListener(this) { uploadTask ->
                    if (uploadTask.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            noteData["noteImage"] = uri.toString()
                            saveNoteToFirestore(noteData, progressDialog)
                        }
                    } else {
                        Variables.displayErrorMessage("Image upload failed.", this)
                        progressDialog.dismiss()
                    }
                }
        } else {
            saveNoteToFirestore(noteData, progressDialog)
        }
    }

    private fun saveNoteToFirestore(noteData: Map<String, Any?>, progressDialog: ProgressDialog) {
        noteId?.let {
            db.collection("notes").document(it)
                .set(noteData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    // Set result and finish the activity
                    val resultIntent = Intent()
                    resultIntent.putExtra("noteUpdated", true)
                    setResult(RESULT_OK, resultIntent)
                    finish() // Close the activity
                }
                .addOnFailureListener { e ->
                    Variables.displayErrorMessage("Error updating note: ${e.message}", this)
                    Log.e("EditActivity", "Error updating note: ${e.message}", e)
                    progressDialog.dismiss()
                }
        }
    }
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown"

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    fileName = it.getString(columnIndex)
                }
            }
        }

        return fileName
    }


    private fun deleteNote() {
        noteId?.let {
            db.collection("notes").document(it).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Note deleted successfully!", Toast.LENGTH_SHORT).show()
                    // Set result and finish the activity
                    val resultIntent = Intent()
                    resultIntent.putExtra("noteUpdated", true)
                    setResult(RESULT_OK, resultIntent)
                    finish() // Close the activity
                }
                .addOnFailureListener { e ->
                    Variables.displayErrorMessage("Error deleting note: ${e.message}", this)
                    Log.e("EditActivity", "Error deleting note: ${e.message}", e)
                }
        }
    }
}
