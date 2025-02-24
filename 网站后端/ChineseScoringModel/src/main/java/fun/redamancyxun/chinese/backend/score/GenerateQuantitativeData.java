package fun.redamancyxun.chinese.backend.score;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class GenerateQuantitativeData {
    public static void main(String[] args) {
        // 指定要读取的文件夹路径
        String folderPath = "src/main/java/fun/redamancyxun/chinese/backend/score/xlsx/";
        // 指定保存的文件夹路径
        String savePath = "src/main/java/fun/redamancyxun/chinese/backend/score/txt/";

        // 读取Excel文件
        Map<String, String> audioTextMapping = new HashMap<>();
        try (FileInputStream file = new FileInputStream("src/main/java/fun/redamancyxun/chinese/backend/score/AudioTextMapping.xlsx");
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过表头
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String pcmPath = row.getCell(0).getStringCellValue();
                String text = row.getCell(1).getStringCellValue();
                audioTextMapping.put(pcmPath, text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 处理每个xlsx文件
        for (String fileName : audioTextMapping.keySet()) {
            String fileFullName = fileName.substring(0, fileName.lastIndexOf('.')) + ".xlsx";
            File file = new File(folderPath + fileFullName);
            try (FileInputStream excelFile = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(excelFile)) {
                Sheet sheet = workbook.getSheetAt(0);

                // 读取第一行数据
                Row headerRow = sheet.getRow(0);
                double smoothnessScore = headerRow.getCell(4).getNumericCellValue();
                double completenessScore = headerRow.getCell(5).getNumericCellValue();
                double phoneticScore = headerRow.getCell(6).getNumericCellValue();
                double toneScore = headerRow.getCell(7).getNumericCellValue();
                double totalScore = headerRow.getCell(8).getNumericCellValue();

                // 生成txt文件，文件名与xlsx文件同名
                String outputFileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".txt";
                File outputFile = new File(savePath + outputFileName);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    // 在txt最前面加上一段话
                    String introText = "此数据为留学生音频量化数据，每条数据都是口语音频中的音素信息，每条数据格式为：{content:{beg_pos,end_pos,dp_message,mono_tone,is_yun,perr_msg};}（数据为空代表数据无此属性）。参数说明：{'content':'音素内容','beg_pos':'开始边界时间','end_pos':'结束边界时间','dp_message':'0正常；16漏读；32增读；64回读；128替换（当dp_message不为0时，perr_msg可能出现与dp_message值保持一致的情况）','mono_tone':'调型','is_yun':'0声母，1韵母','perr_msg':'当is_yun=0时，perr_msg有两种状态：0声母正确，1声母错误；当is_yun=1时，perr_msg有四种状态：0韵母和调型均正确，1韵母错误，2调型错误，3韵母和调型均错误'}（注：content为sil表明是非文本中有的内容）。其中给出的第一条数据是可参考的讯飞AI维度评分。量化数据如下：{"
                            + "{" + "流畅度分:" + smoothnessScore + ",完整度分:" + completenessScore + ",声韵分:" + phoneticScore + ",调型分:" + toneScore + ",总分【模型回归】:" + totalScore + "};";
                    writer.write(introText);

                    // 从第二行开始读取数据
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        String content = row.getCell(1).getStringCellValue();
                        List<String> cToKValues = new ArrayList<>();

                        for (int j : new int[]{2, 3, 9, 10, 11, 12}) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                if (cell.getCellType() == CellType.NUMERIC) {
                                    cToKValues.add(String.valueOf((int) cell.getNumericCellValue()));
                                } else {
                                    cToKValues.add(cell.getStringCellValue());
                                }
                            } else {
                                cToKValues.add("");
                            }
                        }

                        String contentLine = content + ":{" + String.join(",", cToKValues) + "};";
                        writer.write(contentLine);
                    }

                    writer.write("}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("处理完成，结果已保存到 " + fileName.substring(0, fileName.lastIndexOf('.')) + ".txt" + " 文件中。");
        }
    }
}
