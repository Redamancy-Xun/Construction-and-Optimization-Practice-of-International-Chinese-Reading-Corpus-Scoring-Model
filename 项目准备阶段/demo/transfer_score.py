import os
import xml.etree.ElementTree as ET
import pandas as pd

# 遍历 cn 目录下的所有文件
for filename in os.listdir("cn"):
    if filename.endswith(".xml"):
        # 创建空的DataFrame来存储当前文件的数据
        all_data = []

        # 解析 XML 文件
        tree = ET.parse(os.path.join("cn", filename))
        root = tree.getroot()

        # 遍历 XML 树
        for read_sentence in root.findall('.//read_sentence'):
            for word in read_sentence.findall('.//word'):
                sylls = word.findall('.//syll')
                for syll in sylls:
                    syll_data = {
                        'filename': filename,
                        'content': syll.get('content'),
                        'beg_pos': syll.get('beg_pos'),
                        'end_pos': syll.get('end_pos'),
                        'symbol': syll.get('symbol'),
                        'time_len': syll.get('time_len'),
                        'rec_node_type': syll.get('rec_node_type')
                    }
                    all_data.append(syll_data)
                    phones = syll.findall('.//phone')
                    for phone in phones:
                        phone_data = {
                            'filename': filename,
                            'content': phone.get('content'),
                            'beg_pos': phone.get('beg_pos'),
                            'end_pos': phone.get('end_pos'),
                            'symbol': phone.get('symbol'),
                            'time_len': phone.get('time_len'),
                            'rec_node_type': phone.get('rec_node_type'),
                            'dp_message': phone.get('dp_message'),
                            'is_yun': phone.get('is_yun'),
                            'perr_msg': phone.get('perr_msg'),
                            'perr_level_msg': phone.get('perr_level_msg')
                        }
                        all_data.append(phone_data)
            break


        # 将当前文件的数据存储到DataFrame中
        df = pd.DataFrame(all_data)

        # 将DataFrame保存为文本文件，文件名与XML文件一致
        df.to_csv(os.path.join("cn", f"{os.path.splitext(filename)[0]}.txt"), index=False, sep='\t')
        # 将DataFrame保存为Excel文件，文件名与XML文件一致
        df.to_excel(os.path.join("cn", f"{os.path.splitext(filename)[0]}.xlsx"), index=False)

