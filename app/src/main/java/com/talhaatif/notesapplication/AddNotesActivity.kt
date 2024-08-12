package com.talhaatif.notesapplication

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.talhaatif.notesapplication.databinding.ActivityAddNotesBinding
import com.talhaatif.notesapplication.firebase.Util
import com.talhaatif.notesapplication.firebase.Variables
import java.text.SimpleDateFormat
import java.util.*

class AddNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNotesBinding
    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null
    private val storageRef = Variables.storageRef
    private val db = Variables.db
    private val util = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set EditTexts as read-only
        binding.imageEditText.isFocusable = false
        binding.deadlineEditText.isFocusable = false

        // Open image picker
        binding.imageIcon.setOnClickListener {
            openImagePicker()
        }

        // Open date picker
        binding.deadlineIcon.setOnClickListener {
            openDatePicker()
        }

        // Add note
        binding.addTodoButton.setOnClickListener {
            addNote()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.imageEditText.setText(getFileNameFromUri(imageUri!!)) // Display the image name
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                return cursor.getString(columnIndex)
            }
        }
        return "Unknown"
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

    private fun addNote() {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val deadline = binding.deadlineEditText.text.toString().trim()
        val noteId = UUID.randomUUID().toString() // Generate a unique ID for the note

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and Description are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Adding note...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val uid = util.getLocalData(this, "uid")

        val noteData = hashMapOf(
            "noteId" to noteId,
            "noteTitle" to title,
            "noteDescription" to description,
            "noteDate" to if (deadline.isNotEmpty()) com.google.firebase.Timestamp.now() else null,
            "noteImage" to "", // Initially empty, will update after upload
            "uid" to uid
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
        db.collection("notes")
            .document(noteData["noteId"] as String)
            .set(noteData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Note added successfully!", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                // Set result and finish the activity
                val resultIntent = Intent()
                resultIntent.putExtra("noteUpdated", true)
                setResult(RESULT_OK, resultIntent)
                finish() // Close the activity
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage("Error adding note: ${e.message}", this)
                Log.e("AddNoteActivity", "Error adding note: ${e.message}", e)
                progressDialog.dismiss()
            }
    }
}
