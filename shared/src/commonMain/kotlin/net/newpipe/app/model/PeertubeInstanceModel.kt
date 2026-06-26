/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.model

import kotlinx.serialization.Serializable

@Serializable
data class PeertubeInstanceModel(
    val name: String,
    val url: String
)
