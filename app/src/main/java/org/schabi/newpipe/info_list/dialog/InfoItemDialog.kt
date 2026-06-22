package org.schabi.newpipe.info_list.dialog

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.schabi.newpipe.App
import org.schabi.newpipe.R
import org.schabi.newpipe.error.ErrorInfo
import org.schabi.newpipe.error.ErrorUtil
import org.schabi.newpipe.error.UserAction
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.player.helper.PlayerHolder
import org.schabi.newpipe.util.StreamTypeUtil
import org.schabi.newpipe.util.external_communication.KoreUtils

class InfoItemDialog private constructor(
    private val activity: Activity,
    private val fragment: Fragment,
    private val info: StreamInfoItem,
    private val entries: List<StreamDialogEntry>
) {
    private val dialog: AlertDialog

    init {
        val composeView = ComposeView(activity).apply {
            setContent {
                MaterialTheme {
                    InfoItemDialogTitle(info)
                }
            }
        }

        val items = entries.map { activity.getString(it.resource) }.toTypedArray()

        dialog = AlertDialog.Builder(activity)
            .setCustomTitle(composeView)
            .setItems(items) { _, which ->
                entries[which].action.onClick(fragment, info)
            }
            .create()
    }

    fun show() {
        dialog.show()
    }

    class Builder @JvmOverloads constructor(
        private val activity: Activity?,
        private val context: Context?,
        private val fragment: Fragment,
        private val infoItem: StreamInfoItem,
        private val addDefaultEntriesAutomatically: Boolean = true
    ) {
        private val entries = mutableListOf<StreamDialogEntry>()

        init {
            requireNotNull(activity) { "activity is null" }
            requireNotNull(context) { "context is null" }
            requireNotNull(context.resources) { "resources is null" }

            if (addDefaultEntriesAutomatically) {
                addDefaultBeginningEntries()
            }
        }

        fun addEntry(entry: StreamDialogDefaultEntry): Builder {
            entries.add(entry.toStreamDialogEntry())
            return this
        }

        fun addAllEntries(vararg newEntries: StreamDialogDefaultEntry): Builder {
            newEntries.forEach { addEntry(it) }
            return this
        }

        fun setAction(
            entry: StreamDialogDefaultEntry,
            action: StreamDialogEntry.StreamDialogEntryAction
        ): Builder {
            val index = entries.indexOfFirst { it.resource == entry.resource }
            if (index != -1) {
                entries[index] = StreamDialogEntry(entry.resource, action)
            }
            return this
        }

        fun addEnqueueEntriesIfNeeded(): Builder {
            val holder = PlayerHolder.getInstance()
            if (holder.isPlayQueueReady) {
                addEntry(StreamDialogDefaultEntry.ENQUEUE)
                if (holder.queuePosition < holder.queueSize - 1) {
                    addEntry(StreamDialogDefaultEntry.ENQUEUE_NEXT)
                }
            }
            return this
        }

        fun addStartHereEntries(): Builder {
            addEntry(StreamDialogDefaultEntry.START_HERE_ON_BACKGROUND)
            if (!StreamTypeUtil.isAudio(infoItem.streamType)) {
                addEntry(StreamDialogDefaultEntry.START_HERE_ON_POPUP)
            }
            return this
        }

        fun addMarkAsWatchedEntryIfNeeded(): Builder {
            val isWatchHistoryEnabled = PreferenceManager
                .getDefaultSharedPreferences(context!!)
                .getBoolean(context.getString(R.string.enable_watch_history_key), false)
            if (isWatchHistoryEnabled && !StreamTypeUtil.isLiveStream(infoItem.streamType)) {
                addEntry(StreamDialogDefaultEntry.MARK_AS_WATCHED)
            }
            return this
        }

        fun addPlayWithKodiEntryIfNeeded(): Builder {
            if (KoreUtils.shouldShowPlayWithKodi(context!!, infoItem.serviceId)) {
                addEntry(StreamDialogDefaultEntry.PLAY_WITH_KODI)
            }
            return this
        }

        fun addDefaultBeginningEntries(): Builder {
            addEnqueueEntriesIfNeeded()
            addStartHereEntries()
            return this
        }

        fun addDefaultEndEntries(): Builder {
            addAllEntries(
                StreamDialogDefaultEntry.DOWNLOAD,
                StreamDialogDefaultEntry.APPEND_PLAYLIST,
                StreamDialogDefaultEntry.SHARE,
                StreamDialogDefaultEntry.OPEN_IN_BROWSER
            )
            addPlayWithKodiEntryIfNeeded()
            addMarkAsWatchedEntryIfNeeded()
            addEntry(StreamDialogDefaultEntry.SHOW_CHANNEL_DETAILS)
            return this
        }

        fun create(): InfoItemDialog {
            if (addDefaultEntriesAutomatically) {
                addDefaultEndEntries()
            }
            return InfoItemDialog(activity!!, fragment, infoItem, entries)
        }

        companion object {
            @JvmStatic
            fun reportErrorDuringInitialization(throwable: Throwable, item: InfoItem) {
                ErrorUtil.showSnackbar(
                    App.instance.baseContext,
                    ErrorInfo(
                        throwable,
                        UserAction.OPEN_INFO_ITEM_DIALOG,
                        "none",
                        item.serviceId
                    )
                )
            }
        }
    }

    companion object {
        private val TAG = InfoItemDialog::class.java.simpleName
    }
}

@Composable
fun InfoItemDialogTitle(info: StreamInfoItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(id = R.dimen.video_item_search_padding),
                top = dimensionResource(id = R.dimen.video_item_search_padding),
                end = dimensionResource(id = R.dimen.video_item_search_padding)
            )
    ) {
        Text(
            text = info.name,
            style = MaterialTheme.typography.titleLarge,
            fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                dimensionResource(id = R.dimen.channel_item_detail_title_text_size).toSp()
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
        )
        if (!info.uploaderName.isNullOrEmpty()) {
            Text(
                text = info.uploaderName!!,
                style = MaterialTheme.typography.bodySmall,
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                    dimensionResource(id = R.dimen.video_item_search_uploader_text_size).toSp()
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }
    }
}
