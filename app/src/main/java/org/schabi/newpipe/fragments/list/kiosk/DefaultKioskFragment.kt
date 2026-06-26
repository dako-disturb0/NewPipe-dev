/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments.list.kiosk

import android.os.Bundle
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.util.ServiceHelper

class DefaultKioskFragment : KioskFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (serviceId < 0) {
            updateSelectedDefaultKiosk()
        }
    }

    override fun onResume() {
        super.onResume()
        if (serviceId != ServiceHelper.getSelectedServiceId(requireContext())) {
            updateSelectedDefaultKiosk()
        }
    }

    private fun updateSelectedDefaultKiosk() {
        try {
            serviceId = ServiceHelper.getSelectedServiceId(requireContext())
            kioskId = NewPipe.getService(serviceId).kioskList.defaultKioskId
        } catch (e: Exception) {
            kioskId = "trending"
        }
    }
}
