package fun.redamancyxun.chinese.backend.score;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateQuantitativeXlsx {

    public static void main(String[] args) {
        try {
            // Load Excel file
            FileInputStream excelFile = new FileInputStream(new File("src/main/java/fun/redamancyxun/chinese/backend/score/AudioTextMapping.xlsx"));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            // Map PCM paths to text
            Map<String, String> audioTextMapping = new HashMap<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                String pcmPath = row.getCell(0).getStringCellValue();
                String text = row.getCell(1).getStringCellValue();
                audioTextMapping.put(pcmPath, text);
            }

            // Process each XML file
            for (String pcmPath : audioTextMapping.keySet()) {
                String xmlFilename = (FilenameUtils.removeExtension(pcmPath) + ".xml").replace("pcm", "xml");
                System.out.println("正在处理 " + xmlFilename);

                // Parse XML file
                File xmlFile = new File(xmlFilename);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);

                // Normalize the document
                doc.getDocumentElement().normalize();

                // Collect data from XML
                List<Map<String, String>> allData = new ArrayList<>();
                NodeList readSentenceNodes = doc.getElementsByTagName("read_sentence");
                for (int temp = 0; temp < readSentenceNodes.getLength(); temp++) {
                    Element readSentence = (Element) readSentenceNodes.item(temp);
                    if (readSentence.getAttribute("content").isEmpty()) continue;

                    Map<String, String> readSentenceData = new HashMap<>();
                    readSentenceData.put("filename", xmlFilename);
                    readSentenceData.put("content", readSentence.getAttribute("content"));
                    readSentenceData.put("beg_pos", readSentence.getAttribute("beg_pos"));
                    readSentenceData.put("end_pos", readSentence.getAttribute("end_pos"));
                    readSentenceData.put("fluency_score", readSentence.getAttribute("fluency_score"));
                    readSentenceData.put("integrity_score", readSentence.getAttribute("integrity_score"));
                    readSentenceData.put("phone_score", readSentence.getAttribute("phone_score"));
                    readSentenceData.put("tone_score", readSentence.getAttribute("tone_score"));
                    readSentenceData.put("total_score", readSentence.getAttribute("total_score"));
                    allData.add(readSentenceData);

                    NodeList sentenceNodes = readSentence.getElementsByTagName("sentence");
                    for (int i = 0; i < sentenceNodes.getLength(); i++) {
                        Element sentence = (Element) sentenceNodes.item(i);
                        NodeList wordNodes = sentence.getElementsByTagName("word");
                        for (int j = 0; j < wordNodes.getLength(); j++) {
                            Element word = (Element) wordNodes.item(j);
                            NodeList syllNodes = word.getElementsByTagName("syll");
                            for (int k = 0; k < syllNodes.getLength(); k++) {
                                Element syll = (Element) syllNodes.item(k);
                                NodeList phoneNodes = syll.getElementsByTagName("phone");
                                for (int l = 0; l < phoneNodes.getLength(); l++) {
                                    Element phone = (Element) phoneNodes.item(l);
                                    Map<String, String> phoneData = new HashMap<>();
                                    phoneData.put("filename", xmlFilename);
                                    phoneData.put("content", phone.getAttribute("content"));
                                    phoneData.put("beg_pos", phone.getAttribute("beg_pos"));
                                    phoneData.put("end_pos", phone.getAttribute("end_pos"));
                                    phoneData.put("dp_message", phone.getAttribute("dp_message"));
                                    phoneData.put("mono_tone", phone.getAttribute("mono_tone"));
                                    phoneData.put("is_yun", phone.getAttribute("is_yun"));
                                    phoneData.put("perr_msg", phone.getAttribute("perr_msg"));
                                    allData.add(phoneData);
                                }
                            }
                        }
                    }
                }

                // Save data to Excel
                Workbook excelWorkbook = new XSSFWorkbook();
                Sheet excelSheet = excelWorkbook.createSheet("Data");
                int rowCount = 0;
                for (Map<String, String> data : allData) {
                    Row row = excelSheet.createRow(rowCount++);
                    int colCount = 0;
                    for (String key : data.keySet()) {
                        Cell cell = row.createCell(colCount++);
                        cell.setCellValue(data.get(key));
                    }
                }

                try (FileOutputStream outputStream = new FileOutputStream((FilenameUtils.removeExtension(xmlFilename) + ".xlsx").replace("xml", "xlsx"))) {
                    excelWorkbook.write(outputStream);
                }

            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

}
