package fun.redamancyxun.chinese.backend.controller.lesson;

import fun.redamancyxun.chinese.backend.common.Result;
import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonListResponse;
import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonDetailResponse;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.service.LessonService;
import fun.redamancyxun.chinese.backend.service.ScoreActionService;
import fun.redamancyxun.chinese.backend.util.SessionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "课程相关接口")
@RestController
@RequestMapping("/lesson")
@Slf4j
public class LessonController {

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private LessonService lessonService;

    /**
     * 获取课程列表
     * @return LessonListResponse
     */
    @GetMapping(value = "/getLessonList", produces = "application/json")
    @ApiOperation(value = "获取课程列表", response = LessonListResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bookNumber", value = "书籍编号", paramType = "query", dataType = "Integer")
    })
    public Result getLessonList(@RequestParam("bookNumber") Integer bookNumber) {
        try {
            return Result.success(lessonService.getLessonList(bookNumber));
        } catch (Exception e) {
            if (e instanceof MyException) {
                return Result.result(((MyException) e).getEnumExceptionType());
            }
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取具体课程信息
     * @return LessonDetailResponse
     */
    @GetMapping(value = "/getLessonDetail", produces = "application/json")
    @ApiOperation(value = "获取具体课程信息", response = LessonDetailResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unitId", value = "unitId", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "bookNumber", value = "书籍编号", paramType = "query", dataType = "Integer")
    })
    public Result getLessonDetail(@RequestParam("unitId") String unitId,
                                  @RequestParam("bookNumber") Integer bookNumber) {
        try {
            return Result.success(lessonService.getLessonDetail(unitId, bookNumber));
        } catch (Exception e) {
            if (e instanceof MyException) {
                return Result.result(((MyException) e).getEnumExceptionType());
            }
            return Result.fail(e.getMessage());
        }
    }
}
