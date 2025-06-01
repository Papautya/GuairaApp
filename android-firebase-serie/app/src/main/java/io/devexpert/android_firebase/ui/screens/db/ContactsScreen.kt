package io.devexpert.android_firebase.ui.screens.db

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import io.devexpert.android_firebase.model.Contact
import io.devexpert.android_firebase.utils.AuthManager
import io.devexpert.android_firebase.utils.RealtimeManager
import java.io.File
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactsScreen(realtime: RealtimeManager, authManager: AuthManager) {
    var showAddContactDialog by remember { mutableStateOf(false) }
    val contacts by realtime.getContactsFlow().collectAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddContactDialog = true
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }

            if (showAddContactDialog) {
                AddContactDialog(
                    onContactAdded = { contact ->
                        realtime.addContact(contact)
                        showAddContactDialog = false
                    },
                    onDialogDismissed = { showAddContactDialog = false },
                    authManager = authManager,
                )
            }
        }
    ) { _  ->
        if(contacts.isNotEmpty()) {
            LazyColumn {
                contacts.forEach { contact ->
                    item {
                        ContactItem(contact = contact, realtime = realtime)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No se encontraron \nContactos",
                    fontSize = 18.sp, fontWeight = FontWeight.Thin, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, realtime: RealtimeManager) {
    var showDeleteContactDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }


    // Crea un lanzador de actividad para capturar una foto y guardarla en un URI especificado.
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            // Crea una copia del contacto actual, actualizando el campo photoUri con el nuevo URI como string
            val updated = contact.copy(photoUri = photoUri.toString())
            // Actualiza el contacto en la base de datos (en este caso, usando Firebase Realtime Database)
            realtime.updateContact(contact.key ?: "", updated)
        }
    }

    fun takePicture(context: Context): Uri {
        // Crea un nuevo archivo en el directorio de imágenes de la app (en almacenamiento externo),
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${UUID.randomUUID()}.jpg"
        )

        // Obtiene un URI seguro para el archivo usando FileProvider,
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Autoridad definida en el Manifest y xml de FileProvider
            photoFile
        )

        // Guarda el URI en una variable global o de clase llamada photoUri
        photoUri = uri

        // Retorna el URI del archivo creado, para que pueda ser usado
        return uri
    }

    if (showEditContactDialog) {
        EditContactDialog(
            contact = contact,
            onContactUpdated = { updatedContact ->
                realtime.updateContact(contact.key ?: "", updatedContact)
                showEditContactDialog = false
            },
            onDialogDismissed = {
                showEditContactDialog = false
            }
        )
    }

    if (showDeleteContactDialog) {
        DeleteContactDialog(
            onConfirmDelete = {
                realtime.deleteContact(contact.key ?: "")
                showDeleteContactDialog = false
            },
            onDismiss = {
                showDeleteContactDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 0.dp)
            .fillMaxWidth())
    {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(3f)) {
                if (!contact.photoUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = contact.photoUri),
                        contentDescription = "Contact Image",
                        modifier = Modifier.size(64.dp).padding(end = 8.dp)
                    )
                }
                Column {
                    Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = contact.phoneNumber, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(text = contact.email, fontWeight = FontWeight.Thin, fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { showDeleteContactDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                }
                IconButton(onClick = { showEditContactDialog = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Icon")
                }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = {
                    val uri = takePicture(context)
                    // Actualiza la variable de estado photoUri con ese URI
                    cameraLauncher.launch(uri)
                }) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera Icon")
                }
            }
        }
    }
}


@Composable
fun AddContactDialog(onContactAdded: (Contact) -> Unit, onDialogDismissed: () -> Unit, authManager: AuthManager) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var uid = authManager.getCurrentUser()?.uid

    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Agregar Contacto") },
        confirmButton = {
            Button(
                onClick = {
                    val newContact = Contact(
                        name = name,
                        phoneNumber = phoneNumber,
                        email = email,
                        uid = uid.toString())
                    onContactAdded(newContact)
                    name = ""
                    phoneNumber = ""
                    email = ""
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
                    value = name,
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Nombre") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    label = { Text(text = "Teléfono") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    label = { Text(text = "Correo electrónico") }
                )
            }
        }
    )
}

@Composable
fun DeleteContactDialog(onConfirmDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar contacto") },
        text = { Text("¿Estás seguro que deseas eliminar el contacto?") },
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
fun EditContactDialog(
    contact: Contact,
    onContactUpdated: (Contact) -> Unit,
    onDialogDismissed: () -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var phoneNumber by remember { mutableStateOf(contact.phoneNumber) }
    var email by remember { mutableStateOf(contact.email) }

    AlertDialog(
        onDismissRequest = onDialogDismissed,
        title = { Text(text = "Editar Contacto") },
        confirmButton = {
            Button(onClick = {
                val updatedContact = contact.copy(
                    name = name,
                    phoneNumber = phoneNumber,
                    email = email
                )
                onContactUpdated(updatedContact)
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Teléfono") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") }
                )
            }
        }
    )
}