package fun.redamancyxun.chinese.backend.controller.user.request;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@ApiModel("更新用户信息请求")
public class UpdateUserInfoRequest {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("性别(0男,1女)")
    private Integer gender;

    @ApiModelProperty("年龄")
    @TableField(value = "age")
    private Integer age;

    @ApiModelProperty("国籍")
    private String nation;
}
