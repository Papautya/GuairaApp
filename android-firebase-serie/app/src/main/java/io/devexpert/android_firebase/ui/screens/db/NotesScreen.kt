package io.devexpert.android_firebase.ui.screens.db

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.devexpert.android_firebase.model.Contact
import io.devexpert.android_firebase.model.Note
import io.devexpert.android_firebase.utils.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotesScreen(firestore: FirestoreManager) {
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val notes by firestore.getNotesFlow().collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddNoteDialog = true
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
            if (showAddNoteDialog) {
                AddNoteDialog(
                    onNoteAdded = { note ->
                        scope.launch {
                            firestore.addNote(note)
                        }
                        showAddNoteDialog = false
                    },
                    onDialogDismissed = { showAddNoteDialog = false },
                )
            }
        }
    ) {
        if(!notes.isNullOrEmpty()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp)
            ) {
                notes.forEach {
                    item {
                        NoteItem(note = it, firestore = firestore)
                    }
                }
            }
        } else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No se encontraron \nNotas",
                    fontSize = 18.sp, fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, firestore: FirestoreManager) {
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }

    val onDeleteNoteConfirmed: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            firestore.deleteNote(note.id ?: "")
        }
    }

    if (showDeleteNoteDialog) {
        DeleteNoteDialog(
            onConfirmDelete = {
                onDeleteNoteConfirmed()
                showDeleteNoteDialog = false
            },
            onDismiss = {
                showDeleteNoteDialog = false
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()

    if (showEditNoteDialog) {
        EditNoteDialog(
            note = note,
            onNoteUpdated = { updatedNote ->
                coroutineScope.launch {
                    firestore.updateNote(updatedNote)
                    showEditNoteDialog = false
                }
            },
            onDialogDismissed = {
                showEditNoteDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.padding(6.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                fontWeight = FontWeight.Thin,
                fontSize = 13.sp,
                lineHeight = 15.sp
            )

            // Aquí usamos un Row para poner los botones en una fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Alinea los íconos a la derecha
            ) {
                IconButton(
                    onClick = { showDeleteNoteDialog = true }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                }
                IconButton(
                    onClick = { showEditNoteDialog = true }
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Icon")
                }
            }
        }
    }
}


@Composable
fun AddNoteDialog(onNoteAdded: (Note) -> Unit, onDialogDismissed: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Agregar Nota") },
        confirmButton = {
            Button(
                onClick = {
                    val newNote = Note(
                        title = title,
                        content = content)
                    onNoteAdded(newNote)
                    title = ""
                    content = ""
                }
            ) {
                Text(text = "Agregar")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDialogDismissed()
                }
            ) {
                Text(text = "Cancelar")
            }
        },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Título") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = content,
                    onValueChange = { content = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    maxLines = 4,
                    label = { Text(text = "Contenido") }
                )
            }
        }
    )
}

@Composable
fun DeleteNoteDialog(onConfirmDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Nota") },
        text = { Text("¿Estás seguro que deseas eliminar la nota?") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditNoteDialog(
    note: Note,
    onNoteUpdated: (Note) -> Unit,
    onDialogDismissed: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    AlertDialog(
        onDismissRequest = onDialogDismissed,
        title = { Text(text = "Editar Contacto") },
        confirmButton = {
            Button(onClick = {
                val updatedContact = note.copy(
                    title = title,
                    content = content
                )
                onNoteUpdated(updatedContact)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDialogDismissed) {
                Text("Cancelar")
            }
        },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Título") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = content,
                    onValueChange = { content = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    maxLines = 4,
                    label = { Text(text = "Contenido") }
                )
            }
        }
    )
}