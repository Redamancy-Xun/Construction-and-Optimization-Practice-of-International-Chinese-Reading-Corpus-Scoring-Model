import xml.etree.ElementTree as ET
import pandas as pd

# 解析XML文件
tree = ET.parse("C:\\Users\\10235\\Desktop\\test.xml")
root = tree.getroot()

# 创建空的DataFrame来存储数据
data = []

# 遍历XML树
for read_sentence in root.findall('.//read_sentence'):
    for word in read_sentence.findall('.//word'):
        sylls = word.findall('.//syll')
        for syll in sylls:
            syll_data = {
                'content': syll.get('content'),
                'beg_pos': syll.get('beg_pos'),
                'end_pos': syll.get('end_pos'),
                'symbol': syll.get('symbol'),
                'time_len': syll.get('time_len'),
                'rec_node_type': syll.get('rec_node_type')
            }
            data.append(syll_data)
            phones = syll.findall('.//phone')
            for phone in phones:
                phone_data = {
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
                data.append(phone_data)
    break

# 将数据存储到DataFrame中
df = pd.DataFrame(data)

# 将DataFrame保存为Excel文件
df.to_excel('output.xlsx', index=False)
