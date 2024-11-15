import pandas as pd
import json

# 读取xlsx文件
def read_xlsx(file_path, sheet_name=None):
    df = pd.read_excel(file_path, sheet_name=sheet_name)
    return df

# 读取JSON文件内容
def read_json_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    # 去除换行符和无意义空格
    content = content.replace('\n', '').replace(' ', '')
    return content

# 生成JSON数据
def generate_json(df, row_index, scoring_criteria):
    json_data = {
        "messages": [
            {
                "role": "system",
                "content": "你是一名为留学生中文口语打分的国汉老师，请对学生的口语表现进行评估。你的评分标准基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分。每个维度的评分标准和计算方式如下：" + scoring_criteria
            },
                {
                    "role": "user",
                    "content": "我是一位学习中文口语的留学生，我的朗读内容是：{" + str(df.iloc[row_index, 2]) + "}。我的音频的量化数据是：{" + str(df.iloc[row_index, 4]) + "}。你是一名为留学生中文口语打分的国汉老师，请根据system身份下给你输入的评分标准对我的口语表现进行评估，给我的语速、停顿、声母、韵母、声调和完整度五个维度分别给出0~5的整数评分并给出有实际意义和作用的评语。输出格式例子：语速：2；停顿：3；声母：3；韵母：3；声调：3；完整度：5；评语：朗读不够流利，有少许声母韵母错误，但完成度很好，希望继续努力~！"
                },
            {
                "role": "assistant",
                "content": "语速：" + str(df.iloc[row_index, 5]) + "；停顿：" + str(df.iloc[row_index, 6]) + "；声母：" + str(df.iloc[row_index, 7]) + "；韵母：" + str(df.iloc[row_index, 8]) + "；声调：" + str(df.iloc[row_index, 9]) + "；完整度：" + str(df.iloc[row_index, 10]) + "；评语：" + str(df.iloc[row_index, 11])
            }
        ]
    }
    return json_data

# 保存JSONL文件
def save_jsonl(json_data, output_file):
    with open(output_file, 'a', encoding='utf-8') as f:
        f.write(json.dumps(json_data, ensure_ascii=False) + '\n')

# 主函数
def main(input_file, output_file, json_file):
    df_1 = read_xlsx(input_file, "第一单元")
    df_2 = read_xlsx(input_file, "第二单元")
    df_3 = read_xlsx(input_file, "第三单元")
    df_4 = read_xlsx(input_file, "第四单元")
    df_6 = read_xlsx(input_file, "第六单元")
    df_7 = read_xlsx(input_file, "第七单元")
    df = pd.concat([df_1, df_2, df_3, df_4, df_6, df_7], ignore_index=True)
    scoring_criteria = read_json_file(json_file)
    with open(output_file, 'w', encoding='utf-8') as f:
        pass  # JSONL 文件不需要初始化
    for index, row in df.iterrows():
        json_data = generate_json(df, index, scoring_criteria)
        save_jsonl(json_data, output_file)

if __name__ == "__main__":
    input_file = "..\\data\\train_data\\xlsx\\训练文本v1.0.xlsx"  # 替换为你的输入文件路径
    output_file = "..\\data\\train_data\\json\\训练文本v1.0.jsonl"  # 替换为你的输出文件路径
    json_file = "..\\data\\train_data\\evaluation_standards\\评分标准.json"  # 替换为你的评分标准JSON文件路径
    main(input_file, output_file, json_file)