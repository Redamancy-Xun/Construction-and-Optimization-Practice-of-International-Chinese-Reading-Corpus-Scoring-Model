package fun.redamancyxun.chinese.backend.controller.socre.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import fun.redamancyxun.chinese.backend.service.ScoreService;
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
@ApiModel("模型评分结果")
public class AudioScoreResponse {

    @ApiModelProperty("语速")
    private Integer speed;

    @ApiModelProperty("停顿")
    private Integer pause;

    @ApiModelProperty("声母")
    private Integer initialConsonants;

    @ApiModelProperty("韵母")
    private Integer finalVowels;

    @ApiModelProperty("声调")
    private Integer tones;

    @ApiModelProperty("完整度")
    private Integer completeness;

    @ApiModelProperty("评语&改进建议")
    private String advice;
}
