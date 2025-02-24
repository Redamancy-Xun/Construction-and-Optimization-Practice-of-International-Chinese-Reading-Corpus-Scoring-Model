package fun.redamancyxun.chinese.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.redamancyxun.chinese.backend.entity.AdviceAction;
import fun.redamancyxun.chinese.backend.entity.User;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.mapper.AdviceActionMapper;
import fun.redamancyxun.chinese.backend.service.AdviceActionService;
import fun.redamancyxun.chinese.backend.service.UserService;
import fun.redamancyxun.chinese.backend.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 建议操作相关接口
 * @author Redamancy
 * @description 建议行为相关接口
 * @createDate 2024-11-9 22:39:04
 */
@Service
public class AdviceActionImpl implements AdviceActionService {

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceActionMapper adviceActionMapper;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * 创建建议行为
     * @param userId 用户id
     * @param advice 建议内容
     * @return AdviceAction
     * @throws MyException 自定义异常
     */
    @Override
    public AdviceAction createAdviceAction(String userId, String advice) throws MyException {

        // 获取用户信息
        User user = userService.getUserById(userId);
//        sessionUtils.refreshData(user);

        AdviceAction adviceAction = AdviceAction.builder()
                .userId(user.getId())
                .advice(advice)
                .createTime(LocalDateTime.now())
                .build();

        if (adviceActionMapper.insert(adviceAction) == 0) {
            throw new MyException(EnumExceptionType.INSERT_FAILED);
        }

        return adviceAction;
    }

    /**
     * 根据用户id查找建议行为
     * @param userId 用户id
     * @return AdviceAction
     * @throws MyException 自定义异常
     */
    @Override
    public List<AdviceAction> findAdviceActionByUserId(String userId) throws MyException {

        // 获取用户信息
        User user = userService.getUserById(userId);
//        sessionUtils.refreshData(user);

        QueryWrapper<AdviceAction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        return adviceActionMapper.selectList(queryWrapper);
    }
}
