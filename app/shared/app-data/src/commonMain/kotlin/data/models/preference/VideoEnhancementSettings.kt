/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.openani.mediamp.features.VideoEnhancementMode

/**
 * 视频增强 (Anime4K 超分) 的持久化设置.
 *
 * [mode] 是上次用户选择的预设; 默认 [VideoEnhancementMode.OFF] (不增强).
 * 仅桌面端 mpv 后端提供该功能, 其他平台忽略此设置.
 */
@Immutable
@Serializable
data class VideoEnhancementSettings(
    val mode: VideoEnhancementMode = VideoEnhancementMode.OFF,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = VideoEnhancementSettings()
    }
}
