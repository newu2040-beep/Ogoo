package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.db.CustomLutEntity
import com.example.data.db.OgooDatabase
import com.example.data.db.ProjectEntity
import com.example.data.model.ExportConfig
import com.example.data.model.ExportDestination
import com.example.data.model.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProjectRepository(private val context: Context) {

    private val db = OgooDatabase.getDatabase(context)
    private val projectDao = db.projectDao()
    private val customLutDao = db.customLutDao()

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val favoriteProjects: Flow<List<ProjectEntity>> = projectDao.getFavoriteProjects()
    val draftProjects: Flow<List<ProjectEntity>> = projectDao.getDraftProjects()
    val customLuts: Flow<List<CustomLutEntity>> = customLutDao.getAllCustomLuts()

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun saveProject(project: ProjectEntity) = projectDao.insertProject(project)

    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)

    suspend fun deleteProject(id: String) = projectDao.deleteProjectById(id)

    suspend fun saveCustomLut(lut: CustomLutEntity) = customLutDao.insertCustomLut(lut)

    suspend fun deleteCustomLut(id: String) = customLutDao.deleteById(id)

    suspend fun clearSampleProjects() = withContext(Dispatchers.IO) {
        projectDao.clearSampleProjects()
    }

    /**
     * Saves exported Bitmap to MediaStore Gallery or app files directory according to ExportConfig.
     */
    suspend fun saveExportedBitmap(
        bitmap: Bitmap,
        config: ExportConfig,
        fileNamePrefix: String = "OGOO_EDIT"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.${config.format.extension}"
            val compressFormat = when (config.format) {
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                ExportFormat.JPG -> Bitmap.CompressFormat.JPEG
            }

            if (config.destination == ExportDestination.GALLERY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, config.format.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OGOO")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                resolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream == null) throw Exception("Could not open output stream")
                    bitmap.compress(compressFormat, config.quality, outputStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success(uri.toString())
            } else {
                val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "OGOO_Exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val outputFile = File(exportDir, fileName)

                FileOutputStream(outputFile).use { fos ->
                    bitmap.compress(compressFormat, config.quality, fos)
                }

                Result.success(outputFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
