import re

with open("app/src/main/java/org/schabi/newpipe/local/downloads/DownloadsLibraryFragment.kt", "r") as f:
    content = f.read()

# Currently Material3 is not available in dependencies. The app uses standard views. Wait, the prompt said:
# "Boleh Menggunakan Jetpack Compose Material 3, Boleh Pake XML, Terserah, Jangan Sampe Error UI."
# It seems Material3 is not installed in the project. The app uses compose 1.11.2 and material3 wasn't added correctly or at all.
# Let's replace Material3 with standard Android XML views to avoid dependency issues, OR add material3 to build.gradle.kts.
