package fun.redamancyxun.chinese.backend.dto;

import fun.redamancyxun.chinese.backend.entity.ScoreAction;
import fun.redamancyxun.chinese.backend.entity.User;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * session缓存实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("SessionData 会话实体")
public class SessionData  implements Serializable {

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("性别(0男,1女)")
    private Integer gender;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("国籍")
    private String nation;

    @ApiModelProperty("手机号")
    private String telephone;

    @ApiModelProperty("头像")
    private String portrait;

    @ApiModelProperty("身份(0是学生，1是老师)")
    private Integer role;

    @ApiModelProperty("发音特点、学习建议")
    private String advice;

    @ApiModelProperty("历史练习与评价记录")
    private List<ScoreAction> history;


    public SessionData(User user, List<ScoreAction> history) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.gender = user.getGender();
        this.age = user.getAge();
        this.nation = user.getNation();
        this.telephone = user.getTelephone();
        this.portrait = user.getPortrait();
        this.role = user.getRole();
        this.advice = user.getAdvice();

        this.history = history;
    }

    public SessionData(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.gender = user.getGender();
        this.age = user.getAge();
        this.nation = user.getNation();
        this.telephone = user.getTelephone();
        this.portrait = user.getPortrait();
        this.role = user.getRole();
        this.advice = user.getAdvice();

        this.history = null;
    }

}
