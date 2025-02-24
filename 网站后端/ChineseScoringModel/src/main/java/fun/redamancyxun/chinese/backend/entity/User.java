package fun.redamancyxun.chinese.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel("user 用户信息")
@TableName(value ="user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    @ApiModelProperty("id")
    @TableId(value = "id")
    private String id;

    @ApiModelProperty("密码")
    @TableField(value = "password")
    private String password;

    @ApiModelProperty("用户名")
    @TableField(value = "username")
    private String username;

    @ApiModelProperty("性别(0男，1女，2其他)")
    @TableField(value = "gender")
    private Integer gender;

    @ApiModelProperty("年龄")
    @TableField(value = "age")
    private Integer age;

    @ApiModelProperty("国籍")
    @TableField(value = "nation")
    private String nation;

    @ApiModelProperty("手机号")
    @TableField(value = "telephone")
    private String telephone;

    @ApiModelProperty("头像")
    @TableField(value = "portrait")
    private String portrait;

    @ApiModelProperty("身份(0是学生，1是老师)")
    @TableField(value = "role")
    private Integer role;

    @ApiModelProperty("发音特点、学习建议")
    @TableField(value = "advice")
    private String advice;


}