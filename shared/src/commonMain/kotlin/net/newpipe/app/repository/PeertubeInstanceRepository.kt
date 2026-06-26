/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import net.newpipe.app.model.PeertubeInstanceModel

interface PeertubeInstanceRepository {
    fun getCurrentInstance(): PeertubeInstanceModel
    fun getInstanceList(): List<PeertubeInstanceModel>
    fun selectInstance(instance: PeertubeInstanceModel): PeertubeInstanceModel
    fun saveInstanceList(instances: List<PeertubeInstanceModel>)
    fun restoreDefaults(): List<PeertubeInstanceModel>
    suspend fun fetchInstanceMetaData(url: String): PeertubeInstanceModel
}
