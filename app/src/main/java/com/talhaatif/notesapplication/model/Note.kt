package com.talhaatif.notesapplication.model

import com.google.firebase.Timestamp

data class Note(
    val noteId: String = "",
    val noteTitle: String = "",
    val noteDescription: String = "",
    val noteDate: Timestamp? = null,
    val noteImage: String = "",
    val uid: String = ""
)

