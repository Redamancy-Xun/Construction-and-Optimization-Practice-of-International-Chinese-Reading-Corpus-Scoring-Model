package fun.redamancyxun.chinese.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.redamancyxun.chinese.backend.entity.ScoreAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description 针对表【score_action(练习与评价记录)】的数据库操作Mapper
 */
@Mapper
public interface ScoreActionMapper extends BaseMapper<ScoreAction> {

    /**
     * 查询历史练习与评价记录
     * @param userId 用户id
     * @return List<ScoreAction>
     */
    @Select("SELECT * FROM score_action WHERE user_id = #{userId}")
    List<ScoreAction> selectList(String userId);
}
