# 视频增强（Anime4K 超分）

本文档介绍播放器的**视频增强**（Video Enhancement，即 Anime4K 超分辨率）功能：
架构链路、工作原理、预设与限制，以及如何扩展。桌面端（mediamp-mpv 后端）专属。

## 功能概述

桌面端使用 mpv（mediamp-mpv）作为播放内核。mpv 支持运行时加载 GLSL **user shader**
（`glsl-shaders` 选项），因此 Anime4K 不需要重新编译 mpv，作为着色器文件在播放时注入即可。

功能**默认关闭**：不开启时不设置 `glsl-shaders`，渲染管线与改动前完全一致。开启后，
用户从播放器控制条选择一个预设，着色器在 mpv 的渲染管线中执行（vo=libmpv +
`gpu-dumb-mode=no`，见 mediamp-mpv 源码）。

## 架构链路

```
EpisodePage (app/shared, commonMain)
  ├─ vm.player.features[VideoEnhancement]          ← 仅 mpv 后端存在; 其他后端为 null → UI 不显示
  ▼
VideoEnhancementControllerState (app/shared/video-player, commonMain)
  ├─ 当前预设的 UI 状态; 仿 VideoAspectRatioControllerState
  ▼
VideoEnhancement feature (mediamp-api, commonMain)
  └─ VideoEnhancementMode: OFF / ANIME4K_RU_S / _M / _L / _VL
  ▼
MpvVideoEnhancement (mediamp-mpv, jvmMain)
  ├─ 着色器打包在 mediamp-mpv desktopMain resources (shaders/anime4k/*.glsl,
  │   单文件 Restore+Upscale 预设, 来自 Anime4K v4, MIT 许可, 随包附 Anime4K-LICENSE.txt)
  ├─ 首次构造时解包到 java.io.tmpdir/mediamp-anime4k-shaders/ (mpv config=no, 需绝对路径)
  └─ handle.setPropertyString("glsl-shaders", <绝对路径> | "")    ← OFF 时清空
  ▼
libmpv: Anime4K GLSL user shader 在缩放管线执行 → 共享纹理 → Compose/Skia 显示
```

UI 入口：`PlayerControllerDefaults.VideoEnhancementSelector`
（`app/shared/video-player/.../progress/PlayerControllerBar.kt`），在 `EpisodeVideoImpl`
的 `endActions` 中与画面比例选择器并列显示（桌面端）。

## 预设

| 预设 | 说明 |
|---|---|
| 关闭（默认） | 不注入任何着色器 |
| Anime4K S / M / L / VL | Restore+Upscale 链，四档质量（S 最省、VL 最高） |

所有预设来自 mpv 生态的单文件组合变体（每预设一个 `.glsl`，内含还原 + 放大两段 pass）。
UR（先放大后还原）与 Soft 变体也已打包进资源，但第一版 UI 未暴露。

## 生效条件与限制

- 着色器自带 `WHEN` 守卫：**输出需在每个轴上超过输入的 1.2 倍**才执行。
  因此在小窗口或显示器分辨率不超过视频分辨率时，超分不生效（与独立 mpv 行为一致），
  不是 bug。
- 开启后 GPU 负载上升，4K 输出下低端显卡可能掉帧；S 档与关闭是兜底。
- 切换在下一帧渲染时生效，无需重载媒体。
- 该功能不持久化（会话级），应用重启后回到"关闭"。
- Android/iOS 不提供此功能（ExoPlayer/VLC 后端无 shader 管线）。

## 验证

- mediamp 侧：`mediamp-mpv/src/desktopTest/kotlin/MpvAnime4kShaderTest.kt` —— headless
  播放 + RU_M 注入，断言 `glsl-shaders` 回读、播放推进、无 shader 相关错误日志、OFF 清空。
- animeko 侧：`EpisodeVideoControllerTest.touch - video enhancement selector - pick a preset dispatches setMode`
  —— 控制条下拉交互测试。
- 真机：`:app:desktop:run` 播放 1080p 视频并放大窗口，肉眼对比各预设（日志无 shader 错误）。

## 如何扩展（UR / Soft / 更多预设）

1. mediamp-api 的 `VideoEnhancementMode` 加枚举值（保持 OFF 第一，UI 顺序按枚举序）；
2. mediamp-mpv 资源目录 `shaders/anime4k/` 已有全部 16 个变体，只需在
   `MpvVideoEnhancement.PRESET_FILES` 中补枚举 → 文件名的映射；
3. animeko 各语言 `strings.xml` 加文案，`renderVideoEnhancementMode` 加分支。
