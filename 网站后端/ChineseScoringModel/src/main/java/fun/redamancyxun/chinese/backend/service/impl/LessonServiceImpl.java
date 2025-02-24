package fun.redamancyxun.chinese.backend.service.impl;

import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonDetailResponse;
import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonListResponse;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.service.LessonService;
import fun.redamancyxun.chinese.backend.util.ExcelReader;
import fun.redamancyxun.chinese.backend.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课程相关接口实现
 * @author Redamancy
 * @description 课程相关接口实现
 * @createDate 2024-10-16 22:39:04
 */
@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * 获取课程详情
     * @param unitId
     * @return LessonDetailResponse
     * @throws MyException
     */
    @Override
    public LessonDetailResponse getLessonDetail(String unitId, Integer bookNumber) throws MyException {
        sessionUtils.refreshData(null);
        ExcelReader excelReader = ExcelReader.getInstance(bookNumber);
        String text = excelReader.getDataMap().get(unitId);
        if (text == null) {
            throw new MyException(EnumExceptionType.TEXT_NOT_EXIST);
        }
        return LessonDetailResponse.builder()
                .text(text)
                .unitId(unitId)
                .build();

    }

    /**
     * 获取课程列表
     * @return LessonListResponse
     * @throws MyException
     */
    @Override
    public LessonListResponse getLessonList(Integer bookNumber) throws MyException {
        ExcelReader excelReader = ExcelReader.getInstance(bookNumber);
        sessionUtils.refreshData(null);

        return LessonListResponse.builder()
                .lessons(excelReader.getCategoryCountMap())
                .totalUnits(excelReader.getUnitCount())
                .build();
    }


}
