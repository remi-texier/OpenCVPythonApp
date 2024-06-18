@file:OptIn(ExperimentalMaterial3Api::class)

package fr.univlr.opencvpythonapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import fr.univlr.opencvpythonapp.python.*
import fr.univlr.opencvpythonapp.utils.*
import fr.univlr.opencvpythonapp.ui.theme.OpenCVPythonAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.*

// Activité principale de l'application
class MainActivity : ComponentActivity() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    private val bitmaps = _bitmaps.asStateFlow()

    private val _processedBitmap = MutableStateFlow<Bitmap?>(null)
    private val processedBitmap = _processedBitmap.asStateFlow()

    private val _frameTime = MutableStateFlow(0)
    private val frameTime = _frameTime.asStateFlow()
    private val frameTimes = mutableListOf<Int>()

    private val _drawerFiles = MutableStateFlow<List<File>>(emptyList())
    private val drawerFiles = _drawerFiles.asStateFlow()

    // Gestionnaire de résultat pour la sélection de fichier
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                addFileToSharedFolder(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializePython() // Initialisation de l'environnement Python
        checkPermissions() // Vérification des permissions nécessaires
        initializeSharedFolder() // Initialisation du dossier partagé

        setContent {
            OpenCVPythonAppTheme {
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()
                val lifecycleOwner = LocalLifecycleOwner.current
                val controller = initializeCameraController() // Initialisation du contrôleur de la caméra

                val bitmaps by bitmaps.collectAsState()
                val processedBitmap by processedBitmap.collectAsState()
                val frameTime by frameTime.collectAsState()
                val drawerFiles by drawerFiles.collectAsState()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val openDrawer = { scope.launch { drawerState.open() } }

                val sliderPosition = remember { mutableStateOf(0.5f) }

                // Composable pour le menu de navigation
                ModalNavigationDrawer(
                    drawerContent = {
                        DrawerContent(
                            sharedFolderPath = getSharedFolderPath(),
                            drawerFiles = drawerFiles,
                            onItemSelected = { item -> Log.d("DrawerItem", item) },
                            onAddFileClicked = { openFilePicker() },
                            onDeleteFileClicked = { file -> deleteFileFromSharedFolder(file) }
                        )
                    },
                    drawerState = drawerState
                ) {
                    // Composable pour la scaffold avec fond de tiroir
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = 0.dp,
                        sheetContent = {
                            PhotoBottomSheetContent(bitmaps = bitmaps, modifier = Modifier.fillMaxWidth())
                        }
                    ) { padding ->
                        MainScreenContent(
                            padding = padding,
                            controller = controller,
                            processedBitmap = processedBitmap,
                            frameTime = frameTime,
                            openDrawer = { openDrawer() },
                            sliderPosition = sliderPosition
                        )
                    }
                    setupImageAnalysis(lifecycleOwner, controller) // Configuration de l'analyse d'image
                }
            }
        }
    }

    // Démarre l'environnement Python
    private fun initializePython() {
        startPython(this)
    }

    // Demande les permissions nécessaires
    private fun checkPermissions() {
        if (!hasRequiredPermissions(this)) {
            requestPermissions(this)
        }
    }

    private fun initializeSharedFolder() {
        val sharedFolder = getExternalFilesDir("SharedFolder")
        if (sharedFolder != null) {
            if (!sharedFolder.exists()) {
                sharedFolder.mkdirs() // Crée le dossier partagé s'il n'existe pas
            }
            _drawerFiles.value = sharedFolder.listFiles()?.toList() ?: emptyList() // Met à jour la liste des fichiers
        }
    }

    private fun initializeCameraController(): LifecycleCameraController {
        return LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE or CameraController.IMAGE_ANALYSIS)
        }
    }

    private fun setupImageAnalysis(lifecycleOwner: LifecycleOwner, controller: LifecycleCameraController) {
        val imageAnalysis = setupImageAnalysis().apply {
            setAnalyzer(ContextCompat.getMainExecutor(this@MainActivity)) { image ->
                lifecycleScope.launch(Dispatchers.IO) {
                    processImage(image, _processedBitmap, ::updateFrameTime) // Traite l'image
                }
            }
        }
        bindImageAnalysisToLifecycle(lifecycleOwner, controller, imageAnalysis, this) // Lie l'analyse d'image au cycle de vie de l'app
    }

    private fun updateFrameTime(frameTime: Long) {
        val frameTimeInt = frameTime.toInt()
        if (frameTimes.size >= 100) {
            frameTimes.removeAt(0) // Supprime l'ancien temps de traitement s'il y en a plus de 100
        }
        frameTimes.add(frameTimeInt) // Ajoute le temps de traitement actuel
        _frameTime.value = frameTimeInt // Met à jour l'état du temps de traitement
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Permet de choisir n'importe quel type de fichier
        }
        pickFileLauncher.launch(intent) // Lance le choix du fichier
    }

    private fun addFileToSharedFolder(uri: Uri) {
        val sharedFolder = getExternalFilesDir("SharedFolder") ?: return
        val fileName = getFileName(contentResolver, uri)
        val file = File(sharedFolder, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream) // Copie le fichier sélectionné dans le dossier partagé
            }
        }
        _drawerFiles.value = sharedFolder.listFiles()?.toList() ?: emptyList() // Remet à jour la liste des fichiers
    }

    private fun deleteFileFromSharedFolder(file: File) {
        if (file.exists()) {
            file.delete() // Supprime le fichier s'il existe
        }
        val sharedFolder = getExternalFilesDir("SharedFolder")
        _drawerFiles.value = sharedFolder?.listFiles()?.toList() ?: emptyList() // Met à jour la liste des fichiers
    }

    private fun getSharedFolderPath(): String? {
        return getExternalFilesDir("SharedFolder")?.absolutePath // Retourne le chemin du dossier partagé
    }
}


// Contenu principal de l'écran avec la caméra et les contrôles
@Composable
fun MainScreenContent(
    padding: PaddingValues,
    controller: LifecycleCameraController,
    processedBitmap: Bitmap?,
    frameTime: Int,
    openDrawer: () -> Unit,
    sliderPosition: MutableState<Float>
) {
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

        IconButton(onClick = openDrawer, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
        }

        BottomControls(
            sliderPosition = sliderPosition,
            onButtonClick = { buttonId ->
                val result = when (buttonId) {
                    "Button 1" -> button1Action()
                    "Button 2" -> button2Action()
                    else -> null
                }
                println(result.toString())
            },
            onSliderChange = { value ->
                sliderPosition.value = value
                val result = sliderChange(value)
                println(result.toString())
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

