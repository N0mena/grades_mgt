package hei.school.minou.service;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
import hei.school.minou.entity.Promotion;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class GraduateExcelGenerationService {

  private static final String[] FIXED_HEADERS = {"Prénom", "Nom", "Email", "Moyenne générale"};

  public byte[] generate(Promotion promotion, List<Graduate> graduates) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Diplômés " + promotion.ref());

      List<String> headers = new ArrayList<>();
      Collections.addAll(headers, FIXED_HEADERS);
      headers.addAll(courseColumns(graduates));

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        headerRow.createCell(i).setCellValue(headers.get(i));
      }

      int rowIndex = 1;
      for (Graduate graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(graduate.firstName());
        row.createCell(1).setCellValue(graduate.lastName());
        row.createCell(2).setCellValue(graduate.email());
        row.createCell(3).setCellValue(graduate.overallAverage());
        int column = FIXED_HEADERS.length;
        for (String course : headers.subList(FIXED_HEADERS.length, headers.size())) {
          Float average = graduate.courseAverages().get(course);
          if (average != null) {
            row.createCell(column).setCellValue(average);
          }
          column++;
        }
      }

      for (int i = 0; i < headers.size(); i++) {
        sheet.autoSizeColumn(i);
      }
      workbook.write(output);
      return output.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to generate graduate Excel file", e);
    }
  }

  private Set<String> courseColumns(List<Graduate> graduates) {
    Set<String> courses = new LinkedHashSet<>();
    for (Graduate graduate : graduates) {
      courses.addAll(graduate.courseAverages().keySet());
    }
    return courses;
  }
}
