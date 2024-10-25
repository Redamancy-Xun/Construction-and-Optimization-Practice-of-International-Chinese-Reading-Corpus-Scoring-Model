import pandas as pd
import os

def convert_xlsx_to_csv(xlsx_file, csv_dir, csv_file_base_name):
    # 确保目录存在，如果不存在则创建
    if not os.path.exists(csv_dir):
        os.makedirs(csv_dir)

    # 读取 xlsx 文件
    data = pd.read_excel(xlsx_file, sheet_name=None)  # sheet_name=None 读取所有表格
    # 遍历每一张表格，将其保存为 CSV 文件
    for sheet_name, df in data.items():
        # 构建 CSV 文件名
        csv_filename = os.path.join(csv_dir, f"{sheet_name}.csv")
        # 保存为 CSV 文件
        df.to_csv(csv_filename, index=False, encoding='utf-8-sig')  # 使用 utf-8-sig 编码以避免中文乱码问题
        print(f"已将 '{sheet_name}' 转换为 '{csv_filename}'")

if __name__ == "__main__":
    # 输入 XLSX 文件路径
    xlsx_file_path = "cn/音频-文本对照表.xlsx"

    # 输入 CSV 文件的保存目录
    csv_dir_path = "cn"

    # 输入转换后 CSV 文件的基础名称
    csv_file_base_name = ""

    # 调用转换函数
    convert_xlsx_to_csv(xlsx_file_path, csv_dir_path, csv_file_base_name)