import re

with open('app/src/main/res/layout/player.xml', 'r') as f:
    content = f.read()

# Replace the linear layout containing the play/pause buttons
content = content.replace('''        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:orientation="horizontal"
            android:weightSum="5.5">

            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playPreviousButton"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_marginEnd="10dp"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:clickable="true"
                android:contentDescription="@string/previous_stream"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ic_previous"
                app:tint="@color/white" />


            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playPauseButton"
                android:layout_width="0dp"
                android:layout_height="60dp"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="@string/pause"
                android:scaleType="fitCenter"
                android:src="@drawable/ic_pause"
                app:tint="@color/white" />

            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playNextButton"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_marginStart="10dp"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:clickable="true"
                android:contentDescription="@string/next_stream"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ic_next"
                app:tint="@color/white" />

        </LinearLayout>''', '''        <org.schabi.newpipe.player.ui.MainPlayerControlsContainer
            android:id="@+id/composeControlsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:layout_marginBottom="20dp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:orientation="horizontal"
            android:weightSum="5.5"
            android:visibility="gone">

            <!-- Hidden legacy buttons to preserve VideoPlayerUi bindings -->
            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playPreviousButton"
                android:layout_width="0dp"
                android:layout_height="40dp" />
            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playPauseButton"
                android:layout_width="0dp"
                android:layout_height="60dp" />
            <androidx.appcompat.widget.AppCompatImageButton
                android:id="@+id/playNextButton"
                android:layout_width="0dp"
                android:layout_height="40dp" />
        </LinearLayout>''')

content = content.replace('''            <LinearLayout
                android:id="@+id/bottomControls"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:gravity="center"
                android:minHeight="40dp"
                android:orientation="horizontal"
                android:paddingLeft="@dimen/player_main_controls_padding"
                android:paddingRight="@dimen/player_main_controls_padding">

                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackCurrentTime"
                    android:layout_width="wrap_content"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:minHeight="30dp"
                    android:text="-:--:--"
                    android:textColor="@android:color/white"
                    tools:ignore="HardcodedText"
                    tools:text="1:06:29" />


                <org.schabi.newpipe.views.FocusAwareSeekBar
                    android:id="@+id/playbackSeekBar"
                    style="@style/Widget.AppCompat.SeekBar"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:layout_marginTop="2dp"
                    android:layout_weight="1"
                    android:nextFocusDown="@id/screenRotationButton"
                    tools:progress="25"
                    tools:secondaryProgress="50" />

                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackEndTime"
                    android:layout_width="wrap_content"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:text="-:--:--"
                    android:textColor="@android:color/white"
                    tools:ignore="HardcodedText"
                    tools:text="1:23:49" />

                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackLiveSync"
                    android:layout_width="wrap_content"
                    android:layout_height="match_parent"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:gravity="center"
                    android:paddingLeft="4dp"
                    android:paddingRight="4dp"
                    android:text="@string/duration_live"
                    android:textAllCaps="true"
                    android:textColor="@android:color/white"
                    android:visibility="gone"
                    tools:ignore="HardcodedText,RtlHardcoded,RtlSymmetry" />

                <androidx.appcompat.widget.AppCompatImageButton
                    android:id="@+id/screenRotationButton"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:layout_marginStart="4dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:contentDescription="@string/toggle_screen_orientation"
                    android:focusable="true"
                    android:nextFocusUp="@id/playbackSeekBar"
                    android:padding="@dimen/player_main_buttons_padding"
                    android:scaleType="fitCenter"
                    android:src="@drawable/ic_fullscreen"
                    android:visibility="gone"
                    app:tint="@color/white"
                    tools:ignore="RtlHardcoded"
                    tools:visibility="visible" />
            </LinearLayout>''', '''            <LinearLayout
                android:id="@+id/bottomControls"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:gravity="center"
                android:minHeight="40dp"
                android:orientation="horizontal"
                android:visibility="gone"
                android:paddingLeft="@dimen/player_main_controls_padding"
                android:paddingRight="@dimen/player_main_controls_padding">

                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackCurrentTime"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
                <org.schabi.newpipe.views.FocusAwareSeekBar
                    android:id="@+id/playbackSeekBar"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackEndTime"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
                <org.schabi.newpipe.views.NewPipeTextView
                    android:id="@+id/playbackLiveSync"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
                <androidx.appcompat.widget.AppCompatImageButton
                    android:id="@+id/screenRotationButton"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
            </LinearLayout>''')

with open('app/src/main/res/layout/player.xml', 'w') as f:
    f.write(content)
