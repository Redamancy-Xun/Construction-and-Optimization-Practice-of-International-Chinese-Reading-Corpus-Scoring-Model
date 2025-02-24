package fun.redamancyxun.chinese.backend.service;

import fun.redamancyxun.chinese.backend.entity.AdviceAction;

import java.util.List;

/**
 * 建议操作相关接口
 * @author Redamancy
 * @description 建议行为相关接口
 * @createDate 2024-11-9 22:39:04
 */
public interface AdviceActionService {

    // 创建一条建议记录
    AdviceAction createAdviceAction(String userId, String advice);

    // 根据用户id获取该用户的建议记录
    List<AdviceAction> findAdviceActionByUserId(String userId);
}
