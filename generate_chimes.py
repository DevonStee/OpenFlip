#!/usr/bin/env python3
"""
生成 Hourly Chime 音频文件
用法: python generate_chimes.py

需要: ffmpeg 安装
"""

import subprocess
import os

# 配置
CHIME_SOUND = "app/src/main/res/raw/chime_sound.mp3"  # 单下声音
OUTPUT_DIR = "app/src/main/res/raw"  # 输出目录
INTERVAL_MS = 1200  # 间隔1.2秒


def generate_chime_audio(count, output_file):
    """生成指定次数的钟声音频"""

    if count == 1:
        # 直接复制原文件
        subprocess.run(["cp", CHIME_SOUND, output_file], check=True)
        print(f"✓ 生成: {output_file} (1下)")
        return

    # 构建 ffmpeg 命令
    # 使用 filter_complex 来拼接音频
    inputs = []
    filter_parts = []

    for i in range(count):
        inputs.extend(["-i", CHIME_SOUND])
        delay = i * INTERVAL_MS
        filter_parts.append(f"[{i}:a]adelay={delay}|{delay}[a{i}]")

    # 混合所有音频
    mix_inputs = "".join([f"[a{i}]" for i in range(count)])
    # Keep each strike level consistent regardless of input count.
    filter_parts.append(f"{mix_inputs}amix=inputs={count}:duration=longest:normalize=0[out]")

    filter_complex = ";".join(filter_parts)

    cmd = [
        "ffmpeg",
        "-y",  # 覆盖输出文件
        *inputs,
        "-filter_complex",
        filter_complex,
        "-map",
        "[out]",
        "-c:a",
        "libmp3lame",
        "-q:a",
        "2",
        output_file,
    ]

    subprocess.run(cmd, check=True, capture_output=True)
    print(f"✓ 生成: {output_file} ({count}下, 间隔{INTERVAL_MS}ms)")


def main():
    print("🎵 生成 Hourly Chime 音频文件\n")

    # 检查原文件存在
    if not os.path.exists(CHIME_SOUND):
        print(f"❌ 错误: 找不到原文件 {CHIME_SOUND}")
        print("请确保 chime_sound.mp3 存在")
        return

    # 生成1-12下的音频
    for count in range(1, 13):
        output_file = f"{OUTPUT_DIR}/chime_{count:02d}.mp3"
        generate_chime_audio(count, output_file)

    # 生成刻钟音频 (1下)
    quarter_file = f"{OUTPUT_DIR}/chime_quarter.mp3"
    subprocess.run(["cp", CHIME_SOUND, quarter_file], check=True)
    print(f"✓ 生成: {quarter_file} (刻钟用)\n")

    print("🎉 全部完成!")
    print("\n生成的文件:")
    for count in range(1, 13):
        print(f"  - chime_{count:02d}.mp3 ({count}下)")
    print(f"  - chime_quarter.mp3 (刻钟)")


if __name__ == "__main__":
    main()
