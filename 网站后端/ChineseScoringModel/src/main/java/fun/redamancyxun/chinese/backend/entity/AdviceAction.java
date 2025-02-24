package fun.redamancyxun.chinese.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.time.LocalDateTime;

@ApiModel("advice_action “发育特点、学习建议”_记录")
@TableName(value ="advice_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdviceAction {

    @Id
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("受评用户id")
    @TableField(value = "user_id")
    private String userId;

    @ApiModelProperty("发育特点、学习建议")
    @TableField(value = "advice")
    private String advice;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
