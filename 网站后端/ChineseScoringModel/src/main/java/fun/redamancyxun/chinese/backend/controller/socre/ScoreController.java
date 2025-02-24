package fun.redamancyxun.chinese.backend.controller.socre;

import fun.redamancyxun.chinese.backend.common.Result;
import fun.redamancyxun.chinese.backend.controller.socre.request.AudioScoreRequest;
import fun.redamancyxun.chinese.backend.controller.socre.response.AudioScoreResponse;
import fun.redamancyxun.chinese.backend.controller.user.response.UserInfoResponse;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.service.ScoreActionService;
import fun.redamancyxun.chinese.backend.service.ScoreService;
import fun.redamancyxun.chinese.backend.util.SessionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Api(tags = "音频评分相关接口")
@RestController
@RequestMapping("/score")
@Slf4j
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreActionService scoreActionService;

    /**
     * 音频评分
     * @param audioFile
     * @param courseId
     * @param courseContent
     * @return audioScoreResponse
     */
    @PostMapping(value = "/score")
    @ApiOperation(value = "音频评分", response = AudioScoreResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "audio", value = "音频文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "bookId", value = "书籍id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "courseId", value = "文章id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "courseContent", value = "文章内容", required = true, dataType = "String")
    })
    public Result sore(@NotNull @RequestParam("audio") MultipartFile audioFile,
                       @NotNull @RequestParam("bookId") String bookId,
                       @NotNull @RequestParam("courseId") String courseId,
                       @NotNull @RequestParam("courseContent") String courseContent) {
        try {
            AudioScoreRequest audioScoreRequest = new AudioScoreRequest(audioFile, bookId, courseId, courseContent);
            Result result = Result.success(scoreService.score(audioScoreRequest));
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof MyException) {
                return Result.result(((MyException) e).getEnumExceptionType(), ((MyException) e).getErrorMsg());
            }
            return Result.fail(e.getMessage());
        }
    }

}
