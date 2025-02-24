package fun.redamancyxun.chinese.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.redamancyxun.chinese.backend.controller.user.request.UpdateUserInfoRequest;
import fun.redamancyxun.chinese.backend.controller.user.response.UserInfoResponse;
import fun.redamancyxun.chinese.backend.entity.User;
import fun.redamancyxun.chinese.backend.exception.MyException;
import org.springframework.web.multipart.MultipartFile;

/**
*  针对表【user(用户信息)】的数据库操作Service
*/
public interface UserService extends IService<User> {

    // 登录
    UserInfoResponse login(String telephone, String password, Integer role) throws Exception;

    // 检测登录状态
    Integer checkLogin() throws MyException;

    // 注销
    UserInfoResponse logout();

    // 注册
    UserInfoResponse signup(String telephone, String password, Integer role) throws MyException;

    // 获取用户信息
    UserInfoResponse getUserInfo();

    // 更新用户信息
    UserInfoResponse updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest);

    // 更新一个用户的“发育特点、学习建议”
    UserInfoResponse updateOneAdvice(String userId);

    // 更新当前用户的“发育特点、学习建议”
    UserInfoResponse updateOneAdvice();

    // 更新所有用户的“发育特点、学习建议”
    UserInfoResponse updateAllAdvice();

    // 修改密码
    UserInfoResponse changePassword(String oldPassword, String newPassword);

    // 上传用户头像
    String uploadPortrait(MultipartFile file);

//    void deleteUser();

    User getUserById(String userId);
}
