package ise_demo_java;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;



/**
 * 语音评测 WebAPI 接口调用示例
 * 
 * 运行方法：直接运行 main() 即可
 * 
 * 结果： 控制台输出语音评测结果信息
 * 
 * @author iflytek
 * 
 */
public class ise {
	// 合成webapi接口地址
	private static final String WEBISE_URL = "http://api.xfyun.cn/v1/service/v1/ise";
	// 应用ID
	private static final String APPID = "**";
	// 接口密钥
	private static final String API_KEY = "*******";
	// 评测文本
	private static final String TEXT = "";
	// 音频编码
	private static final String AUE = "raw";
	// 采样率
	private static final String AUF = "audio/L16;rate=16000";
	// 结果级别
	private static final String RESULT_LEVEL = "entirety";
	// 语种
	private static final String LANGUAGE = "zh_cn";
	// 评测种类
	private static final String CATEGORY = "read_sentence";
	// 音频文件地址
	private static final String AUDIO_PATH = "音频路径";

	/**
	 * 评测 WebAPI 调用示例程序
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Map<String, String> header = buildHttpHeader();
		byte[] audioByteArray = FileUtil.read(AUDIO_PATH);
		String audioBase64 = new String(Base64.encodeBase64(audioByteArray), "UTF-8");
		String result = HttpUtil.doPost1(WEBISE_URL, header, "audio=" + URLEncoder.encode(audioBase64, "UTF-8") + "&text=" + URLEncoder.encode(TEXT, "UTF-8"));
		System.out.println("评测 WebAPI 接口调用结果：" + result);

	}

	/**
	 * 组装http请求头
	 */
	private static Map<String, String> buildHttpHeader() throws UnsupportedEncodingException {
		String curTime = System.currentTimeMillis() / 1000L + "";
		String param = "{\"auf\":\"" + AUF + "\",\"aue\":\"" + AUE + "\",\"result_level\":\"" + RESULT_LEVEL + "\",\"language\":\"" + LANGUAGE + "\",\"category\":\"" + CATEGORY + "\"}";
		String paramBase64 = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
		String checkSum = DigestUtils.md5Hex(API_KEY + curTime + paramBase64);
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		header.put("X-Param", paramBase64);
		header.put("X-CurTime", curTime);
		header.put("X-CheckSum", checkSum);
		header.put("X-Appid", APPID);
		return header;
	}
}
