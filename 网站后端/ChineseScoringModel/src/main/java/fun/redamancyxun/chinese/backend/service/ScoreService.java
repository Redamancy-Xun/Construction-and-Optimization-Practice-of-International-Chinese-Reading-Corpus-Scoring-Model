package fun.redamancyxun.chinese.backend.service;

import fun.redamancyxun.chinese.backend.controller.socre.request.AudioScoreRequest;
import fun.redamancyxun.chinese.backend.controller.socre.response.AudioScoreResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 评分相关接口
 * @author Redamancy
 * @description 评分相关接口
 * @createDate 2024-11-3 22:39:04
 */
public interface ScoreService {

    // 将字节流byte[]保存为pcm文件
    String byte2pcm(byte[] data);

    // 利用pcm文件路径和文本内容生成音频-文本对照表
    void pcm2xlsx(String pcmPath, String text);

    // 利用讯飞模型，量化pcm音频数据为xml数据
    void pcm2xml() throws Exception;

    // 利用python脚本，将xml文件转化为txt
    void xml2txt();

    // 利用训练好的模型，整合信息并得出音频得分
    AudioScoreResponse score(AudioScoreRequest audioScoreRequest) throws Exception;

}
