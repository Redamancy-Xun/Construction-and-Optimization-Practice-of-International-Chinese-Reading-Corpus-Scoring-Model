import os
import websocket
import datetime
import hashlib
import base64
import hmac
import json
from urllib.parse import urlencode
import time
import ssl
from wsgiref.handlers import format_date_time
from datetime import datetime
from time import mktime
import _thread as thread
import csv
import xml.etree.ElementTree as ET
# -*- coding: utf-8 -*-

STATUS_FIRST_FRAME = 0  # 第一帧的标识
STATUS_CONTINUE_FRAME = 1  # 中间帧标识
STATUS_LAST_FRAME = 2  # 最后一帧的标识

# BusinessArgs参数常量
SUB = "ise"
ENT = "cn_vip"
CATEGORY = "read_sentence"

# 读取音频-文本对照表.csv
def read_audio_text_mapping():
    audio_text_mapping = {}
    with open('cn/音频-文本对照表.csv', mode='r', encoding='utf-8') as csv_file:
        reader = csv.reader(csv_file)
        next(reader)  # Skip header
        for row in reader:
            audio_text_mapping[row[0]] = row[1]
    return audio_text_mapping

class Ws_Param(object):
    # 构造函数
    def __init__(self, APPID, APIKey, APISecret, AudioFile, Text):
        self.APPID = APPID
        self.APIKey = APIKey
        self.APISecret = APISecret
        self.AudioFile = AudioFile
        self.Text = Text

        # 公共参数(commonArgs)
        self.CommonArgs = {"app_id": self.APPID}
        # 业务参数(businessArgs)，更多个性化参数可在官网查看
        self.BusinessArgs = {"category": CATEGORY, "sub": SUB, "ent": ENT, "cmd": "ssb", "auf": "audio/L16;rate=16000",
                             "aue": "raw", "text": self.Text, "ttp_skip": True, "aus": 1}

    # 生成url
    def create_url(self):
        # 初始化 url 为讯飞ISE API的基本地址
        url = 'ws://ise-api.xfyun.cn/v2/open-ise'
        # 获取时间信息
        now = datetime.now()
        date = format_date_time(mktime(now.timetuple()))

        # 创建请求头信息
        signature_origin = "host: " + "ise-api.xfyun.cn" + "\n"
        signature_origin += "date: " + date + "\n"
        signature_origin += "GET " + "/v2/open-ise " + "HTTP/1.1"
        # 使用 HMAC-SHA256 算法根据 API 密钥对签名原文进行加密，生成签名值
        signature_sha = hmac.new(self.APISecret.encode('utf-8'), signature_origin.encode('utf-8'),
                                 digestmod=hashlib.sha256).digest()
        signature_sha = base64.b64encode(signature_sha).decode(encoding='utf-8')

        # 创建授权信息 authorization_origin：包含了使用的算法、请求头信息和签名值等信息
        authorization_origin = "api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"" % (
            self.APIKey, "hmac-sha256", "host date request-line", signature_sha)
        authorization = base64.b64encode(authorization_origin.encode('utf-8')).decode(encoding='utf-8')

        # 构建 URL
        v = {
            "authorization": authorization,
            "date": date,
            "host": "ise-api.xfyun.cn"
        }
        url = url + '?' + urlencode(v)

        return url

# 收到websocket消息的处理
def on_message(ws, message):
    try:
        code = json.loads(message)["code"]
        sid = json.loads(message)["sid"]
        # 错误处理
        if code != 0:
            errMsg = json.loads(message)["message"]
            print("sid:%s call error:%s code is:%s" % (sid, errMsg, code))
        else:
            data = json.loads(message)["data"]
            status = data["status"]
            result = data["data"]
            if (status == 2):
                xml_data = base64.b64decode(result).decode("gbk")
                root = ET.fromstring(xml_data)
                # 将XML保存到文件中，使用UTF-8编码
                pcm_filename = os.path.basename(wsParam.AudioFile)
                xml_filename = os.path.splitext(pcm_filename)[0] + '.xml'
                xml_filepath = os.path.join('cn', xml_filename)
                with open(xml_filepath, "w", encoding="utf-8") as f:
                    f.write(xml_data)
                print(f"XML result saved as {xml_filepath}")

    except Exception as e:
        print("receive msg, but parse exception:", e)

# 收到websocket错误的处理
def on_error(ws, error):
    print("### error:", error)

# 收到websocket关闭的处理
def on_close(ws):
    print("### closed ###")

# 收到websocket连接建立的处理
def on_open(ws):
    def run(*args):
        frameSize = 1280  # 帧大小
        intervel = 0.04  # 帧间隔（不修改）
        status = STATUS_FIRST_FRAME  # 状态

        with open(wsParam.AudioFile, "rb") as fp:
            while True:
                # 读取
                buf = fp.read(frameSize)
                # 读取为空处理
                if not buf:
                    status = STATUS_LAST_FRAME
                # 第一帧处理
                if status == STATUS_FIRST_FRAME:
                    d = {"common": wsParam.CommonArgs,
                         "business": wsParam.BusinessArgs,
                         "data": {"status": 0}}
                    d = json.dumps(d)
                    ws.send(d)
                    status = STATUS_CONTINUE_FRAME
                # 连续帧处理
                elif status == STATUS_CONTINUE_FRAME:
                    d = {"business": {"cmd": "auw", "aus": 2, "aue": "raw"},
                         "data": {"status": 1, "data": str(base64.b64encode(buf).decode())}}
                    ws.send(json.dumps(d))
                # 最后一帧处理
                elif status == STATUS_LAST_FRAME:
                    d = {"business": {"cmd": "auw", "aus": 4, "aue": "raw"},
                         "data": {"status": 2, "data": str(base64.b64encode(buf).decode())}}
                    ws.send(json.dumps(d))
                    time.sleep(1)  # 不修改
                    break
                time.sleep(intervel)
        ws.close()

    thread.start_new_thread(run, ())

if __name__ == "__main__":
    audio_text_mapping = read_audio_text_mapping()

    for audio_file, text in audio_text_mapping.items():
        wsParam = Ws_Param(APPID='4884d8d1', APISecret='MGJlOThkM2Q1Njc1YzcxODliMTQ4ZWZi',
                           APIKey='269cba2b9e41326b0303e1a1b36b208b',
                           AudioFile=os.path.join('cn', audio_file + '.pcm'), Text='\uFEFF' + text)
        # 禁用 WebSocket 的调试跟踪信息，避免在控制台输出大量额外信息
        websocket.enableTrace(False)
        wsUrl = wsParam.create_url()
        # 创建一个 WebSocketApp 对象 ws，指定连接的 URL、消息处理函数 on_message、错误处理函数 on_error 和关闭处理函数 on_close
        ws = websocket.WebSocketApp(wsUrl, on_message=on_message, on_error=on_error, on_close=on_close)
        ws.on_open = on_open
        # 通过 WebSocket 连接运行主循环，同时通过参数 sslopt 设置 SSL 选项，这里 cert_reqs 参数表示对对方的 SSL 证书进行验证
        ws.run_forever(sslopt={"cert_reqs": ssl.CERT_NONE})