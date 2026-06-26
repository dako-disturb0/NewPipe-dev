/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments.list.kiosk

import android.os.Bundle
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment
import org.schabi.newpipe.extractor.NewPipe

open class KioskFragment : BaseComposeFragment() {
    @JvmField
    protected var serviceId: Int = -1
    @JvmField
    protected var kioskId: String = ""

    override val destination: Destination
        get() = Destination.Kiosk(serviceId = serviceId, kioskId = kioskId)

    companion object {
        @JvmStatic
        fun getInstance(serviceId: Int): KioskFragment {
            val defaultKioskId = try {
                NewPipe.getService(serviceId).kioskList.defaultKioskId
            } catch (e: Exception) {
                "trending"
            }
            return getInstance(serviceId, defaultKioskId)
        }

        @JvmStatic
        fun getInstance(serviceId: Int, kioskId: String): KioskFragment {
            val fragment = KioskFragment()
            fragment.serviceId = serviceId
            fragment.kioskId = kioskId
            val args = Bundle().apply {
                putInt("service_id", serviceId)
                putString("kiosk_id", kioskId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            serviceId = args.getInt("service_id", -1)
            kioskId = args.getString("kiosk_id", "")
        }
        super.onCreate(savedInstanceState)
    }
}
