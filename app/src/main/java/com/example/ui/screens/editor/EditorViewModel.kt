package com.example.ui.screens.editor

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ProjectEntity
import com.example.data.lut.CubeLutData
import com.example.data.model.EditRecipe
import com.example.data.model.ExportConfig
import com.example.data.model.ExportDestination
import com.example.data.model.ExportFormat
import com.example.data.model.ExportResolution
import com.example.data.model.HistogramData
import com.example.data.repository.ProjectRepository
import com.example.engine.BitmapLoader
import com.example.engine.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap: StateFlow<Bitmap?> = _sourceBitmap.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _recipe = MutableStateFlow(EditRecipe())
    val recipe: StateFlow<EditRecipe> = _recipe.asStateFlow()

    private val _histogram = MutableStateFlow(HistogramData())
    val histogram: StateFlow<HistogramData> = _histogram.asStateFlow()

    private val _isSplitMode = MutableStateFlow(false)
    val isSplitMode: StateFlow<Boolean> = _isSplitMode.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering: StateFlow<Boolean> = _isRendering.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow<String?>(null)
    val autoSaveStatus: StateFlow<String?> = _autoSaveStatus.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = Stack<EditRecipe>()
    private val redoStack = Stack<EditRecipe>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var customCubeLut: CubeLutData? = null
    private var renderJob: Job? = null
    private var autoSaveJob: Job? = null

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            if (project != null) {
                _currentProject.value = project
                val parsedRecipe = EditRecipe.fromJson(project.recipeJson)
                _recipe.value = parsedRecipe
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()

                val bmp = BitmapLoader.loadBitmap(
                    context = getApplication(),
                    uriString = project.sourceUri,
                    sampleResName = project.sampleResName,
                    maxDimension = 1440
                )
                _sourceBitmap.value = bmp
                if (bmp != null) {
                    scheduleRender(parsedRecipe, immediate = true)
                }
            }
        }
    }

    fun loadFromUri(uriString: String, title: String = "Imported Photo") {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                id = "proj_${System.currentTimeMillis()}",
                title = title,
                sourceUri = uriString,
                recipeJson = EditRecipe().toJson(),
                isDraft = true
            )
            repository.saveProject(newProject)
            _currentProject.value = newProject
            _recipe.value = EditRecipe()

            val bmp = BitmapLoader.loadBitmap(
                context = getApplication(),
                uriString = uriString,
                maxDimension = 1440
            )
            _sourceBitmap.value = bmp
            if (bmp != null) {
                scheduleRender(EditRecipe(), immediate = true)
            }
        }
    }

    fun loadFromBitmap(bitmap: android.graphics.Bitmap, title: String = "Camera Photo") {
        viewModelScope.launch {
            val file = java.io.File(getApplication<Application>().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            withContext(Dispatchers.IO) {
                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                }
            }
            val uriString = android.net.Uri.fromFile(file).toString()
            val newProject = ProjectEntity(
                id = "proj_${System.currentTimeMillis()}",
                title = title,
                sourceUri = uriString,
                recipeJson = EditRecipe().toJson(),
                isDraft = true
            )
            repository.saveProject(newProject)
            _currentProject.value = newProject
            _recipe.value = EditRecipe()
            _sourceBitmap.value = bitmap
            scheduleRender(EditRecipe(), immediate = true)
        }
    }

    fun updateRecipe(newRecipe: EditRecipe, pushToHistory: Boolean = false) {
        if (pushToHistory && _recipe.value != newRecipe) {
            undoStack.push(_recipe.value)
            redoStack.clear()
            updateUndoRedoStates()
        }
        _recipe.value = newRecipe
        scheduleRender(newRecipe, immediate = false)
        saveDraft(newRecipe)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_recipe.value)
            val prev = undoStack.pop()
            _recipe.value = prev
            updateUndoRedoStates()
            scheduleRender(prev, immediate = true)
            saveDraft(prev)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_recipe.value)
            val next = redoStack.pop()
            _recipe.value = next
            updateUndoRedoStates()
            scheduleRender(next, immediate = true)
            saveDraft(next)
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun toggleSplitMode() {
        _isSplitMode.value = !_isSplitMode.value
    }

    fun applyAutoTone() {
        val src = _sourceBitmap.value ?: return
        viewModelScope.launch {
            val autoRecipe = withContext(Dispatchers.Default) {
                ImageProcessor.computeAutoTone(src)
            }
            updateRecipe(
                _recipe.value.copy(
                    exposure = autoRecipe.exposure,
                    contrast = autoRecipe.contrast,
                    highlights = autoRecipe.highlights,
                    shadows = autoRecipe.shadows,
                    vibrance = autoRecipe.vibrance
                ),
                pushToHistory = true
            )
        }
    }

    fun applyLut(lutId: String, intensity: Float = 1.0f) {
        val updated = _recipe.value.copy(lutId = lutId, lutIntensity = intensity)
        updateRecipe(updated, pushToHistory = true)
    }

    fun setCustomCubeLut(cubeData: CubeLutData) {
        customCubeLut = cubeData
        val updated = _recipe.value.copy(lutId = "custom_cube", lutIntensity = 1.0f)
        updateRecipe(updated, pushToHistory = true)
    }

    fun resetCategory(category: String) {
        val current = _recipe.value
        val updated = when (category) {
            "Light" -> current.copy(exposure = 0f, contrast = 0f, highlights = 0f, shadows = 0f, whites = 0f, blacks = 0f)
            "Color" -> current.copy(temperature = 0f, tint = 0f, vibrance = 0f, saturation = 0f)
            "Effects" -> current.copy(texture = 0f, clarity = 0f, dehaze = 0f, vignetteAmount = 0f, filmGrainAmount = 0f)
            "Detail" -> current.copy(sharpening = 0f, noiseReduction = 0f)
            "HSL" -> current.copy(hslHues = List(8) { 0f }, hslSaturations = List(8) { 0f }, hslLuminances = List(8) { 0f })
            "Curves" -> current.copy(
                curvePointsMaster = listOf(0f to 0f, 1f to 1f),
                curvePointsRed = listOf(0f to 0f, 1f to 1f),
                curvePointsGreen = listOf(0f to 0f, 1f to 1f),
                curvePointsBlue = listOf(0f to 0f, 1f to 1f)
            )
            "Wheels" -> current.copy(shadowsSat = 0f, midtonesSat = 0f, highlightsSat = 0f)
            else -> current
        }
        updateRecipe(updated, pushToHistory = true)
    }

    fun resetAll() {
        updateRecipe(EditRecipe(), pushToHistory = true)
    }

    private fun scheduleRender(recipeToRender: EditRecipe, immediate: Boolean) {
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            if (!immediate) {
                delay(16) // Smooth 60fps coalescing
            }
            val src = _sourceBitmap.value ?: return@launch
            _isRendering.value = true

            val processed = withContext(Dispatchers.Default) {
                ImageProcessor.processBitmap(src, recipeToRender, customCubeLut)
            }
            val histo = withContext(Dispatchers.Default) {
                ImageProcessor.computeHistogram(processed)
            }

            _previewBitmap.value = processed
            _histogram.value = histo
            _isRendering.value = false

            // Schedule auto-save to phone gallery after user stops adjusting
            scheduleAutoSaveToPhone(processed, recipeToRender)
        }
    }

    private fun scheduleAutoSaveToPhone(processedBitmap: Bitmap, currentRecipe: EditRecipe) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500) // Debounce so continuous editing doesn't spam gallery
            _autoSaveStatus.value = "Saving to phone..."
            val config = ExportConfig(
                resolution = ExportResolution.RES_4K,
                quality = 95,
                format = ExportFormat.JPG,
                destination = ExportDestination.GALLERY
            )
            val projTitle = _currentProject.value?.title?.replace(" ", "_") ?: "OGOO_EDIT"
            val result = repository.saveExportedBitmap(
                bitmap = processedBitmap,
                config = config,
                fileNamePrefix = projTitle
            )
            if (result.isSuccess) {
                _autoSaveStatus.value = "Saved to Gallery"
            } else {
                _autoSaveStatus.value = "Draft saved"
            }
        }
    }

    fun saveToPhoneNow(onResult: (Boolean) -> Unit) {
        val bmp = _previewBitmap.value ?: return onResult(false)
        viewModelScope.launch {
            _autoSaveStatus.value = "Saving to phone..."
            val config = ExportConfig(
                resolution = ExportResolution.RES_4K,
                quality = 100,
                format = ExportFormat.JPG,
                destination = ExportDestination.GALLERY
            )
            val projTitle = _currentProject.value?.title?.replace(" ", "_") ?: "OGOO_EDIT"
            val result = repository.saveExportedBitmap(
                bitmap = bmp,
                config = config,
                fileNamePrefix = projTitle
            )
            if (result.isSuccess) {
                _autoSaveStatus.value = "Saved to Gallery"
                onResult(true)
            } else {
                _autoSaveStatus.value = "Save failed"
                onResult(false)
            }
        }
    }

    private fun saveDraft(r: EditRecipe) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val updated = proj.copy(
                recipeJson = r.toJson(),
                updatedAt = System.currentTimeMillis()
            )
            repository.updateProject(updated)
            _currentProject.value = updated
        }
    }
}
