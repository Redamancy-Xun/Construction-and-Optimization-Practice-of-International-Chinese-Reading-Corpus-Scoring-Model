import os
import librosa
import soundfile as sf
import array

# 定义子目录名
subdirectory = 'cn'

# 检查子目录是否存在，如果不存在则创建
if not os.path.exists(subdirectory):
    os.makedirs(subdirectory)

# 遍历子目录中的所有音频文件 (.wav 和 .mp3)
for filename in os.listdir(subdirectory):
    if filename.endswith(('.wav', '.mp3')):  # 检查文件扩展名
        # 构建完整的文件路径
        file_path = os.path.join(subdirectory, filename)
        print(f"Processing {file_path}...")  # 打印当前正在处理的文件名

        # 读取音频文件
        y, sr = librosa.load(file_path, sr=None, mono=True)  # 自动获取原始采样率
        print(f"Original sample rate: {sr}")  # 打印原始采样率

        # 重新采样到16000 Hz
        y_16 = librosa.resample(y, orig_sr=sr, target_sr=16000)
        print(f"Resampled to 16000 Hz")

        # 保存处理后的文件为16000Hz的WAV文件
        new_wav_file_path = os.path.splitext(file_path)[0] + '_16000.wav'
        sf.write(new_wav_file_path, y_16, 16000, format='WAV')
        print(f"Written WAV file at 16000 Hz: {new_wav_file_path}")

        # 如果是MP3文件，处理后删除原始文件
        if filename.endswith('.mp3'):
            os.remove(file_path)
            print(f"Deleted original MP3 file: {file_path}")

        # 转换为PCM格式，裁剪到[-1.0, 1.0]范围内
        y_pcm = array.array('h', (int(max(min(sample, 1.0), -1.0) * 32767) for sample in y_16))
        output_file = os.path.splitext(new_wav_file_path)[0] + '.pcm'
        with open(output_file, 'wb') as pcm_file:
            y_pcm.tofile(pcm_file)
        print(f"Written PCM file: {output_file}")

print("所有文件已处理完毕")
