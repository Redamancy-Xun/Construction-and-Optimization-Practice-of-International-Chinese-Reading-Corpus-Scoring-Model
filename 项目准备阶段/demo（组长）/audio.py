import os
import librosa
import soundfile as sf
import array

# 定义子目录名
subdirectory = 'cn'

# 检查子目录是否存在，如果不存在则创建
if not os.path.exists(subdirectory):
    os.makedirs(subdirectory)

# 遍历文件夹中的所有音频文件 (.wav 和 .mp3)
for filename in os.listdir(subdirectory):
    if filename.endswith(('.wav', '.mp3')):  # 检查文件扩展名
        # 构建完整的文件路径
        file_path = os.path.join(subdirectory, filename)
        # 读取音频文件
        y, sr = librosa.load(file_path, sr=None, mono=True)  # 自动获取原始采样率
        y_16 = librosa.resample(y, orig_sr=sr, target_sr=16000)

        # 检查是否为.mp3文件，如果是则保存为.wav格式
        if filename.endswith('.mp3'):
            # 替换扩展名为.wav
            wav_file_path = os.path.splitext(file_path)[0] + '.wav'
            # 写入.wav文件
            sf.write(wav_file_path, y_16, 16000, format='WAV')
            # 删除原.mp3文件
            os.remove(file_path)
            # 更新file_path为新的.wav文件路径，以便进一步处理
            file_path = wav_file_path

        # 转换为PCM格式
        y_pcm = array.array('h', (int(sample * 32767) for sample in y_16))
        # 构建输出文件路径，使用相同的文件名但扩展名为.pcm
        output_file = os.path.splitext(file_path)[0] + '.pcm'

        # 将PCM格式的音频数据写入文件
        with open(output_file, 'wb') as pcm_file:
            y_pcm.tofile(pcm_file)


print("所有文件已处理完毕")
