package fun.redamancyxun.chinese.backend.controller.socre.request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@ApiModel("音频上传请求")
public class AudioScoreRequest {

    @ApiModelProperty("音频文件")
    private MultipartFile audioFile;

    @ApiModelProperty("书籍id")
    private String bookId;

    @ApiModelProperty("文章id")
    private String courseId;

    @ApiModelProperty("文章内容")
    private String courseContent;
}
