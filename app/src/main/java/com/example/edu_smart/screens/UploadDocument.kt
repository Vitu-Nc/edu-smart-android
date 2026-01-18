package com.example.edu_smart.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.edu_smart.model.LibraryDocument
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Composable
fun UploadDocumentScreen(navController: NavController) {

    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf("") }

    val pickPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedPdfUri = it
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { pickPdfLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select PDF")
        }

        Spacer(modifier = Modifier.height(8.dp))

        selectedPdfUri?.let {
            Text(text = "PDF selected: ${getFileName(context, it)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedPdfUri != null && title.isNotBlank() && description.isNotBlank()) {
                    uploading = true
                    uploadMessage = ""
                    val fileName = UUID.randomUUID().toString() + ".pdf"
                    val storageRef = FirebaseStorage.getInstance().reference.child("library/$fileName")

                    storageRef.putFile(selectedPdfUri!!)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val doc = LibraryDocument(
                                    title = title,
                                    description = description,
                                    url = uri.toString()
                                )
                                FirebaseFirestore.getInstance().collection("library")
                                    .add(doc)
                                    .addOnSuccessListener {
                                        uploadMessage = "Upload successful!"
                                        title = ""
                                        description = ""
                                        selectedPdfUri = null

                                        // ✅ Navigate to Library screen
                                        navController.navigate("library") {
                                            popUpTo("upload") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        uploadMessage = "Failed to save metadata."
                                        uploading = false
                                    }
                            }
                        }
                        .addOnFailureListener {
                            uploadMessage = "Upload failed."
                            uploading = false
                        }
                } else {
                    uploadMessage = "Please fill all fields and select a PDF."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uploading
        ) {
            Text(if (uploading) "Uploading..." else "Upload Document")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(uploadMessage)
    }
}

// Helper function to show file name
fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "unknown"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            name = it.getString(nameIndex)
        }
    }
    return name
}
