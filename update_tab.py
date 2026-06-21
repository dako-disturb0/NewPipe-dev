import re

with open("app/src/main/java/org/schabi/newpipe/settings/tabs/Tab.java", "r") as f:
    content = f.read()

# 1. Add DOWNLOADS_LIBRARY to Type enum
enum_match = re.search(r'public enum Type \{([\s\S]*?)(\s*private final Tab tab;)', content)
if enum_match:
    enum_content = enum_match.group(1)
    if "DOWNLOADS_LIBRARY" not in enum_content:
        new_enum_content = enum_content.rstrip()
        # Find the last enum value which usually ends with ; or ,
        last_comma = new_enum_content.rfind(';')
        if last_comma != -1:
            new_enum_content = new_enum_content[:last_comma] + ",\n        DOWNLOADS_LIBRARY(new DownloadsLibraryTab());"
        else:
            new_enum_content = new_enum_content + ",\n        DOWNLOADS_LIBRARY(new DownloadsLibraryTab());"
        content = content[:enum_match.start(1)] + new_enum_content + content[enum_match.start(2):]

# 2. Add Imports if necessary
if "org.schabi.newpipe.local.downloads.DownloadsLibraryFragment" not in content:
    content = content.replace("import org.schabi.newpipe.fragments.BlankFragment;", "import org.schabi.newpipe.fragments.BlankFragment;\nimport org.schabi.newpipe.local.downloads.DownloadsLibraryFragment;")

# 3. Add DownloadsLibraryTab class at the end
class_template = """
    public static class DownloadsLibraryTab extends Tab {
        public static final int ID = 10;

        @Override
        public int getTabId() {
            return ID;
        }

        @Override
        public String getTabName(final Context context) {
            return context.getString(R.string.downloads_title);
        }

        @DrawableRes
        @Override
        public int getTabIconRes(final Context context) {
            return R.drawable.ic_file_download;
        }

        @Override
        public DownloadsLibraryFragment getFragment(final Context context) {
            return new DownloadsLibraryFragment();
        }
    }
}
"""

if "class DownloadsLibraryTab" not in content:
    content = content.replace("\n}", class_template)

with open("app/src/main/java/org/schabi/newpipe/settings/tabs/Tab.java", "w") as f:
    f.write(content)
