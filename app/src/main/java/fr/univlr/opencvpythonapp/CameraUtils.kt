package fr.univlr.opencvpythonapp

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.nio.ByteBuffer

fun setupImageAnalysis(): ImageAnalysis {
    return ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
}

fun bindImageAnalysisToLifecycle(lifecycleOwner: LifecycleOwner, controller: LifecycleCameraController, imageAnalysis: ImageAnalysis, activity: MainActivity) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, controller.cameraSelector, imageAnalysis)
    }, ContextCompat.getMainExecutor(activity))
    controller.bindToLifecycle(lifecycleOwner)
}

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


fun Bitmap.toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(byteCount)
    copyPixelsToBuffer(byteBuffer)
    return byteBuffer.array()
}


fun convertToBitmap(byteArray: ByteArray): Bitmap {
    val width = 640
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteArray))
    return bitmap
}


fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

