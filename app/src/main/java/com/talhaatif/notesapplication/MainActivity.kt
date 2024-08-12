package com.talhaatif.notesapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.talhaatif.notesapplication.adapter.NoteAdapter
import com.talhaatif.notesapplication.databinding.ActivityMainBinding
import com.talhaatif.notesapplication.firebase.Util
import com.talhaatif.notesapplication.firebase.Variables
import com.talhaatif.notesapplication.model.Note

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private val notesList = mutableListOf<Note>()
    private val util = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.shimmerViewContainer.startShimmer() // Start shimmer here
        setupRecyclerView()

        fetchNotes()

        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, AddNotesActivity::class.java), EDIT_NOTE_REQUEST_CODE)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(notesList, this)
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = noteAdapter
        //Stop Shimmer
//        binding.shimmerViewContainer.stopShimmer()
        //Hide Shimmer view
//        binding.shimmerViewContainer.isVisible = false
    }


    private fun fetchNotes() {


        val uid = util.getLocalData(this, "uid")
        Toast.makeText(this, "User ID: $uid", Toast.LENGTH_SHORT).show()
        Variables.db.collection("notes")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                notesList.clear()
                for (document in result) {
                    val note = document.toObject(Note::class.java)
                    notesList.add(note)
                }
                noteAdapter.notifyDataSetChanged()

//                // Stop shimmer and show/hide relevant views
//                binding.shimmerViewContainer.stopShimmer()
//                binding.shimmerViewContainer.isVisible = false
                Log.d("MainActivity", "Number of notes: ${notesList.size}")


                if (notesList.isEmpty()) {
                    binding.noDataText.visibility = View.VISIBLE
                    binding.rvNotes.visibility = View.GONE
                } else {
                    binding.noDataText.visibility = View.GONE
                    binding.rvNotes.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Variables.displayErrorMessage("Error getting notes: ${exception.message}", this)
                val errorMessage = "Error getting notes: ${exception.message}"
                // Log error message
                Log.e("MainActivity", errorMessage)
                // Stop shimmer in case of error
//                binding.shimmerViewContainer.stopShimmer()
//                binding.shimmerViewContainer.visibility = View.GONE
                // Optionally show an error view
            }
    }

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            val noteUpdated = data?.getBooleanExtra("noteUpdated", false) ?: false
            if (noteUpdated) {
                // Refresh the notes list or perform required actions
                fetchNotes()
            }
        }
    }
}
