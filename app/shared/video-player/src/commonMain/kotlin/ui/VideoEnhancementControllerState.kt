/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.lang.*
import org.openani.mediamp.InternalForInheritanceMediampApi
import org.openani.mediamp.features.VideoEnhancement
import org.openani.mediamp.features.VideoEnhancementMode
import org.jetbrains.compose.resources.*

@Stable
class VideoEnhancementControllerState(
    private val videoEnhancement: VideoEnhancement,
    scope: CoroutineScope,
    /**
     * 创建时立即应用的预设 (持久化设置恢复). 为 [VideoEnhancementMode.OFF] 时不调用后端,
     * 保持"默认不触碰渲染管线"的语义. 后端拒绝该预设时 (着色器不可用) 当前状态保持 OFF.
     */
    initialMode: VideoEnhancementMode = VideoEnhancementMode.OFF,
    /**
     * 用户通过本控制器切换预设时回调 (持久化用); 恢复初始值不触发.
     */
    private val onModeChanged: ((VideoEnhancementMode) -> Unit)? = null,
) {
    var currentMode by mutableStateOf(videoEnhancement.mode.value)
    val currentIndex by derivedStateOf { Entries.indexOf(currentMode) }

    init {
        if (initialMode != VideoEnhancementMode.OFF) {
            videoEnhancement.setMode(initialMode)
        }
        scope.launch {
            videoEnhancement.mode.collect {
                currentMode = it
            }
        }
    }

    fun setMode(mode: VideoEnhancementMode) {
        videoEnhancement.setMode(mode)
        onModeChanged?.invoke(mode)
    }

    companion object {
        val Entries: List<VideoEnhancementMode> = VideoEnhancementMode.entries
    }
}

@Composable
fun renderVideoEnhancementMode(mode: VideoEnhancementMode): String {
    return when (mode) {
        VideoEnhancementMode.OFF -> stringResource(Lang.video_player_video_enhancement_off)
        VideoEnhancementMode.ANIME4K_RU_S -> stringResource(Lang.video_player_video_enhancement_ru_s)
        VideoEnhancementMode.ANIME4K_RU_M -> stringResource(Lang.video_player_video_enhancement_ru_m)
        VideoEnhancementMode.ANIME4K_RU_L -> stringResource(Lang.video_player_video_enhancement_ru_l)
        VideoEnhancementMode.ANIME4K_RU_VL -> stringResource(Lang.video_player_video_enhancement_ru_vl)
    }
}

@OptIn(InternalForInheritanceMediampApi::class)
object NoOpVideoEnhancement : VideoEnhancement {
    override val mode: StateFlow<VideoEnhancementMode> = MutableStateFlow(VideoEnhancementMode.OFF)
    override fun setMode(mode: VideoEnhancementMode) {

    }
}
