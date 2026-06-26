/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual implementation of VideoSurface using reflection to load
 * ExpandableSurfaceView to avoid circular module dependency, with a standard SurfaceView fallback.
 */
@Composable
actual fun VideoSurface(
    modifier: Modifier,
    resizeMode: Int,
    aspectRatio: Float,
    baseHeight: Int,
    maxHeight: Int,
    onSurfaceCreated: (Any) -> Unit,
    onSurfaceDestroyed: () -> Unit
) {
    AndroidView(
        factory = { context ->
            val view = try {
                val clazz = Class.forName("org.schabi.newpipe.player.ExpandableSurfaceView")
                val constructor = clazz.getConstructor(Context::class.java, AttributeSet::class.java)
                constructor.newInstance(context, null) as View
            } catch (e1: Exception) {
                try {
                    val clazz = Class.forName("org.schabi.newpipe.views.ExpandableSurfaceView")
                    val constructor = clazz.getConstructor(Context::class.java, AttributeSet::class.java)
                    constructor.newInstance(context, null) as View
                } catch (e2: Exception) {
                    SurfaceView(context)
                }
            }

            if (view is SurfaceView) {
                view.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        onSurfaceCreated(holder)
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        onSurfaceDestroyed()
                    }
                })
            }
            view
        },
        modifier = modifier,
        update = { view ->
            try {
                val clazz = view.javaClass
                if (clazz.name.endsWith("ExpandableSurfaceView")) {
                    val setResizeModeMethod = clazz.getMethod("setResizeMode", Int::class.javaPrimitiveType)
                    setResizeModeMethod.invoke(view, resizeMode)

                    val setAspectRatioMethod = clazz.getMethod("setAspectRatio", Float::class.javaPrimitiveType)
                    setAspectRatioMethod.invoke(view, aspectRatio)

                    val setHeightsMethod = clazz.getMethod("setHeights", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    setHeightsMethod.invoke(view, baseHeight, maxHeight)
                }
            } catch (e: Exception) {
                // Ignore reflection errors
            }
        }
    )
}
