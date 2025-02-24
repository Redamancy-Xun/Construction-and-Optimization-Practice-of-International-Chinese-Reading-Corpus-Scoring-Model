package fun.redamancyxun.chinese.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.time.OffsetDateTime;

@ApiModel("score_action 评分记录")
@TableName(value ="score_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreAction {

    @Id
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("受评用户id")
    @TableField(value = "user_id")
    private String userId;

    @ApiModelProperty("受评书籍id")
    @TableField(value = "book_id")
    private String bookId;

    @ApiModelProperty("受评文本id")
    @TableField(value = "text_id")
    private String textId;

    @ApiModelProperty("语速")
    @TableField(value = "speed")
    private Integer speed;

    @ApiModelProperty("停顿")
    @TableField(value = "pause")
    private Integer pause;

    @ApiModelProperty("声母")
    @TableField(value = "initialConsonants")
    private Integer initialConsonants;

    @ApiModelProperty("韵母")
    @TableField(value = "finalVowels")
    private Integer finalVowels;

    @ApiModelProperty("声调")
    @TableField(value = "tones")
    private Integer tones;

    @ApiModelProperty("完整度")
    @TableField(value = "completeness")
    private Integer completeness;

    @ApiModelProperty("评语&改进建议")
    @TableField(value = "advice")
    private String advice;
}
