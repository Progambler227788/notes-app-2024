package com.talhaatif.notesapplication.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.talhaatif.notesapplication.EditActivity
import com.talhaatif.notesapplication.R
import com.talhaatif.notesapplication.model.Note

class NoteAdapter(private val notes: List<Note>, private val activity: Activity) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.noteTitle
        holder.descriptionTextView.text = note.noteDescription
        holder.dateTextView.text = note.noteDate?.toDate()?.toString() ?: "No Date"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditActivity::class.java).apply {
                putExtra("noteId", note.noteId)
            }
            activity.startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE)
        }
    }

    override fun getItemCount(): Int = notes.size

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.card_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.card_description)
        val dateTextView: TextView = itemView.findViewById(R.id.card_date)
    }
}
