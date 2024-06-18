package fr.univlr.opencvpythonapp.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import java.io.File

// Contenu du tiroir de navigation contenant les fichiers
@Composable
fun DrawerContent(sharedFolderPath: String?, drawerFiles: List<File>, onItemSelected: (String) -> Unit, onAddFileClicked: () -> Unit, onDeleteFileClicked: (File) -> Unit) {
    ModalDrawerSheet {
        Text("Files", modifier = Modifier.padding(16.dp))
        Divider()
        LazyColumn {
            // Liste des fichiers dans le tiroir
            items(drawerFiles) { file ->
                FileItem(file = file, onItemSelected = onItemSelected, onDeleteFileClicked = onDeleteFileClicked)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        NavigationDrawerItem(
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add File")
                }
            },
            selected = false,
            onClick = onAddFileClicked
        )
    }
}

// Élément individuel de fichier dans le tiroir
@Composable
fun FileItem(file: File, onItemSelected: (String) -> Unit, onDeleteFileClicked: (File) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        if (file.extension in listOf("png", "jpg", "jpeg", "gif", "heic")) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp).padding(end = 8.dp))
        }
        Text(text = file.name, modifier = Modifier.weight(1f).clickable { onItemSelected(file.name) })
        IconButton(onClick = { onDeleteFileClicked(file) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete File")
        }
    }
}
