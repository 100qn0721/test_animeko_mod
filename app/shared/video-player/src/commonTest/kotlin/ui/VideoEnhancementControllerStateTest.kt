/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.videoplayer.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.ui.framework.takeSnapshot
import org.openani.mediamp.InternalForInheritanceMediampApi
import org.openani.mediamp.features.VideoEnhancement
import org.openani.mediamp.features.VideoEnhancementMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(InternalForInheritanceMediampApi::class)
private class TestVideoEnhancement(initial: VideoEnhancementMode) : VideoEnhancement {
    private val _flow = MutableStateFlow(initial)
    override val mode = _flow.asStateFlow()
    val setCalls = mutableListOf<VideoEnhancementMode>()

    override fun setMode(mode: VideoEnhancementMode) {
        setCalls += mode
        _flow.value = mode
    }
}

class VideoEnhancementControllerStateTest {
    @Test
    fun `init - reading currentMode from videoEnhancement`() = runTest {
        val videoEnhancement = TestVideoEnhancement(VideoEnhancementMode.ANIME4K_RU_M)
        val state = VideoEnhancementControllerState(
            videoEnhancement = videoEnhancement,
            scope = backgroundScope,
        )
        takeSnapshot()

        assertEquals(VideoEnhancementMode.ANIME4K_RU_M, state.currentMode)
        assertEquals(
            VideoEnhancementControllerState.Entries.indexOf(VideoEnhancementMode.ANIME4K_RU_M),
            state.currentIndex,
        )
    }

    @Test
    fun `init - saved non-OFF mode is applied to the backend once`() = runTest {
        val videoEnhancement = TestVideoEnhancement(VideoEnhancementMode.OFF)
        VideoEnhancementControllerState(
            videoEnhancement = videoEnhancement,
            scope = backgroundScope,
            initialMode = VideoEnhancementMode.ANIME4K_RU_L,
        )
        takeSnapshot()

        assertEquals(listOf(VideoEnhancementMode.ANIME4K_RU_L), videoEnhancement.setCalls)
    }

    @Test
    fun `init - saved OFF mode does not touch the backend`() = runTest {
        val videoEnhancement = TestVideoEnhancement(VideoEnhancementMode.OFF)
        VideoEnhancementControllerState(
            videoEnhancement = videoEnhancement,
            scope = backgroundScope,
            initialMode = VideoEnhancementMode.OFF,
        )
        takeSnapshot()

        assertFalse(videoEnhancement.setCalls.isNotEmpty(), "OFF must not call setMode")
    }

    @Test
    fun `setMode - user selection is reported to the persistence callback`() = runTest {
        val videoEnhancement = TestVideoEnhancement(VideoEnhancementMode.OFF)
        val persisted = mutableListOf<VideoEnhancementMode>()
        val state = VideoEnhancementControllerState(
            videoEnhancement = videoEnhancement,
            scope = backgroundScope,
            onModeChanged = { persisted += it },
        )
        takeSnapshot()

        state.setMode(VideoEnhancementMode.ANIME4K_RU_VL)
        takeSnapshot()
        assertEquals(listOf(VideoEnhancementMode.ANIME4K_RU_VL), persisted)
        assertEquals(VideoEnhancementMode.ANIME4K_RU_VL, state.currentMode)
    }
}
