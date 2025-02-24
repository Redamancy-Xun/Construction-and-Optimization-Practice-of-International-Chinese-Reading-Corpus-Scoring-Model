package fun.redamancyxun.chinese.backend.service;

import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonDetailResponse;
import fun.redamancyxun.chinese.backend.controller.lesson.response.LessonListResponse;

/**
 * 课程相关接口
 * @author Redamancy
 * @description 课程相关接口
 * @createDate 2024-10-16 22:39:04
 */
public interface LessonService {

    // 获取课程详情
    LessonDetailResponse getLessonDetail(String unitId, Integer bookNumber);

    // 获取课程列表
    LessonListResponse getLessonList(Integer bookNumber);
}
