package fun.redamancyxun.chinese.backend.service;

import com.baomidou.mybatisplus.annotation.TableField;
import fun.redamancyxun.chinese.backend.entity.ScoreAction;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 评分操作相关接口
 * @author Redamancy
 * @description 评分行为相关接口
 * @createDate 2024-10-16 22:39:04
 */
public interface ScoreActionService {

    // 创建评分行为
    ScoreAction createScoreAction(String userId,String bookId, String textId, Integer speed, Integer pause, Integer initialConsonants, Integer finalVowels, Integer tones, Integer completeness, String advice);

    // 查找用户的评分记录
    List<ScoreAction> findScoreActionByUserId(String userId);
}
