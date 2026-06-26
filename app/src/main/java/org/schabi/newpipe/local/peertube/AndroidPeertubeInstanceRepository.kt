/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.peertube

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.grack.nanojson.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.newpipe.app.model.PeertubeInstanceModel
import net.newpipe.app.repository.PeertubeInstanceRepository
import org.schabi.newpipe.R
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance
import org.schabi.newpipe.util.PeertubeHelper

class AndroidPeertubeInstanceRepository(
    private val context: Context
) : PeertubeInstanceRepository {

    override fun getCurrentInstance(): PeertubeInstanceModel {
        val instance = PeertubeHelper.currentInstance
        return PeertubeInstanceModel(instance.name, instance.url)
    }

    override fun getInstanceList(): List<PeertubeInstanceModel> {
        return PeertubeHelper.getInstanceList(context).map {
            PeertubeInstanceModel(it.name, it.url)
        }
    }

    override fun selectInstance(instance: PeertubeInstanceModel): PeertubeInstanceModel {
        val peertubeInstance = PeertubeInstance(instance.url, instance.name)
        val selected = PeertubeHelper.selectInstance(peertubeInstance, context)
        return PeertubeInstanceModel(selected.name, selected.url)
    }

    override fun saveInstanceList(instances: List<PeertubeInstanceModel>) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val savedInstanceListKey = context.getString(R.string.peertube_instance_list_key)
        
        val jsonWriter = JsonWriter.string().`object`().array("instances")
        for (instance in instances) {
            jsonWriter.`object`()
            jsonWriter.value("name", instance.name)
            jsonWriter.value("url", instance.url)
            jsonWriter.end()
        }
        val jsonToSave = jsonWriter.end().end().done()
        sharedPreferences.edit { putString(savedInstanceListKey, jsonToSave) }
    }

    override fun restoreDefaults(): List<PeertubeInstanceModel> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val savedInstanceListKey = context.getString(R.string.peertube_instance_list_key)
        sharedPreferences.edit { remove(savedInstanceListKey) }
        
        selectInstance(PeertubeInstanceModel(PeertubeInstance.DEFAULT_INSTANCE.name, PeertubeInstance.DEFAULT_INSTANCE.url))
        return getInstanceList()
    }

    override suspend fun fetchInstanceMetaData(url: String): PeertubeInstanceModel = withContext(Dispatchers.IO) {
        val instance = PeertubeInstance(url)
        instance.fetchInstanceMetaData()
        PeertubeInstanceModel(instance.name, instance.url)
    }
}
