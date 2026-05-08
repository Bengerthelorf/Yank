# Yank · 上岛记

> 敲一下，钉住屏幕上重要的东西。

Android 工具：截屏 → 视觉大模型识别 → 结构化 JSON → Pin 到通知栏 / 存为笔记。

## Status

🚧 v0.1 设计完成，开发中。

## 触发方式

Yank 不在 App 内提供识别按钮——自己拍自己没有意义。识别走两种外部入口：

- **App Shortcut**：桌面长按图标、三星 Good Lock RegiStar、Pixel Quick Tap、OnePlus 快捷启动、Tasker 等任何能调用 Shortcut 的工具
- **控制中心磁贴**：下拉控制中心，点击 "上岛记" 磁贴

## 核心流程

1. 通过任一入口触发
2. MediaProjection 截屏
3. 本地 ZXing 解析二维码
4. 截图发送至 OpenAI 兼容 VLM（默认百炼 Qwen3-VL）
5. 模型返回结构化 JSON 数组
6. 按 type 路由：Pin 通知 / 存笔记 / 延迟 Pin（待办）

## Type 体系

排队、取餐、券码、快递、票券（火车 / 登机 / 电影）、待办、notes —— 7 项封闭枚举。

## 依赖

- Kotlin + Jetpack Compose + Material 3 Expressive
- Ktor Client (OpenAI 兼容协议)
- Room + DataStore + EncryptedSharedPreferences
- ZXing core
- WorkManager
- Min SDK 29（Android 10）/ Target SDK 36（Android 16）
