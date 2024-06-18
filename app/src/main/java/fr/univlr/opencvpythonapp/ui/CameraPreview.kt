package fr.univlr.opencvpythonapp.ui

import android.graphics.Bitmap
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

// Composable pour la fenêtre d'aperçu de la caméra
@Composable
fun CameraPreview(controller: LifecycleCameraController, processedBitmap: Bitmap?, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    Box(modifier = modifier) {
        // Connecte la preview Android native pour la prévisualisation de la caméra
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.matchParentSize()
        )
        // Affiche l'image traitée au lieu de la vue de la caméra
        processedBitmap?.let { bitmap ->
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.matchParentSize())
        }
    }
}
