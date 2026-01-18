// screens/UploadScreen.kt
package com.example.edu_smart.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.edu_smart.util.UploadUtil

@Composable
fun UploadScreen() {
    var uri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri = it
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { launcher.launch("application/pdf") }) {
            Text("Choose PDF")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (uri != null && title.isNotBlank()) {
                    isUploading = true
                    UploadUtil.uploadPdf(uri!!, title, description, {
                        isUploading = false
                        title = ""
                        description = ""
                        uri = null
                    }, {
                        isUploading = false
                    })
                }
            },
            enabled = uri != null && !isUploading
        ) {
            Text("Upload")
        }
    }
}
