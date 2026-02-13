#!/bin/bash
# 生成 Hourly Chime 音频文件
# 需要 ffmpeg: brew install ffmpeg

CHIME_SOUND="app/src/main/res/raw/chime_sound.mp3"
OUTPUT_DIR="app/src/main/res/raw"
INTERVAL=1.2  # 间隔1.2秒

if [ ! -f "$CHIME_SOUND" ]; then
    echo "❌ 错误: 找不到 $CHIME_SOUND"
    exit 1
fi

echo "🎵 生成 Hourly Chime 音频文件"
echo ""

# 生成1-12下的音频
for count in {1..12}; do
    OUTPUT="$OUTPUT_DIR/chime_$(printf "%02d" $count).mp3"
    
    if [ $count -eq 1 ]; then
        cp "$CHIME_SOUND" "$OUTPUT"
    else
        # 构建 ffmpeg 输入列表
        INPUTS=""
        for ((i=0; i<$count; i++)); do
            INPUTS="$INPUTS -i $CHIME_SOUND"
        done
        
        # 构建 filter_complex
        FILTER=""
        for ((i=0; i<$count; i++)); do
            DELAY=$(echo "$i * $INTERVAL * 1000" | bc)
            FILTER="$FILTER[$i:a]adelay=${DELAY}|${DELAY}[a$i];"
        done
        
        # 混合
        MIX=""
        for ((i=0; i<$count; i++)); do
            MIX="$MIX[a$i]"
        done
        FILTER="${FILTER}${MIX}amix=inputs=${count}:duration=longest:normalize=0[out]"
        
        ffmpeg -y $INPUTS -filter_complex "$FILTER" -map "[out]" -c:a libmp3lame -q:a 2 "$OUTPUT" 2>/dev/null
    fi
    
    echo "✓ chime_$(printf "%02d" $count).mp3 (${count}下)"
done

# 刻钟音频
cp "$CHIME_SOUND" "$OUTPUT_DIR/chime_quarter.mp3"
echo "✓ chime_quarter.mp3 (刻钟)"

echo ""
echo "🎉 完成! 共生成 13 个音频文件"
