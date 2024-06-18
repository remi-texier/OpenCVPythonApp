package fr.univlr.opencvpythonapp.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

// Obtient le nom de fichier à partir d'un URI
fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
    var name = ""
    val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        name = cursor.getString(nameIndex)
    }
    return name
}
