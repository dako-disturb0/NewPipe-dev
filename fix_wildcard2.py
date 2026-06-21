import re

with open("app/src/main/java/org/schabi/newpipe/local/downloads/DownloadsLibraryFragment.kt", "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.*", "import kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.Job\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.withContext\nimport kotlinx.coroutines.cancel")

with open("app/src/main/java/org/schabi/newpipe/local/downloads/DownloadsLibraryFragment.kt", "w") as f:
    f.write(content)
