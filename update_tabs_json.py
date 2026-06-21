import re

with open("app/src/main/java/org/schabi/newpipe/settings/tabs/TabsJsonHelper.java", "r") as f:
    content = f.read()

# Replace FALLBACK_INITIAL_TABS_LIST with DOWNLOADS_LIBRARY added
if "Tab.Type.DOWNLOADS_LIBRARY.getTab()" not in content:
    content = content.replace("Tab.Type.BOOKMARKS.getTab());", "Tab.Type.BOOKMARKS.getTab(),\n            Tab.Type.DOWNLOADS_LIBRARY.getTab());")

with open("app/src/main/java/org/schabi/newpipe/settings/tabs/TabsJsonHelper.java", "w") as f:
    f.write(content)
