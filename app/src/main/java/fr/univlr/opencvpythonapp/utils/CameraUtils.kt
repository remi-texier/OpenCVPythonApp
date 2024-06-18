package fr.univlr.opencvpythonapp.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import fr.univlr.opencvpythonapp.ui.MainActivity
import java.nio.ByteBuffer

// Configure et retourne une instance d'ImageAnalysis pour l'analyse d'image en temps réel
fun setupImageAnalysis(): ImageAnalysis {
    return ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
}

// Lie l'analyse d'image au cycle de vie du contrôleur de caméra et de l'owner du cycle de vie
fun bindImageAnalysisToLifecycle(lifecycleOwner: LifecycleOwner, controller: LifecycleCameraController, imageAnalysis: ImageAnalysis, activity: MainActivity) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, controller.cameraSelector, imageAnalysis)
    }, ContextCompat.getMainExecutor(activity))
    controller.bindToLifecycle(lifecycleOwner)
}

// Convertit un ImageProxy en Bitmap
fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width
    val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
    buffer.rewind()
    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
}

// Convertit un Bitmap en tableau d'octets
fun Bitmap.toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(byteCount)
    copyPixelsToBuffer(byteBuffer)
    return byteBuffer.array()
}

// Convertit un tableau d'octets en Bitmap
fun convertToBitmap(byteArray: ByteArray): Bitmap {
    val width = 640
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteArray))
    return bitmap
}

// Fait pivoter un Bitmap selon un certain angle
fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

