package fr.univlr.opencvpythonapp.python

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import fr.univlr.opencvpythonapp.utils.convertToBitmap
import fr.univlr.opencvpythonapp.utils.rotateBitmap
import fr.univlr.opencvpythonapp.utils.toBitmap
import fr.univlr.opencvpythonapp.utils.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var python: Python
lateinit var main: PyObject
lateinit var processing: PyObject

// Démarre l'environnement Python
fun startPython(context: Context) {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    python = Python.getInstance()
    main = python.getModule("main")
    processing = python.getModule("processing")
}

fun button1Action(): String? = processing.callAttr("button_1_action").toString() // Appelle la fonction Python associée au bouton 1
fun button2Action(): String? = processing.callAttr("button_2_action").toString() // Appelle la fonction Python associée au bouton 2
fun sliderChange(value: Float): String = processing.callAttr("slider_change", value).toString() // Appelle la fonction Python associée au changement de slider

// Envoie l'image à Python pour traitement
fun processImage(image: ImageProxy, processedBitmapState: MutableStateFlow<Bitmap?>, updateFrameTime: (Long) -> Unit) {
    val startTime = SystemClock.elapsedRealtime()
    try {
        val byteArray = image.toBitmap().toByteArray()
        val result: ByteArray? = main.callAttr("process_image", byteArray).toJava(ByteArray::class.java)
        result?.let {
            val processedBitmap = convertToBitmap(it)
            val rotatedBitmap = rotateBitmap(processedBitmap, 90f)
            processedBitmapState.value = rotatedBitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        image.close()
    }
    val endTime = SystemClock.elapsedRealtime()
    updateFrameTime(endTime - startTime)
}

