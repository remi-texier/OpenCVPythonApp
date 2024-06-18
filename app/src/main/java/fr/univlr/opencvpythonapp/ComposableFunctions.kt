package fr.univlr.opencvpythonapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File


@Composable
fun CameraPreview(controller: LifecycleCameraController, processedBitmap: Bitmap?, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.matchParentSize()
        )

        processedBitmap?.let { bitmap ->
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
fun BottomControls(sliderPosition: MutableState<Float>, onButtonClick: (String) -> Unit, onSliderChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onButtonClick("Button 1") }) {
                Text("+")
            }
            Button(onClick = { onButtonClick("Button 2") }) {
                Text("-")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition.value,
            onValueChange = {
                sliderPosition.value = it
                onSliderChange(it)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun PhotoBottomSheetContent(bitmaps: List<Bitmap>, modifier: Modifier = Modifier) {
    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalItemSpacing = 16.dp, contentPadding = PaddingValues(16.dp), modifier = modifier) {
        items(bitmaps) { bitmap ->
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.clip(RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
fun DrawerContent(sharedFolderPath: String?, drawerFiles: List<File>, onItemSelected: (String) -> Unit, onAddFileClicked: () -> Unit, onDeleteFileClicked: (File) -> Unit) {
    ModalDrawerSheet {
        Text("Files", modifier = Modifier.padding(16.dp))
        Divider()
        LazyColumn {
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