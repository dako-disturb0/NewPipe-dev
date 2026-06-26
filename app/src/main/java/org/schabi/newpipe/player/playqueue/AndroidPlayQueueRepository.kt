/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.player.playqueue

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.newpipe.app.repository.PlayQueueRepository
import net.newpipe.app.repository.PlayQueueStateItem
import org.schabi.newpipe.player.helper.PlayerHolder
import org.schabi.newpipe.util.Localization

class AndroidPlayQueueRepository(
    private val context: Context
) : PlayQueueRepository {

    private val playerHolder = PlayerHolder.getInstance()

    override val playQueueItems: Flow<List<PlayQueueStateItem>> = callbackFlow {
        var currentQueue: PlayQueue? = null
        var rxSubscription: io.reactivex.rxjava3.disposables.Disposable? = null

        fun updateSubscription(newQueue: PlayQueue?) {
            rxSubscription?.dispose()
            rxSubscription = null
            currentQueue = newQueue

            if (newQueue == null) {
                trySend(emptyList())
                return
            }

            fun emitCurrentQueue() {
                val items = newQueue.streams.mapIndexed { idx, item ->
                    PlayQueueStateItem(
                        title = item.title,
                        uploader = item.uploader,
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url,
                        durationText = if (item.duration >= 0) Localization.getDurationString(item.duration) else null,
                        isPlaying = newQueue.index == idx,
                        index = idx
                    )
                }
                trySend(items)
            }

            emitCurrentQueue()

            rxSubscription = newQueue.broadcastReceiver.subscribe { event ->
                emitCurrentQueue()
            }
        }

        val job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val activeQueue = playerHolder.playQueue.orElse(null)
                if (activeQueue != currentQueue) {
                    updateSubscription(activeQueue)
                }
                delay(500)
            }
        }

        awaitClose {
            job.cancel()
            rxSubscription?.dispose()
        }
    }

    override val currentPlayingIndex: Flow<Int> = callbackFlow {
        var currentQueue: PlayQueue? = null
        var rxSubscription: io.reactivex.rxjava3.disposables.Disposable? = null

        fun updateSubscription(newQueue: PlayQueue?) {
            rxSubscription?.dispose()
            rxSubscription = null
            currentQueue = newQueue

            if (newQueue == null) {
                trySend(-1)
                return
            }

            trySend(newQueue.index)

            rxSubscription = newQueue.broadcastReceiver.subscribe { event ->
                trySend(newQueue.index)
            }
        }

        val job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val activeQueue = playerHolder.playQueue.orElse(null)
                if (activeQueue != currentQueue) {
                    updateSubscription(activeQueue)
                }
                delay(500)
            }
        }

        awaitClose {
            job.cancel()
            rxSubscription?.dispose()
        }
    }

    override fun moveItem(fromIndex: Int, toIndex: Int) {
        playerHolder.playQueue.ifPresent { queue ->
            if (fromIndex in 0 until queue.size() && toIndex in 0 until queue.size()) {
                queue.move(fromIndex, toIndex)
            }
        }
    }

    override fun removeItem(index: Int) {
        playerHolder.playQueue.ifPresent { queue ->
            if (index in 0 until queue.size()) {
                queue.remove(index)
            }
        }
    }

    override fun selectItem(index: Int) {
        val player = playerHolder.player.orElse(null)
        val queue = player?.playQueue
        if (queue != null && index in 0 until queue.size()) {
            player.selectQueueItem(queue.getItem(index))
        }
    }

    override fun clearQueue() {
        // Stop service/player to clear/dismiss playback
        playerHolder.stopService()
    }
}
