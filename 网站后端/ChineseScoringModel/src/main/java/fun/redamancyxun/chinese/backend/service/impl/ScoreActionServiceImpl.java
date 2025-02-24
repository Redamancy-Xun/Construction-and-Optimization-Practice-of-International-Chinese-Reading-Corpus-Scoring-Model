package fun.redamancyxun.chinese.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.redamancyxun.chinese.backend.entity.ScoreAction;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.mapper.ScoreActionMapper;
import fun.redamancyxun.chinese.backend.service.ScoreActionService;
import fun.redamancyxun.chinese.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评分操作相关接口实现类
 * @author Redamancy
 * @description 评分行为相关接口实现类
 * @createDate 2024-10-16 22:39:04
 */
@Service
public class ScoreActionServiceImpl implements ScoreActionService {

    @Autowired
    private ScoreActionMapper scoreActionMapper;

    @Autowired
    private UserService userService;

    /**
     * 创建评分行为
     * @param userId 用户id
     * @param textId 文章id
     * @param speed 语速
     * @param pause 停顿
     * @param initialConsonants 声母
     * @param finalVowels 韵母
     * @param tones 声调
     * @param completeness 完整度
     * @param advice 评语&改进建议
     * @return 评分行为实体
     */
    @Override
    public ScoreAction createScoreAction(String userId,String bookId, String textId, Integer speed, Integer pause, Integer initialConsonants, Integer finalVowels, Integer tones, Integer completeness, String advice) {

        ScoreAction scoreAction = ScoreAction.builder()
                .userId(userId)
                .bookId(bookId)
                .textId(textId)
                .speed(speed)
                .pause(pause)
                .initialConsonants(initialConsonants)
                .finalVowels(finalVowels)
                .tones(tones)
                .completeness(completeness)
                .advice(advice)
                .build();

        if (scoreActionMapper.insert(scoreAction) == 0) {
            throw new MyException(EnumExceptionType.INSERT_FAILED);
        }

        return scoreAction;
    }

    /**
     * 根据用户id查找评分行为
     * @param userId 用户id
     * @return 评分行为列表
     */
    @Override
    public List<ScoreAction> findScoreActionByUserId(String userId) {

        if (userId == null) {
            throw new MyException(EnumExceptionType.DATA_IS_NULL);
        }
        if (userService.getUserById(userId) == null) {
            throw new MyException(EnumExceptionType.USER_NOT_EXIST);
        }

        return scoreActionMapper.selectList(new QueryWrapper<ScoreAction>().eq("user_id", userId));
    }
}
