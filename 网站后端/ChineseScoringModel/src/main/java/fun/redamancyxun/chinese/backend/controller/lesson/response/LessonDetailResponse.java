package fun.redamancyxun.chinese.backend.controller.lesson.response;

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
@ApiModel("课程具体信息")
public class LessonDetailResponse {

    @ApiModelProperty("课程归属单元")
    private String unitId;

    @ApiModelProperty("课程具体内容")
    private String text;
}
