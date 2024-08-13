package com.talhaatif.notesapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.talhaatif.notesapplication.adapter.NoteAdapter
import com.talhaatif.notesapplication.databinding.ActivityMainBinding
import com.talhaatif.notesapplication.firebase.Util
import com.talhaatif.notesapplication.firebase.Variables
import com.talhaatif.notesapplication.model.Note
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private val notesList = mutableListOf<Note>()
    private val filteredList = mutableListOf<Note>()
    private val util = Util()
    private lateinit var datePickers: DatePickers
    private var currentQuery: String = ""
    private var currentStartDate: Date? = null
    private var currentEndDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer() // Start shimmer here
        setupRecyclerView()

        fetchNotes()

        datePickers = DatePickers(this, ::applyDateRangeFilter)

        // Add text change listener to the search bar
        binding.searchBar.addTextChangedListener { text ->
            currentQuery = text.toString().trim()
            filterNotes()
        }

        // Set up click listener for filter icon
        binding.filterIcon.setOnClickListener {
            datePickers.showDatePickerDialog()
        }
        // add item
        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, AddNotesActivity::class.java), EDIT_NOTE_REQUEST_CODE)
        }
        // settings
        binding.settingsIcon.setOnClickListener {
            startActivity(Intent(this,ProfileUpdate::class.java))
        }
    }

    private fun filterNotes() {
        filteredList.clear()
        filteredList.addAll(notesList.filter { note ->
            val matchesQuery = note.noteTitle.contains(currentQuery, ignoreCase = true)
            val matchesDateRange = if (currentStartDate != null && currentEndDate != null) {
                val noteDate = note.noteDate?.toDate()
                noteDate != null && noteDate in currentStartDate!!..currentEndDate!!
            } else {
                true
            }
            matchesQuery && matchesDateRange
        })
        noteAdapter.updateNotes(filteredList)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(filteredList, this) // Use filteredList
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = noteAdapter
    }

    private fun fetchNotes() {
        val uid = util.getLocalData(this, "uid")
//        Toast.makeText(this, "User ID: $uid", Toast.LENGTH_SHORT).show()
        Variables.db.collection("notes")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                notesList.clear()
                for (document in result) {
                    val note = document.toObject(Note::class.java)
                    notesList.add(note)
                }
                filterNotes() // Reapply filter and search after fetching notes
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE

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
                Log.e("MainActivity", "Error getting notes: ${exception.message}")
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
            }
    }

    private fun applyDateRangeFilter(startDate: Date?, endDate: Date?) {
        currentStartDate = startDate
        currentEndDate = endDate
        filterNotes() // Reapply filters based on the new date range
    }

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            val noteUpdated = data?.getBooleanExtra("noteUpdated", false) ?: false
            if (noteUpdated) {

                fetchNotes()
                if(this.currentQuery != ""){
                    this.filterNotes()
                }
                if (currentStartDate!=null && currentEndDate!=null){
                    this.filterNotes()
                }
            }
        }
    }
}
