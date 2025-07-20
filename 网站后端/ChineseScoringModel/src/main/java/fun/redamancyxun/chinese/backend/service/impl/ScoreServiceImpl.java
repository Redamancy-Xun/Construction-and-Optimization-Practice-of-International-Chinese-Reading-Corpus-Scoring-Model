package fun.redamancyxun.chinese.backend.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fun.redamancyxun.chinese.backend.common.CommonConstants;
import fun.redamancyxun.chinese.backend.controller.socre.request.AudioScoreRequest;
import fun.redamancyxun.chinese.backend.controller.socre.response.AudioScoreResponse;
import fun.redamancyxun.chinese.backend.entity.User;
import fun.redamancyxun.chinese.backend.exception.EnumExceptionType;
import fun.redamancyxun.chinese.backend.exception.MyException;
import fun.redamancyxun.chinese.backend.score.IseDemo;
import fun.redamancyxun.chinese.backend.service.ScoreActionService;
import fun.redamancyxun.chinese.backend.service.ScoreService;
import fun.redamancyxun.chinese.backend.service.UserService;
import fun.redamancyxun.chinese.backend.util.SessionUtils;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * 评分相关接口实现类
 * @author Redamancy
 * @description 评分相关接口实现类
 * @createDate 2024-11-3 22:39:04
 */
