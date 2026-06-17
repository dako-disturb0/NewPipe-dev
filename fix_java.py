with open('app/src/main/java/org/schabi/newpipe/player/ui/MainPlayerUi.java', 'r') as f:
    content = f.read()

import_statement = "import org.schabi.newpipe.player.ui.MainPlayerControlsContainer;\n"

content = content.replace("public final class MainPlayerUi extends VideoPlayerUi implements View.OnLayoutChangeListener {", "public final class MainPlayerUi extends VideoPlayerUi implements View.OnLayoutChangeListener {\n    private org.schabi.newpipe.player.ui.MainPlayerControlsContainer composeControlsContainer;\n")

init_views_search = r'(binding = PlayerBinding\.bind\(view\);)'
init_views_replace = r'''\1
        composeControlsContainer = view.findViewById(R.id.composeControlsContainer);
        if (composeControlsContainer != null) {
            composeControlsContainer.setOnPlayPauseClicked(() -> {
                if (player != null) player.playPause();
                return null;
            });
            composeControlsContainer.setOnPreviousClicked(() -> {
                if (player != null) player.playPrevious();
                return null;
            });
            composeControlsContainer.setOnNextClicked(() -> {
                if (player != null) player.playNext();
                return null;
            });
            composeControlsContainer.setOnSeek(progress -> {
                if (player != null && player.getVideoPlayer() != null) {
                    long duration = player.getVideoPlayer().getDuration();
                    player.getVideoPlayer().seekTo((long) (progress * duration));
                }
                return null;
            });
        }
'''

import re
content = re.sub(init_views_search, init_views_replace, content)

update_progress_search = r'(public void onUpdateProgress\(final int currentProgress,\s*final int duration,\s*final int bufferPercent\) \{.*?\s*super\.onUpdateProgress\(currentProgress, duration, bufferPercent\);)'
update_progress_replace = r'''\1

        if (composeControlsContainer != null) {
            composeControlsContainer.isPlaying().setValue(player.isPlaying());
            composeControlsContainer.getShowPrev().setValue(true);
            composeControlsContainer.getShowNext().setValue(true);
            composeControlsContainer.getCurrentTime()
                    .setValue(getTimeString(currentProgress));
            composeControlsContainer.getEndTime()
                    .setValue(getTimeString(duration));

            if (duration > 0) {
                composeControlsContainer.getProgress().setValue((float) currentProgress / duration);
            }
            composeControlsContainer.getBufferProgress().setValue(bufferPercent / 100f);
        }
'''
content = re.sub(update_progress_search, update_progress_replace, content, flags=re.DOTALL)


with open('app/src/main/java/org/schabi/newpipe/player/ui/MainPlayerUi.java', 'w') as f:
    f.write(content)
