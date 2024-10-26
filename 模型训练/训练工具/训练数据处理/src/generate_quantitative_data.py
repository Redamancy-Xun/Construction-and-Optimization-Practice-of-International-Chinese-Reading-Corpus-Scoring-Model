import pandas as pd
import os

# 指定要读取的文件夹路径
folder_path = '..\\data\\pre_data\\xlsx'  # 替换为你的文件夹路径

# 指定保存的文件夹路径
save_path = '..\\data\\pre_data\\txt'

# 获取文件夹中所有的xlsx文件
xlsx_files = [f for f in os.listdir(folder_path) if f.endswith('.xlsx')]

# 处理每个xlsx文件
for file_name in xlsx_files:
    file_path = os.path.join(folder_path, file_name)
    df = pd.read_excel(file_path)

    # 从第二行开始读取数据
    df_phone = df.iloc[1:]

    # 生成txt文件，文件名与xlsx文件同名
    output_file_name = f"{os.path.splitext(file_name)[0]}.txt"
    output_file_path = os.path.join(save_path, output_file_name)

    with open(output_file_path, 'w', encoding='utf-8') as f:  # 指定编码为 utf-8
        # 在txt最前面加上一段话
        intro_text = ("此数据为留学生音频量化数据，每条数据都是口语音频中的音素信息，每条数据格式为：{content:{beg_pos,end_pos,dp_message,mono_tone,is_yun,perr_msg};}（数据为空代表数据无此属性）。参数说明：{'content':'音素内容','beg_pos':'开始边界时间','end_pos':'结束边界时间','dp_message':'0正常；16漏读；32增读；64回读；128替换（当dp_message不为0时，perr_msg可能出现与dp_message值保持一致的情况）','mono_tone':'调型','is_yun':'0声母，1韵母','perr_msg':'当is_yun=0时，perr_msg有两种状态：0声母正确，1声母错误；当is_yun=1时，perr_msg有四种状态：0韵母和调型均正确，1韵母错误，2调型错误，3韵母和调型均错误'}（注：content为sil表明是非文本中有的内容）。其中给出的第一条数据是可参考的讯飞AI维度评分。量化数据如下：{"
              + "{" + "流畅度分:" + str(df.iloc[0, 4]) + ",完整度分:" + str(df.iloc[0, 5]) + ",声韵分:" + str(df.iloc[0, 6]) + ",调型分:" + str(df.iloc[0, 7]) + ",总分【模型回归】:" + str(df.iloc[0, 8]) + "};")
        f.write(intro_text)

        for index, row in df_phone.iterrows():
            c_to_k_values = []
            for i in [2, 3, 9, 10, 11, 12]:
                value = row.iloc[i]
                if pd.notna(value):  # 检查是否为非空值
                    # 尝试将字符串转换为数字
                    numeric_value = pd.to_numeric(value, errors='coerce')
                    if pd.notna(numeric_value):  # 如果转换成功
                        c_to_k_values.append(str(int(numeric_value)))  # 转换为整数并添加到列表
                    else:
                        c_to_k_values.append(str(value))  # 保持原字符串
                else:
                    c_to_k_values.append('')  # 保留空值

            content = f"{row.iloc[1]}:" + "{" + f"{','.join(c_to_k_values)}" + "};"
            f.write(content)

        f.write("}")

    print(f"处理完成，结果已保存到 {output_file_name} 文件中。")