@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private ScoreActionService scoreActionService;

    @Autowired
    private UserService userService;

    /**
     * 将音频文件转换为pcm文件
     * @param data
     * @return pcm文件路径
     * @throws MyException
     */
    // TODO 16000Hz???
    @Override
    public String byte2pcm(byte[] data) throws MyException {
        if (data == null) {
            throw new MyException(EnumExceptionType.DATA_IS_NULL);
        }

        String filePath = CommonConstants.LINUX_SCORE_FILE_PATH + "pcm/" + RandomStringUtils.randomAlphanumeric(12) + ".pcm";
        // 使用 try-with-resources 确保 FileOutputStream 在使用后正确关闭
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // 将 byte[] 数据写入文件
            fos.write(data);
            System.out.println("PCM 文件保存成功：" + filePath + "！");
        } catch (IOException e) {
            System.err.println("保存 PCM 文件时发生错误: " + e.getMessage());
            throw new MyException(EnumExceptionType.IO_ERROR, e.getMessage());
        }

        return filePath;
    }

    /**
     * 利用pcm文件路径和文本内容生成音频-文本对照表
     * @param pcmPath
     * @param text
     * @return
     * @throws MyException
     */
    @Override
    public void pcm2xlsx(String pcmPath, String text) throws MyException {
        if (pcmPath == null || text == null) {
            throw new MyException(EnumExceptionType.DATA_IS_NULL);
        }

        // 创建一个工作簿
        Workbook workbook = new XSSFWorkbook();
        // 创建一个工作表
        Sheet sheet = workbook.createSheet("Audio-Text Mapping");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("PCM文件路径");
        headerRow.createCell(1).setCellValue("文本内容");

        // 创建数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(pcmPath);
        dataRow.createCell(1).setCellValue(text);

        // 自动调整列宽
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }

        // 保存为Excel文件
        try (FileOutputStream fileOut = new FileOutputStream(CommonConstants.LINUX_SCORE_FILE_PATH + "AudioTextMapping.xlsx")) {
            workbook.write(fileOut);
            System.out.println("音频-文本对照表生成成功！");
        } catch (IOException e) {
            System.err.println("保存音频-文本对照表时发生错误: " + e.getMessage());
            throw new MyException(EnumExceptionType.IO_ERROR, e.getMessage());
        }

        // 关闭工作簿
        try {
            workbook.close();
        } catch (IOException e) {
            System.err.println("关闭工作簿时发生错误: " + e.getMessage());
            throw new MyException(EnumExceptionType.IO_ERROR, e.getMessage());
        }
    }

    /**
     * 对pcm音频文件利用讯飞模型量化，转换为xml文件
     */
    @Override
    public void pcm2xml() throws Exception {
//        try {
//            // 定义 Python 解释器路径和脚本路径
//            String pythonPath = CommonConstants.LINUX_PYTHON_PATH;
//            String scriptPath = "src/main/java/fun/redamancyxun/chinese/backend/score/ise_ws_python3_demo.py";
//
//            /*
//             * 创建 ProcessBuilder 并启动进程
//             * ProcessBuilder：用于创建和管理外部进程。这里传入了 Python 解释器路径和脚本路径作为参数
//             * pb.start()：启动外部进程，返回一个 Process 对象，用于与该进程进行交互
//             */
//            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
//            Process proc = pb.start();
//
//            /*
//             * 读取标准输出流
//             * proc.getInputStream()：获取进程的标准输出流
//             * BufferedReader：用于读取输出流的每一行
//             * try-with-resources：确保 BufferedReader 在使用完毕后自动关闭（close()），避免资源泄漏
//             * while ((line = in.readLine()) != null)：逐行读取输出流的内容，并打印到控制台（建议使用日志框架替代）
//             */
//            try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
//                String line;
//                while ((line = in.readLine()) != null) {
//                    System.out.println(line);  // 使用日志框架替代
//                }
//            }
//
//            /*
//             * 读取错误流
//             * proc.getErrorStream()：获取进程的错误输出流
//             * BufferedReader：用于读取错误流的每一行
//             * while ((line = err.readLine()) != null)：逐行读取错误流的内容，并打印到控制台（建议使用日志框架替代）
//             */
//            try (BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
//                String line;
//                while ((line = err.readLine()) != null) {
//                    System.err.println(line);  // 使用日志框架替代
//                }
//            }
//
//            /*
//             * 等待进程完成
//             * proc.waitFor(10, TimeUnit.SECONDS)：等待进程在 10 秒内完成。如果进程在 10 秒内完成，则返回 true；否则返回 false。
//             * proc.exitValue()：获取进程的退出码。如果退出码为 0，表示进程成功执行；否则表示进程执行失败。
//             * proc.destroyForcibly()：强制终止超时的进程。
//             * throw new MyException(...)：如果进程执行失败或超时，抛出自定义异常 MyException，并附带相应的错误信息
//             */
//            // TODO 考虑10s的时间是否足够
//            if (proc.waitFor(10, TimeUnit.SECONDS)) {
//                if (proc.exitValue() != 0) {
//                    throw new MyException(EnumExceptionType.PROCESS_ERROR, "Python script execution failed with exit code: " + proc.exitValue());
//                }
//            } else {
//                proc.destroyForcibly();
//                throw new MyException(EnumExceptionType.PROCESS_TIME_OUT, "Python script execution timed out.");
//            }
//
//            System.out.println("讯飞模型量化成功！");  // 使用日志框架替代
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new MyException(EnumExceptionType.IO_ERROR, e.getMessage());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            throw new MyException(EnumExceptionType.INTERRUPTED_ERROR, e.getMessage());
//        }

        Map<String, String> audioTextMapping = readAudioTextMapping(CommonConstants.LINUX_SCORE_FILE_PATH + "AudioTextMapping.xlsx");
        // 创建一个 CountDownLatch，用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(audioTextMapping.size());
        IseDemo.latch = latch;
        for (Map.Entry<String, String> entry : audioTextMapping.entrySet()) {
            try {
                IseDemo.iseDemoScore(entry.getValue(), entry.getKey());
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        // 等待所有线程完成
        latch.await();

        // WebSocket 关闭后执行下一步操作
        System.out.println("WebSocket closed, continuing with next steps...");
    }

    /**
     * 将xml文件转换为文本文件
     * @return txt路径
     */
    @Override
    public void xml2txt() {
        Process proc1;
        try {
            String[] args1 = new String[]{CommonConstants.LINUX_PYTHON_PATH, CommonConstants.LINUX_SCORE_FILE_PATH + "generate_quantitative_xlsx.py"};
            System.out.println("Executing: " + Arrays.toString(args1));
            proc1 = Runtime.getRuntime().exec(args1);

            // 读取标准输出流
            BufferedReader in = new BufferedReader(new InputStreamReader(proc1.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

            // 读取错误流
            BufferedReader err1 = new BufferedReader(new InputStreamReader(proc1.getErrorStream()));
            String errLine1 = null;
            while ((errLine1 = err1.readLine()) != null) {
                System.err.println(errLine1);
            }
            err1.close();

            proc1.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("成功将xml文件转化为xlsx文件！");

        Process proc2;
        try {
            String[] args2 = new String[]{CommonConstants.LINUX_PYTHON_PATH, CommonConstants.LINUX_SCORE_FILE_PATH + "generate_quantitative_data.py"};
            System.out.println("Executing: " + Arrays.toString(args2));
            proc2 = Runtime.getRuntime().exec(args2);

            // 读取标准输出流
            BufferedReader in = new BufferedReader(new InputStreamReader(proc2.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

            // 读取错误流
            BufferedReader err2 = new BufferedReader(new InputStreamReader(proc2.getErrorStream()));
            String errLine2 = null;
            while ((errLine2 = err2.readLine()) != null) {
                System.err.println(errLine2);
            }
            err2.close();

            proc2.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("成功将xlsx文件转化为txt文件！");
    }

    /**
     * 音频文件评分
     * @param audioScoreRequest
     * @return AudioScoreResponse
     */
    @Override
    public AudioScoreResponse score(AudioScoreRequest audioScoreRequest) throws Exception {

        if (audioScoreRequest == null) {
            throw new MyException(EnumExceptionType.DATA_IS_NULL);
        }
        // 获取当前用户的 userId
        User user = userService.getUserById(sessionUtils.getUserId());
        sessionUtils.refreshData(null);

        // 音频文件、文章id、文章内容
        byte[] audioFile;
        try {
            // 处理音频文件
            audioFile = audioScoreRequest.getAudioFile().getBytes();
            System.out.println("Received Audio Data: " + audioFile.length + " bytes");
        } catch (Exception e) {
            throw new MyException(EnumExceptionType.IO_ERROR, e.getMessage());
        }
        String bookId = audioScoreRequest.getBookId();
        String courseId = audioScoreRequest.getCourseId();
        String courseContent = audioScoreRequest.getCourseContent();

        // 将音频文件转化为pcm文件
        String pcmPath = byte2pcm(audioFile);
        // 将pcm文件转化为xlsx文件
        pcm2xlsx(pcmPath, courseContent);

        // 讯飞模型量化
        pcm2xml();

//        Thread.sleep(10000);

        // 将XML数据保存到文件中
        String xmlFilename = pcmPath.replace(".pcm", ".xml");
        String xmlFilePath = xmlFilename.replace("pcm", "xml");
        // 创建文件并确保目录存在
        Path path = Paths.get(xmlFilePath);
        Files.createDirectories(path.getParent()); // 确保目录存在
        Files.write(path, IseDemo.xmlData.getBytes(StandardCharsets.UTF_8)); // 直接写入内容

        System.out.println("XML result saved as " + xmlFilePath);

        // 将xml文件转化为txt文件
        xml2txt();

        String filePath = (getBaseName(pcmPath) + ".txt").replace("pcm", "txt");
        String fileContent = readFileToString(filePath);

        // 构建prompt
        String system = "你是一名为留学生中文口语打分的国汉老师，请对学生的口语表现进行评估。你的评分标准基于以下六个维度：语速、停顿、声母、韵母、声调和完整度，每个维度给出0~5的整数评分。每个维度的评分标准和计算方式如下：" +
                "{\\\"评分标准\\\":{\\\"准确度\\\":{\\\"声母/韵母\\\":{\\\"分类标准\\\":{\\\"正确率90%-100%\\\":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"发音准确，容易理解，接近母语水平，正确率很高\\\"," +
                "\\\"计算方式\\\":\\\"1-(声韵错误数/总音节数)\\\"},\\\"正确率70%-90%\\\":{\\\"等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"描述\\\":\\\"基本可理解，仅有少量错误\\\",\\\"计算方式\\\":\\\"1-(声韵错误数/总音节数)\\\"}," +
                "\\\"正确率0%-70%\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1,\\\"描述\\\":\\\"发音模糊，听懂困难\\\",\\\"计算方式\\\":\\\"1-(声韵错误数/总音节数)\\\"}}},\\\"声调\\\":{\\\"分类标准\\\":{\\\"正确率90%-100%\\\"" +
                ":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"声调准确，表达清晰\\\",\\\"计算方式\\\":\\\"调型错误数/总音节数\\\"},\\\"正确率70%-90%\\\":{\\\"等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"描述\\\":" +
                "\\\"声调基本正确，偶有错误\\\",\\\"计算方式\\\":\\\"调型错误数/总音节数\\\"},\\\"正确率0%-70%\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1,\\\"描述\\\":\\\"声调错误频繁，难以理解\\\",\\\"计算方式\\\":\\\"调型错误数/总音节数\\\"}}}}," +
                "\\\"流利度\\\":{\\\"语速\\\":{\\\"分类标准\\\":{\\\"每分钟120字及以上\\\":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"语言流畅，富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\\\",\\\"计算方式\\\":\\\"字数/时间（秒）\\\"}" +
                ",\\\"每分钟60-120字\\\":{\\\"等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"描述\\\":\\\"语言较为流畅，无意义的重复少、卡顿少，意识到错误能及时纠正\\\",\\\"计算方式\\\":\\\"字数/时间（秒）\\\"},\\\"每分钟60字以下\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1," +
                "\\\"描述\\\":\\\"磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\\\",\\\"计算方式\\\":\\\"字数/时间（秒）\\\"}}},\\\"停顿\\\":{\\\"分类标准\\\":{\\\"不合理停顿占比3%以内\\\":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"语言流畅，停顿自然，" +
                "富有感情，基本没有认错的字，即使读错也能及时修改或巧妙避免重复式修改\\\",\\\"计算方式\\\":\\\"不合理停顿字数(fil)/总字数\\\"},\\\"不合理停顿3%-10%\\\":{\\\"等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"描述\\\":\\\"偶有停顿，整体较流畅，无意义的重复少、卡顿少，意识到错误能及" +
                "时纠正\\\",\\\"计算方式\\\":\\\"不合理停顿字数(fil)/总字数\\\"},\\\"不合理停顿超过10%\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1,\\\"描述\\\":\\\"停顿频繁，影响理解，磕磕碰碰，无意义的重复多、卡顿多，意识到错误不能及时纠正\\\",\\\"计算方式\\\":\\\"不合理停顿字数(fil)" +
                "/总字数\\\"}}}},\\\"完整度\\\":{\\\"完整性\\\":{\\\"分类标准\\\":{\\\"朗读完成度95%-100%\\\":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"内容完整，毫无缺漏\\\",\\\"计算方式\\\":\\\"1-((增读+漏读+回读)/总字数)\\\"},\\\"朗读完成度85%-94.9%\\\":{\\\"" +
                "等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"描述\\\":\\\"有个别字词遗漏\\\",\\\"计算方式\\\":\\\"1-((增读+漏读+回读)/总字数)\\\"},\\\"朗读完成度84.9%及以下\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1,\\\"描述\\\":\\\"大段内容遗漏，理解困难\\\",\\\"计算方式\\\":\\\"" +
                "1-((增读+漏读+回读)/总字数)\\\"}}}},\\\"感情\\\":{\\\"情感表达\\\":{\\\"分类标准\\\":{\\\"音节饱满，语调自然\\\":{\\\"等级\\\":\\\"A\\\",\\\"量化值(g)\\\":5,\\\"描述\\\":\\\"表现出色，情感丰富\\\"},\\\"表现平稳，缺少情感\\\":{\\\"等级\\\":\\\"B\\\",\\\"量化值(g)\\\":3,\\\"" +
                "描述\\\":\\\"较为平淡，缺乏感染力\\\"},\\\"情感表达不佳，令人不适\\\":{\\\"等级\\\":\\\"C\\\",\\\"量化值(g)\\\":1,\\\"描述\\\":\\\"缺乏真诚，难以倾听\\\"}}}}}}";
        String prompt = "我是一位学习中文口语的留学生，我的朗读内容是：{" + courseContent + "}。我的音频的量化数据是：{" + fileContent +
                "}。你是一名为留学生中文口语打分的国汉老师，请根据system身份下给你输入的评分标准对我的口语表现进行评估，给我的语速、停顿、声母、韵母、声调和完整度五个维度" +
                "分别给出0~5的整数评分并给出有实际意义和作用的评语。输出格式例子：语速：2；停顿：3；声母：3；韵母：3；声调：3；完整度：5；评语：朗读不够流利，有少许声母韵母错误，但完成度很好，希望继续努力~！";
        System.out.println("PROMPT：" + prompt);

        // TODO 调用训练好的模型API
        // 调用模型API
        // API Key
        String apiKey = "5176fd66b5ebf072d3a4cb3cc7373e8b.GYxJkFGKPDaaMW3A";
        // API endpoint
        String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

        // Request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "glm-4-9b:129142139:v1:rovpx6cx");

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
        String responseResult = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    responseBody = response.body().string();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    responseResult = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
                }
                System.out.println("Response: " + responseBody);
                System.out.println("Result: " + responseResult);
            } else {
                System.out.println("Request failed: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Integer speed = Integer.valueOf(responseResult.split("语速：")[1].split("；")[0].trim());
        Integer pause = Integer.valueOf(responseResult.split("停顿：")[1].split("；")[0].trim());
        Integer initialConsonants = Integer.valueOf(responseResult.split("声母：")[1].split("；")[0].trim());
        Integer finalVowels = Integer.valueOf(responseResult.split("韵母：")[1].split("；")[0].trim());
        Integer tones = Integer.valueOf(responseResult.split("声调：")[1].split("；")[0].trim());
        Integer completeness =  Integer.valueOf(responseResult.split("完整度：")[1].split("；")[0].trim());
        String advice = responseResult.split("评语：")[1].trim();

        // 创建评分记录
        scoreActionService.createScoreAction(user.getId(), bookId, courseId, speed, pause, initialConsonants, finalVowels, tones, completeness, advice);

        // 返回评分结果
        AudioScoreResponse audioScoreResponse = AudioScoreResponse.builder()
                .speed(speed)
                .pause(pause)
                .initialConsonants(initialConsonants)
                .finalVowels(finalVowels)
                .tones(tones)
                .completeness(completeness)
                .advice(advice)
                .build();
        System.out.println("评分完成！结果是：" + audioScoreResponse);

//        String userId = user.getId();generate_train_json.py
//
//        // 异步更新用户信息
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//
//        // 在异步任务开始前保存请求上下文
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//
//        // 异步任务
//        executorService.submit(() -> {
//            try {
////                // 在异步任务中恢复请求上下文
////                RequestContextHolder.setRequestAttributes(requestAttributes);
//
//                userService.updateOneAdvice();
//                System.out.println("异步任务完成: 更新用户建议");
//            } catch (Exception e) {
//                System.err.println("异步任务失败: " + e.getMessage());
//            } finally {
////                // 确保清理请求上下文
////                RequestContextHolder.resetRequestAttributes();
//                executorService.shutdown();
//            }
//        });

//        userService.updateOneAdvice();

//        // 异步更新用户信息
//        CompletableFuture.runAsync(() -> {
//            try {
//                userService.updateOneAdvice();
//                System.out.println("异步任务完成: 更新用户建议");
//            } catch (Exception e) {
//                System.err.println("异步任务失败: " + e.getMessage());
//            }
//        });
//
//        // 计算程序用时
//        long startTime = System.currentTimeMillis();
//        userService.updateOneAdvice(user.getId());
//        long endTime = System.currentTimeMillis();
//        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");

        return audioScoreResponse;
    }

    // 读取文件
    public static String readFileToString(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    // 获取文件名
    public static String getBaseName(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty.");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return filename; // 没有扩展名，直接返回文件名
        } else {
            return filename.substring(0, dotIndex); // 返回没有扩展名的部分
        }
    }

    // 读取Excel文件获取音频-文本映射
    private static Map<String, String> readAudioTextMapping(String excelFilePath) throws IOException {
        Map<String, String> mapping = new HashMap<>();
        FileInputStream fileInputStream = new FileInputStream(excelFilePath);
        Workbook workbook = WorkbookFactory.create(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // 跳过标题行
            String pcmPath = row.getCell(0).getStringCellValue();
            String text = row.getCell(1).getStringCellValue();
            mapping.put(pcmPath, text);
        }
        workbook.close();
        fileInputStream.close();
        return mapping;
    }
}
