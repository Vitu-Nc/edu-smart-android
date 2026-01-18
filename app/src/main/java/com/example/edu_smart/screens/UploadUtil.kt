// util/UploadUtil.kt
package com.example.edu_smart.util

import android.net.Uri
import com.example.edu_smart.model.LibraryDocument
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object UploadUtil {
    fun uploadPdf(
        uri: Uri,
        title: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val filename = UUID.randomUUID().toString() + ".pdf"
        val storageRef = FirebaseStorage.getInstance().reference.child("library/$filename")
        val firestore = FirebaseFirestore.getInstance()

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val doc = LibraryDocument(
                    id = filename,
                    title = title,
                    description = description,
                    url = downloadUri.toString()
                )
                firestore.collection("library").document(filename).set(doc)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
        }.addOnFailureListener { e -> onFailure(e) }
    }
}
