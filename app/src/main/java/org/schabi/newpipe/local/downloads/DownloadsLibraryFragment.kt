package org.schabi.newpipe.local.downloads

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.BaseFragment
import org.schabi.newpipe.R
import org.schabi.newpipe.extractor.stream.StreamType

class DownloadsLibraryFragment : BaseFragment() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_downloads_library, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val emptyView: TextView = view.findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadFiles { files ->
            progressBar.visibility = View.GONE
            if (files.isEmpty()) {
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.adapter = DownloadedFilesAdapter(files) { uri, isVideo, fileName ->
                    playFileInternal(requireContext(), uri, isVideo, fileName)
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }

    private fun loadFiles(onResult: (List<DownloadedFile>) -> Unit) {
        scope.launch {
            val files = withContext(Dispatchers.IO) {
                val context = requireContext()
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val downloadUriVideoString = prefs.getString(context.getString(R.string.download_path_video_key), null)
                val downloadUriAudioString = prefs.getString(context.getString(R.string.download_path_audio_key), null)

                val fileList = mutableListOf<DownloadedFile>()
                val pathsToCheck = listOfNotNull(downloadUriVideoString, downloadUriAudioString).distinct()

                if (pathsToCheck.isNotEmpty()) {
                    pathsToCheck.forEach { downloadUriString ->
                        try {
                            if (downloadUriString.startsWith("content://")) {
                                val uri = Uri.parse(downloadUriString)
                                val documentFile = DocumentFile.fromTreeUri(context, uri)
                                documentFile?.listFiles()?.forEach { file ->
                                    if (file.isFile && (file.name?.endsWith(".mp4") == true || file.name?.endsWith(".webm") == true || file.name?.endsWith(".m4a") == true || file.name?.endsWith(".opus") == true)) {
                                        fileList.add(DownloadedFile(file.name ?: "Unknown", file.uri, file.name?.endsWith(".mp4") == true || file.name?.endsWith(".webm") == true))
                                    }
                                }
                            } else {
                                val dir = File(downloadUriString)
                                if (dir.exists() && dir.isDirectory) {
                                    dir.listFiles()?.forEach { file ->
                                        if (file.isFile && (file.name.endsWith(".mp4") || file.name.endsWith(".webm") || file.name.endsWith(".m4a") || file.name.endsWith(".opus"))) {
                                            fileList.add(DownloadedFile(file.name, Uri.fromFile(file), file.name.endsWith(".mp4") || file.name.endsWith(".webm")))
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (fileList.isEmpty()) {
                    try {
                        val defaultDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NewPipe")
                        if (defaultDir.exists() && defaultDir.isDirectory) {
                            defaultDir.listFiles()?.forEach { file ->
                                if (file.isFile && (file.name.endsWith(".mp4") || file.name.endsWith(".webm") || file.name.endsWith(".m4a") || file.name.endsWith(".opus"))) {
                                    fileList.add(DownloadedFile(file.name, Uri.fromFile(file), file.name.endsWith(".mp4") || file.name.endsWith(".webm")))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                fileList.distinctBy { it.uri }
            }
            onResult(files)
        }
    }

    private fun playFileInternal(context: Context, uri: Uri, isVideo: Boolean, fileName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = if (isVideo) "video/*" else "audio/*"
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setPackage(context.packageName)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_VIEW)
                fallbackIntent.setDataAndType(uri, mimeType)
                fallbackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot play file", Toast.LENGTH_SHORT).show()
        }
    }
}

data class DownloadedFile(val name: String, val uri: Uri, val isVideo: Boolean)

class DownloadedFilesAdapter(
    private val files: List<DownloadedFile>,
    private val onClick: (Uri, Boolean, String) -> Unit
) : RecyclerView.Adapter<DownloadedFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_downloaded_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = file.name
        holder.itemView.setOnClickListener {
            onClick(file.uri, file.isVideo, file.name)
        }
    }

    override fun getItemCount() = files.size
}
