import re

with open("app/src/main/java/org/schabi/newpipe/local/downloads/DownloadsLibraryFragment.kt", "r") as f:
    content = f.read()

# Replace wildcard imports with specific ones
content = content.replace("import androidx.compose.foundation.layout.*",
                          "import androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.PaddingValues\nimport androidx.compose.foundation.layout.Arrangement")

content = content.replace("import androidx.compose.material3.*",
                          "import androidx.compose.material3.Text\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CircularProgressIndicator\nimport androidx.compose.material3.Scaffold\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.MaterialTheme")

content = content.replace("import androidx.compose.runtime.*",
                          "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.setValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember")

with open("app/src/main/java/org/schabi/newpipe/local/downloads/DownloadsLibraryFragment.kt", "w") as f:
    f.write(content)
