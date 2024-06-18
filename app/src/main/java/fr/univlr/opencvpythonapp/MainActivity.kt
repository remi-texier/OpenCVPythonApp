@file:OptIn(ExperimentalMaterial3Api::class)

package fr.univlr.opencvpythonapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import fr.univlr.opencvpythonapp.ui.theme.OpenCVPythonAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    private val bitmaps = _bitmaps.asStateFlow()

    private val _processedBitmap = MutableStateFlow<Bitmap?>(null)
    private val processedBitmap = _processedBitmap.asStateFlow()

    private val _frameTime = MutableStateFlow(0)
    private val frameTime = _frameTime.asStateFlow()
    private val frameTimes = mutableListOf<Int>()

    private lateinit var python: Python
    private lateinit var imageProcessing: PyObject
    private val _drawerFiles = MutableStateFlow<List<File>>(emptyList())
    private val drawerFiles = _drawerFiles.asStateFlow()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                addFileToSharedFolder(uri)
            }
        }
    }

    @SuppressLint("RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        python = Python.getInstance()
        imageProcessing = python.getModule("main")

        if (!hasRequiredPermissions(this)) {
            requestPermissions(this)
        }
        val sharedFolder = getExternalFilesDir("SharedFolder")
        if (sharedFolder != null) {
            if (!sharedFolder.exists()) {
                sharedFolder.mkdirs()
            }
            _drawerFiles.value = sharedFolder.listFiles()?.toList() ?: emptyList()
        }


        setContent {
            OpenCVPythonAppTheme {
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()
                val lifecycleOwner = LocalLifecycleOwner.current
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE or CameraController.IMAGE_ANALYSIS)
                    }
                }
                val bitmaps by bitmaps.collectAsState()
                val processedBitmap by processedBitmap.collectAsState()
                val frameTime by frameTime.collectAsState()

                val drawerFiles by drawerFiles.collectAsState()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val openDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                }

                val sliderPosition = remember { mutableStateOf(0.5f) } // Initialize to mid value

                ModalNavigationDrawer(
                    drawerContent = {
                        DrawerContent(
                            sharedFolderPath = sharedFolder?.absolutePath,
                            drawerFiles = drawerFiles,
                            onItemSelected = { item ->
                                Log.d("DrawerItem", item)
                                // Handle drawer item selection
                            },
                            onAddFileClicked = {
                                openFilePicker()
                            },
                            onDeleteFileClicked = { file ->
                                deleteFileFromSharedFolder(file)
                            }
                        )
                    },
                    drawerState = drawerState
                ) {
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = 0.dp,
                        sheetContent = {
                            PhotoBottomSheetContent(bitmaps = bitmaps, modifier = Modifier.fillMaxWidth())
                        }
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxWidth().aspectRatio(3 / 4f)) {
                                    CameraPreview(controller = controller, processedBitmap = processedBitmap, modifier = Modifier.fillMaxSize())
                                }
                                Text(
                                    text = "Took $frameTime ms to process",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                                )

                                Spacer(modifier = Modifier.weight(1f))
                            }

                            IconButton(onClick = { openDrawer() }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
                            }

                            BottomControls(
                                sliderPosition = sliderPosition,
                                onButtonClick = { buttonId ->
                                    val result = when (buttonId) {
                                        "Button 1" -> imageProcessing.callAttr("button_1_action")
                                        "Button 2" -> imageProcessing.callAttr("button_2_action")
                                        else -> null
                                    }
                                    println(result.toString())
                                },
                                onSliderChange = { value ->
                                    sliderPosition.value = value
                                    val result = imageProcessing.callAttr("slider_change", value)
                                    println(result.toString())
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                    val imageAnalysis = remember {
                        setupImageAnalysis().also { imageAnalysis ->
                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { image ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    processImage(image)
                                }
                            }
                        }
                    }
                    bindImageAnalysisToLifecycle(lifecycleOwner, controller, imageAnalysis, this)
                }
            }
        }
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFileLauncher.launch(intent)
    }

    private fun addFileToSharedFolder(uri: Uri) {
        val sharedFolder = getExternalFilesDir("SharedFolder") ?: return
        val fileName = getFileName(uri)
        val file = File(sharedFolder, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        _drawerFiles.value = sharedFolder.listFiles()?.toList() ?: emptyList()
    }

    private fun deleteFileFromSharedFolder(file: File) {
        if (file.exists()) {
            file.delete()
        }
        val sharedFolder = getExternalFilesDir("SharedFolder")
        _drawerFiles.value = sharedFolder?.listFiles()?.toList() ?: emptyList()
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
        }
        return name
    }

    private fun processImage(image: ImageProxy) {
        lifecycleScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            try {
                val byteArray = image.toBitmap().toByteArray()
                val result: ByteArray? = imageProcessing.callAttr("process_image", byteArray).toJava(ByteArray::class.java)
                result?.let {
                    val processedBitmap = convertToBitmap(it)
                    val rotatedBitmap = rotateBitmap(processedBitmap, 90f)
                    _processedBitmap.value = rotatedBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
            val endTime = SystemClock.elapsedRealtime()
            updateFrameTime(endTime - startTime)
        }
    }

    private fun updateFrameTime(frameTime: Long) {
        val frameTimeInt = frameTime.toInt()
        if (frameTimes.size >= 100) {
            frameTimes.removeAt(0)
        }
        frameTimes.add(frameTimeInt)
        _frameTime.value = frameTimeInt
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}
