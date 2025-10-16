package fun.redamancyxun.chinese.backend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fun.redamancyxun.chinese.backend.config.YmlConfig;
import fun.redamancyxun.chinese.backend.controller.user.request.UpdateUserInfoRequest;
import fun.redamancyxun.chinese.backend.controller.user.response.UserInfoResponse;
import fun.redamancyxun.chinese.backend.dto.SessionData;
import fun.redamancyxun.chinese.backend.entity.ScoreAction;
import fun.redamancyxun.chinese.backend.entity.User;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.mapper.UserMapper;
import fun.redamancyxun.chinese.backend.service.AdviceActionService;
import fun.redamancyxun.chinese.backend.service.ScoreActionService;
import fun.redamancyxun.chinese.backend.service.UserService;
import fun.redamancyxun.chinese.backend.util.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static fun.redamancyxun.chinese.backend.common.CommonConstants.*;

/**
* @description 针对表【user(用户信息)】的数据库操作Service实现
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    SessionUtils sessionUtils;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ScoreActionService scoreActionService;

    @Autowired
    AdviceActionService adviceActionService;

    @Autowired
    YmlConfig ymlConfig;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private HttpServletRequest request;

    /**
     * 注册
     * @return UserInfoResponse
     * @throws MyException 通用异常
     */
    @Override
    //@Transactional 注解标记该方法需要进行事务管理，在出现 MyException 异常时进行回滚操作
    @Transactional(rollbackFor = MyException.class)
    public UserInfoResponse signup(String telephone, String password, Integer role) throws MyException {

        // 如果手机号已经被使用，则报错提醒
        QueryWrapper<User> query Wrapper = new QueryWrapper<>();
        queryWrapper.eq("telephone", telephone);
        if (userMapper.selectCount(queryWrapper) != 0){
            throw new MyException(EnumExceptionType.TELEPHONE_EXIST);
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString().substring(0, 8))
                .role(role)
                .username("小吉祥" + UUID.randomUUID().toString().substring(0, 4))
                .portrait(IMAGE_PATH + "defaultAvatar.svg")
                .gender(2)
                .advice("在中文的世界里，每一个字都是一颗星星，每一次发音都是一次星际旅行。虽然旅程漫长，但每一步的探索都是通向光辉的过程。继续前行，书写属于你的星辰大海。")
                .age(0)
                .nation("吉祥国")
                .password(password)
                .telephone(telephone)
                .build();

        //调用 userMapper 的 insert 方法将用户信息插入到数据库中
        if (userMapper.insert(user) == 0){
            throw new MyException(EnumExceptionType.INSERT_FAILED);
        }

        //生成 sessionId 和 sessionData，分别存入 sessionUtils 和 redisUtils 中，设置过期时间为 86400 秒
        String sessionId = sessionUtils.generateSessionId();
        SessionData sessionData = new SessionData(user);
        sessionUtils.setSessionId(sessionId);
        redisUtils.set(user.getId(), sessionId, 86400);
        redisUtils.set(sessionId, sessionData, 86400);

        return new UserInfoResponse(user, sessionId);
    }

    /**
     * 登录
     * @param telephone 手机号
     * @param password 密码
     * @param role 角色
     * @return LoginInfo
     * @throws Exception 异常
     */
    @Override
    public UserInfoResponse login(String telephone, String password, Integer role) throws Exception {;

        // 构造一个 QueryWrapper 用于查询用户信息，根据 telephone 从数据库中查询对应的用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("telephone", telephone);
        userQueryWrapper.eq("role", role);
        User user = userMapper.selectOne(userQueryWrapper);

        // 如果用户不存在，则报错提醒
        if (user == null){
            throw new MyException(EnumExceptionType.USER_NOT_EXIST);
        }

        // 如果密码不正确，则报错提醒
        if (!user.getPassword().equals(password)){
            throw new MyException(EnumExceptionType.PASSWORD_ERROR);
        }

        //生成 sessionId 和 sessionData，分别存入 sessionUtils 和 redisUtils 中，设置过期时间为 86400 秒
        String sessionId = sessionUtils.generateSessionId();
        SessionData sessionData = new SessionData(user);
        sessionUtils.setSessionId(sessionId);
        redisUtils.set(user.getId(), sessionId, 86400);
        redisUtils.set(sessionId, sessionData, 86400);

        //最后构造一个 LoginInfo 对象
        return new UserInfoResponse(user, sessionId);
    }

    /**
     * 根据cookie检测登录状况
     * @return -1 会话过期，0 表示未登录，1 表示学生，2 表示老师，3 表示管理员，4 表示超级管理员
     * @throws MyException 通用异常
     */
    @Override
    public Integer checkLogin() throws MyException {

        //从请求头中获得 sessionId
        String key = request.getHeader("session");

        // 如果请求头中没有 sessionId，则返回未登录
        if (key == null || !redisUtils.hasKey(key)) {
            return 0;
        }
        // 如果 sessionId 过期，则删除 sessionId，并返回会话过期
        if (redisUtils.isExpire(key)) {
            redisUtils.del(key);
            return -1;
        }

        // 刷新 sessionData
        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);
        // 判断用户角色
        if (user.getRole() == 0) {
            return 1;
        }
        if (user.getRole() == 1) {
            return 2;
        }

        return 4;
    }

    /**
     * 注销
     *
     * @return UserInfoResponse
     */
    @Override
    public UserInfoResponse logout() {

//        // 获取当前用户的 userId
//        String userId = sessionUtils.getUserId();
//
//        // 删除 redis 中的 sessionId 和 sessionData
//        redisUtils.del(userId);
//        redisUtils.del(sessionUtils.getSessionId());

        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.invalidate();
        return new UserInfoResponse(user);
    }


    /**
     * 获取用户信息
     * @return UserInfoResponse
     * @throws MyException 通用异常
     */
    @Override
    public UserInfoResponse getUserInfo() {

        // 获取当前用户的 userId
        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);

        // 获取用户的历史练习与评价记录（ScoreAction）
        QueryWrapper<ScoreAction> scoreActionQueryWrapper = new QueryWrapper<>();
        scoreActionQueryWrapper.eq("user_id", user.getId());
        List<ScoreAction> history = scoreActionService.findScoreActionByUserId(user.getId());

        //最后构造一个 LoginInfo 对象
        return new UserInfoResponse(user, history);
    }

    /**
     * 更新用户信息
     * @param userSignupRequest 用户信息
     * @return UserInfoResponse
     * @throws MyException 通用异常
     */
    @Override
    public UserInfoResponse updateUserInfo(UpdateUserInfoRequest userSignupRequest) {

        // 获取当前用户的 userId
        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);

        if (userSignupRequest.getAge() != null) {
            user.setAge(userSignupRequest.getAge());
        }
        if (userSignupRequest.getGender() != null) {
            user.setGender(userSignupRequest.getGender());
        }
        if (userSignupRequest.getNation() != null) {
            user.setNation(userSignupRequest.getNation());
        }
        if (userSignupRequest.getUsername() != null) {
            user.setUsername(userSignupRequest.getUsername());
        }

        if (userMapper.updateById(user) == 0){
            throw new MyException(EnumExceptionType.UPDATE_FAILED);
        }

        return new UserInfoResponse(user);
    }

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return UserInfoResponse
     * @throws MyException 通用异常
     */
    @Override
    public UserInfoResponse changePassword(String oldPassword, String newPassword) throws MyException {

        // 获取当前用户的 userId
        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);

        if (!user.getPassword().equals(oldPassword)){
            throw new MyException(EnumExceptionType.OLD_PASSWORD_ERROR);
        }

        user.setPassword(newPassword);

        if (userMapper.updateById(user) == 0){
            throw new MyException(EnumExceptionType.UPDATE_FAILED);
        }

        return new UserInfoResponse(user);
    }

    /**
     * 上传头像
     * @param file 头像文件
     * @return String
     * @throws MyException 通用异常
     */
    @Override
    public String uploadPortrait(MultipartFile file) throws MyException {

        // 获取当前用户的 userId
        User user = getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);

        if (user == null) {
            throw new MyException(EnumExceptionType.USER_NOT_EXIST);
        }
        String originalFilename = file.getOriginalFilename();
        String flag = IdUtil.fastSimpleUUID();
        String rootFilePath = USER_FILE_PATH + flag + "-" + originalFilename;
        try {
            FileUtil.writeBytes(file.getBytes(), rootFilePath);
        } catch (IOException e) {
            throw new MyException(EnumExceptionType.READ_FILE_ERROR);
        }
        String link = IMAGE_PATH + flag + "-" + originalFilename;
        user.setPortrait(link);

        if (userMapper.updateById(user) == 0) {
            throw new MyException(EnumExceptionType.UPDATE_FAIL);
        }

        return link;
    }

    /**
     * 根据userId获取用户信息
     * @param userId 用户id
     * @return  User
     * @throws MyException 通用异常
     */
    @Override
    public User getUserById(String userId) throws MyException{
        User user = userMapper.selectById(userId);
        sessionUtils.refreshData(null);
        if (user == null){
            throw new MyException(EnumExceptionType.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 更新一个用户的“发育特点、学习建议”
     * @param userId 用户id
     * @throws MyException 通用异常
     */
    @Override
//    @Async
    public UserInfoResponse updateOneAdvice(String userId) throws MyException {
        // 获取用户信息
        User user = getUserById(userId);
//        sessionUtils.refreshData(null);
        List<ScoreAction> history = scoreActionService.findScoreActionByUserId(userId);

        // 如果没有历史记录，则直接返回用户信息
        if (history.isEmpty()) {
            return new UserInfoResponse(user);
        }

        // 获取用户的最近十次评分记录
        List<String> scoreList = new ArrayList<>();
        for (int i = history.size() - 1; i > 0; i--) {
            if (scoreList.size() < 10) {
                String json = "{\"语速\": \"" + history.get(i).getSpeed() + "\",\"停顿\": \"" + history.get(i).getPause() + "\",\"声母\": \"" + history.get(i).getInitialConsonants() + "\",\"韵母\": \"" + history.get(i).getFinalVowels() + "\",\"声调\": \"" + history.get(i).getTones() + "\",\"完整度\": \"" + history.get(i).getCompleteness() + "\",\"评语&更改建议\": \"" + history.get(i).getAdvice() + "\"}";
                scoreList.add(json);
            }
        }

        // 构造一个示例数据
        List<String> exampleScoreList = new ArrayList<>();
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流利，声母韵母较为标准，有自行纠错意识，声调略有不足，朗读完成度高，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 4 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流利，声母韵母较为标准，有自行纠错意识，声调略有进步，出现了误读字。完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有进步，儿化音发音不够自然。完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流畅度不足，声母韵母较为标准，有自行纠错意识，声调略有不足，儿化音发音不够自然，完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母发音较标准，有自行纠错意识，声调略有不足，完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，儿化音发音较为自然。完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 2 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，有误读字，朗读完成度高，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 5 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读非常流利，声母韵母较为标准，声调基本准确，完成度高，表现出色！" + "\"}");
        // TODO 具体的示例待完善 提供给模型的历史记录待完善
        String exampleAdvice = "发音特点：整体来说朗读较为流利，完成度高。声母上存在n、l不分，r发音含混的问题，韵母发音情况较声母来说更好，儿化音有时发音不够自然。声调部分问题较为明显，存在四声调混淆的问题，需要多加注意与练习。整体朗读有节奏感，基本知道应该在何处停顿，但仍有对课文不够熟悉所以断句不恰当的情况，且朗读过程中有自行纠错意识，值得肯定。\n学习建议：可以多听课文的标准录音进行声韵调的模仿学习，在具体声母发音上可以询问任课教师，结合发音理论和实际教学经验来学会运用正确的发生部位发音。还可以多和中国语伴进行语言交流，听一听他们是如何断句的。需要多多复习所学，巩固朗读知识。";

        // 给出请求的数据信息
        String system = "你是一名经验丰富的对外汉语教师，专门负责根据留学生的近期中文口语成绩提供个性化的分析和建议。请根据留学生近期的中文口语成绩，深入分析学生的发音特点，并针对性地提出有效的学习建议。这些建议应考虑到学生的个体差异，既能帮助他们提升口语能力，又具有实际的参考价值和实现可能性。学生的口语成绩评分标准基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分。每个维度的评分标准和计算方式如下：{\"评分标准\":{\"准确度\":{\"声母/韵母\":{\"分类标准\":{\"正确率90%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"发音准确，容易理解，接近母语水平，正确率很高\",\"计算方式\":\"1-(声韵错误数/总音节数)\"},\"正确率70%-90%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"基本可理解，仅有少量错误\",\"计算方式\":\"1-(声韵错误数/总音节数)\"},\"正确率0%-70%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"发音模糊，听懂困难\",\"计算方式\":\"1-(声韵错误数/总音节数)\"}}},\"声调\":{\"分类标准\":{\"正确率90%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"声调准确，表达清晰\",\"计算方式\":\"调型错误数/总音节数\"},\"正确率70%-90%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"声调基本正确，偶有错误\",\"计算方式\":\"调型错误数/总音节数\"},\"正确率0%-70%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"声调错误频繁，难以理解\",\"计算方式\":\"调型错误数/总音节数\"}}}},\"流利度\":{\"语速\":{\"分类标准\":{\"每分钟120字及以上\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"语言流畅，富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\",\"计算方式\":\"字数/时间（秒）\"},\"每分钟60-120字\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"语言较为流畅，无意义的重复少、卡顿少，意识到错误能及时纠正\",\"计算方式\":\"字数/时间（秒）\"},\"每分钟60字以下\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\",\"计算方式\":\"字数/时间（秒）\"}}},\"停顿\":{\"分类标准\":{\"不合理停顿占比3%以内\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"语言流畅，停顿自然，富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"},\"不合理停顿3%-10%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"偶有停顿，整体较流畅，无意义的重复少、卡顿少，意识到错误能及时纠正\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"},\"不合理停顿超过10%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"停顿频繁，影响理解，磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"}}}},\"完整度\":{\"完整性\":{\"分类标准\":{\"朗读完成度95%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"内容完整，毫无缺漏\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"},\"朗读完成度85%-94.9%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"有个别字词遗漏\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"},\"朗读完成度84.9%及以下\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"大段内容遗漏，理解困难\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"}}}},\"感情\":{\"情感表达\":{\"分类标准\":{\"音节饱满，语调自然\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"表现出色，情感丰富\"},\"表现平稳，缺少情感\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"较为平淡，缺乏感染力\"},\"情感表达不佳，令人不适\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"缺乏真诚，难以倾听\"}}}}}}";
        String prompt = "我是一位学习中文口语的留学生，我近期中文口语成绩是：{" + scoreList + "}。口语成绩是基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分，并且有对应的评语&更改建议，在system身份下说明了具体的评分标准。你是一名经验丰富的对外汉语教师，专门负责根据留学生的近期中文口语成绩提供个性化的分析和建议。请根据我近期的中文口语成绩，深入分析我的发音特点，并针对性地提出有效的学习建议。这些建议应考虑到学生的个体差异，既能帮助我们提升口语能力，又具有实际的参考价值和实现可能性。示例：比如有一位留学生近期的中文口语成绩为：{" + exampleScoreList + "}，你的输出示例是：{" + exampleAdvice + "}。";

        System.out.println(prompt);

        // 调用模型API
        // API Key
        String apiKey = "5176fd66b5ebf072d3a4cb3cc7373e8b.GYxJkFGKPDaaMW3A";
        // API endpoint
        String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

        // Request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "glm-4-flash");

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", system);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);

        requestBody.add("messages", new Gson().toJsonTree(new JsonObject[]{systemMessage, userMessage}));

        // Create OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(3000, TimeUnit.SECONDS)    // 读取超时时间
                .writeTimeout(1005, TimeUnit.SECONDS)   // 写入超时时间
                .build();;

        // Create HTTP request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        // Execute request
        String responseBody = null;
        String responseAdvice = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    responseBody = response.body().string();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    responseAdvice = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
                }
                System.out.println("Response: " + responseBody);
                System.out.println("Advice: " + responseAdvice);
            } else {
                System.out.println("Request failed: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 创建ScoreAction
        adviceActionService.createAdviceAction(userId, responseAdvice);

        // 更新用户
        user.setAdvice(responseAdvice);
        if (userMapper.updateById(user) == 0) {
            throw new MyException(EnumExceptionType.UPDATE_FAILED);
        }

        return new UserInfoResponse(user);
    }

    /**
     * 更新当前用户的“发育特点、学习建议”
     * @throws MyException 通用异常
     */
    @Override
//    @Async
    public UserInfoResponse updateOneAdvice() throws MyException {
        // 获取用户信息
        String userId = sessionUtils.getUserId();
        User user = getUserById(userId);
//        sessionUtils.refreshData(null);
        List<ScoreAction> history = scoreActionService.findScoreActionByUserId(userId);

        // 如果没有历史记录，则直接返回用户信息
        if (history.isEmpty()) {
            return new UserInfoResponse(user);
        }

        // 获取用户的最近十次评分记录
        List<String> scoreList = new ArrayList<>();
        for (int i = history.size() - 1; i > 0; i--) {
            if (scoreList.size() < 10) {
                String json = "{\"语速\": \"" + history.get(i).getSpeed() + "\",\"停顿\": \"" + history.get(i).getPause() + "\",\"声母\": \"" + history.get(i).getInitialConsonants() + "\",\"韵母\": \"" + history.get(i).getFinalVowels() + "\",\"声调\": \"" + history.get(i).getTones() + "\",\"完整度\": \"" + history.get(i).getCompleteness() + "\",\"评语&更改建议\": \"" + history.get(i).getAdvice() + "\"}";
                scoreList.add(json);
            }
        }

        // 构造一个示例数据
        List<String> exampleScoreList = new ArrayList<>();
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流利，声母韵母较为标准，有自行纠错意识，声调略有不足，朗读完成度高，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 4 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流利，声母韵母较为标准，有自行纠错意识，声调略有进步，出现了误读字。完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有进步，儿化音发音不够自然。完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 3 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读流畅度不足，声母韵母较为标准，有自行纠错意识，声调略有不足，儿化音发音不够自然，完成度很好，继续努力！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母发音较标准，有自行纠错意识，声调略有不足，完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 4 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，儿化音发音较为自然。完成度很好，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 4 + "\",\"停顿\": \"" + 4 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 3 + "\",\"声调\": \"" + 2 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读较为流利，声母韵母较为标准，有自行纠错意识，声调略有不足，有误读字，朗读完成度高，表现出色！" + "\"}");
        exampleScoreList.add("{\"语速\": \"" + 5 + "\",\"停顿\": \"" + 5 + "\",\"声母\": \"" + 3 + "\",\"韵母\": \"" + 4 + "\",\"声调\": \"" + 3 + "\",\"完整度\": \"" + 5 + "\",\"评语&更改建议\": \"" + "朗读非常流利，声母韵母较为标准，声调基本准确，完成度高，表现出色！" + "\"}");
        // TODO 具体的示例待完善 提供给模型的历史记录待完善
        String exampleAdvice = "发音特点：整体来说朗读较为流利，完成度高。声母上存在n、l不分，r发音含混的问题，韵母发音情况较声母来说更好，儿化音有时发音不够自然。声调部分问题较为明显，存在四声调混淆的问题，需要多加注意与练习。整体朗读有节奏感，基本知道应该在何处停顿，但仍有对课文不够熟悉所以断句不恰当的情况，且朗读过程中有自行纠错意识，值得肯定。\n学习建议：可以多听课文的标准录音进行声韵调的模仿学习，在具体声母发音上可以询问任课教师，结合发音理论和实际教学经验来学会运用正确的发生部位发音。还可以多和中国语伴进行语言交流，听一听他们是如何断句的。需要多多复习所学，巩固朗读知识。";

        // 给出请求的数据信息
        String system = "你是一名经验丰富的对外汉语教师，专门负责根据留学生的近期中文口语成绩提供个性化的分析和建议。请根据留学生近期的中文口语成绩，深入分析学生的发音特点，并针对性地提出有效的学习建议。这些建议应考虑到学生的个体差异，既能帮助他们提升口语能力，又具有实际的参考价值和实现可能性。学生的口语成绩评分标准基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分。每个维度的评分标准和计算方式如下：{\"评分标准\":{\"准确度\":{\"声母/韵母\":{\"分类标准\":{\"正确率90%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"发音准确，容易理解，接近母语水平，正确率很高\",\"计算方式\":\"1-(声韵错误数/总音节数)\"},\"正确率70%-90%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"基本可理解，仅有少量错误\",\"计算方式\":\"1-(声韵错误数/总音节数)\"},\"正确率0%-70%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"发音模糊，听懂困难\",\"计算方式\":\"1-(声韵错误数/总音节数)\"}}},\"声调\":{\"分类标准\":{\"正确率90%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"声调准确，表达清晰\",\"计算方式\":\"调型错误数/总音节数\"},\"正确率70%-90%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"声调基本正确，偶有错误\",\"计算方式\":\"调型错误数/总音节数\"},\"正确率0%-70%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"声调错误频繁，难以理解\",\"计算方式\":\"调型错误数/总音节数\"}}}},\"流利度\":{\"语速\":{\"分类标准\":{\"每分钟120字及以上\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"语言流畅，富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\",\"计算方式\":\"字数/时间（秒）\"},\"每分钟60-120字\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"语言较为流畅，无意义的重复少、卡顿少，意识到错误能及时纠正\",\"计算方式\":\"字数/时间（秒）\"},\"每分钟60字以下\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\",\"计算方式\":\"字数/时间（秒）\"}}},\"停顿\":{\"分类标准\":{\"不合理停顿占比3%以内\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"语言流畅，停顿自然，富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"},\"不合理停顿3%-10%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"偶有停顿，整体较流畅，无意义的重复少、卡顿少，意识到错误能及时纠正\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"},\"不合理停顿超过10%\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"停顿频繁，影响理解，磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\",\"计算方式\":\"不合理停顿字数(fil)/总字数\"}}}},\"完整度\":{\"完整性\":{\"分类标准\":{\"朗读完成度95%-100%\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"内容完整，毫无缺漏\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"},\"朗读完成度85%-94.9%\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"有个别字词遗漏\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"},\"朗读完成度84.9%及以下\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"大段内容遗漏，理解困难\",\"计算方式\":\"1-((增读+漏读+回读)/总字数)\"}}}},\"感情\":{\"情感表达\":{\"分类标准\":{\"音节饱满，语调自然\":{\"等级\":\"A\",\"量化值(g)\":5,\"描述\":\"表现出色，情感丰富\"},\"表现平稳，缺少情感\":{\"等级\":\"B\",\"量化值(g)\":3,\"描述\":\"较为平淡，缺乏感染力\"},\"情感表达不佳，令人不适\":{\"等级\":\"C\",\"量化值(g)\":1,\"描述\":\"缺乏真诚，难以倾听\"}}}}}}";
        String prompt = "我是一位学习中文口语的留学生，我近期中文口语成绩是：{" + scoreList + "}。口语成绩是基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分，并且有对应的评语&更改建议，在system身份下说明了具体的评分标准。你是一名经验丰富的对外汉语教师，专门负责根据留学生的近期中文口语成绩提供个性化的分析和建议。请根据我近期的中文口语成绩，深入分析我的发音特点，并针对性地提出有效的学习建议。这些建议应考虑到学生的个体差异，既能帮助我们提升口语能力，又具有实际的参考价值和实现可能性。示例：比如有一位留学生近期的中文口语成绩为：{" + exampleScoreList + "}，你的输出示例是：{" + exampleAdvice + "}。";

        System.out.println(prompt);

        // 调用模型API
        // API Key
        String apiKey = "5176fd66b5ebf072d3a4cb3cc7373e8b.GYxJkFGKPDaaMW3A";
        // API endpoint
        String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

        // Request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "glm-4-flash");

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", system);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);

        requestBody.add("messages", new Gson().toJsonTree(new JsonObject[]{systemMessage, userMessage}));

        // Create OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(3000, TimeUnit.SECONDS)    // 读取超时时间
                .writeTimeout(1005, TimeUnit.SECONDS)   // 写入超时时间
                .build();;

        // Create HTTP request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        // Execute request
        String responseBody = null;
        String responseAdvice = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    responseBody = response.body().string();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    responseAdvice = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
                }
                System.out.println("Response: " + responseBody);
                System.out.println("Advice: " + responseAdvice);
            } else {
                System.out.println("Request failed: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 创建ScoreAction
        adviceActionService.createAdviceAction(userId, responseAdvice);

        // 更新用户
        user.setAdvice(responseAdvice);
        if (userMapper.updateById(user) == 0) {
            throw new MyException(EnumExceptionType.UPDATE_FAILED);
        }

        return new UserInfoResponse(user);
    }

    @Override
    public UserInfoResponse updateAllAdvice() {
        return null;
    }

//    /**
//     * 注销用户
//     */
//    @Override
//    public void deleteUser() {
//        String userId = sessionUtils.getUserId();
//        User user = userMapper.selectById(userId);
//
//        if (user.getRole() == 0) {
//            throw new MyException(EnumExceptionType.CAN_NOT_DELETE);
//        }
//
//        user.setUsername("用户" + UUID.randomUUID().toString().substring(0, 5));
//        user.setGender(2);
//        user.setPortrait(IMAGE_PATH + "ecnu_go.jpg");
//        user.setSignature("用户已注销");
//        user.setRole(4);
//        user.setName(null);
//        user.setNumber(null);
//        user.setContact(null);
//        user.setSchool(CommonConstants.DEFAULT_SCHOOL);
//        user.setBuilding(null);
//        user.setHouseNumber(null);
//        user.setExpressAppraise(null);
//        user.setOrderAppraise(null);
//        user.setCoin(0);
//        user.setGold(0);
//        user.setCredit(100);
//
//        if(userMapper.updateById(user) == 0){
//            throw new MyException(EnumExceptionType.UPDATE_FAILED);
//        }
//    }
}