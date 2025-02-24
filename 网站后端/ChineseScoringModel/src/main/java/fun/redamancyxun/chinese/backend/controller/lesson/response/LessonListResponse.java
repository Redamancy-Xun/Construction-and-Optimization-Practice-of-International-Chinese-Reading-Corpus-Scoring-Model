package fun.redamancyxun.chinese.backend.controller.lesson.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@ApiModel("课程列表信息")
public class LessonListResponse {

    @ApiModelProperty("单元总数")
    private Integer totalUnits;

    @ApiModelProperty("单元课程列表")
    private Map<String, Integer> lessons;
}
