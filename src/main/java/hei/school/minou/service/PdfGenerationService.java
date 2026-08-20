package hei.school.minou.service;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.Grade;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerationService {

  private static final float MARGIN_X = 50;
  private static final float MARGIN_BOTTOM = 50;
  private static final float START_Y = 750;
  private static final float LINE_HEIGHT = 20;

  public void generateTranscript(String studentName, List<Grade> grades, File outputFile)
      throws IOException {
    try (PDDocument document = new PDDocument()) {
      PdfWriter writer = new PdfWriter(document);
      try {
        writer.writeTitle("Relevé de notes - " + studentName);

        List<CourseAggregate> aggregates = aggregateByCourse(grades);
        if (aggregates.isEmpty()) {
          writer.writeBody("Aucune note disponible.");
        } else {
          for (CourseAggregate course : aggregates) {
            writer.writeHeader(course.title() + " (crédit : " + course.credit() + ")");
            for (Grade grade : course.grades()) {
              writer.writeBody(formatGrade(grade));
            }
            writer.writeBody("Moyenne du cours : " + formatNumber(course.average()) + "/20");
            writer.writeBlank();
          }
          writer.writeAverage(
              "Moyenne générale (pondérée par crédit) : "
                  + formatNumber(overallAverage(aggregates))
                  + "/20");
        }
      } finally {
        writer.close();
      }
      document.save(outputFile);
    }
  }

  private static List<CourseAggregate> aggregateByCourse(List<Grade> grades) {
    Map<UUID, Course> courses = new LinkedHashMap<>();
    Map<UUID, List<Grade>> byCourse = new LinkedHashMap<>();
    UUID noCourseKey = UUID.randomUUID();

    for (Grade grade : grades) {
      Course course = grade.course();
      UUID key = course != null ? course.id() : noCourseKey;
      if (course != null && !courses.containsKey(key)) {
        courses.put(key, course);
      }
      byCourse.computeIfAbsent(key, unused -> new ArrayList<>()).add(grade);
    }

    List<CourseAggregate> result = new ArrayList<>();
    for (Map.Entry<UUID, List<Grade>> entry : byCourse.entrySet()) {
      Course course = courses.get(entry.getKey());
      List<Grade> courseGrades = entry.getValue();
      result.add(new CourseAggregate(course, courseGrades, courseAverage(courseGrades)));
    }
    return result;
  }

  private static float courseAverage(List<Grade> grades) {
    float weightedSum = 0;
    float totalCoefficient = 0;
    for (Grade grade : grades) {
      float value = grade.value() != null ? grade.value() : 0f;
      Float coefficient = coefficientOf(grade);
      float coefficientValue = coefficient != null ? coefficient : 1f;
      weightedSum += value * coefficientValue;
      totalCoefficient += coefficientValue;
    }
    return totalCoefficient == 0 ? 0 : weightedSum / totalCoefficient;
  }

  private static float overallAverage(List<CourseAggregate> aggregates) {
    float weightedSum = 0;
    int totalCredit = 0;
    for (CourseAggregate course : aggregates) {
      int credit = course.credit() != null ? course.credit() : 1;
      weightedSum += course.average() * credit;
      totalCredit += credit;
    }
    return totalCredit == 0 ? 0 : weightedSum / totalCredit;
  }

  private static Float coefficientOf(Grade grade) {
    return grade.exam() != null ? grade.exam().coefficient() : null;
  }

  private static String formatGrade(Grade grade) {
    float value = grade.value() != null ? grade.value() : 0f;
    String text = formatNumber(value) + "/20";
    Float coefficient = coefficientOf(grade);
    if (coefficient != null) {
      text += " (coef " + formatNumber(coefficient) + ")";
    }
    return text;
  }

  private static String formatNumber(float number) {
    return String.format(Locale.ROOT, "%.2f", number);
  }

  private record CourseAggregate(Course course, List<Grade> grades, float average) {
    Integer credit() {
      if (course == null) {
        return 1;
      }
      return course.credit() != null ? course.credit() : 1;
    }

    String title() {
      return course != null ? course.title() : "Cours inconnu";
    }
  }

  private static final class PdfWriter implements AutoCloseable {
    private static final PDType1Font TITLE_FONT =
        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font HEADER_FONT =
        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font BODY_FONT =
        new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font AVERAGE_FONT =
        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final PDDocument document;
    private PDPageContentStream contentStream;
    private float y = START_Y;

    PdfWriter(PDDocument document) throws IOException {
      this.document = document;
      newPage();
    }

    void writeTitle(String text) throws IOException {
      write(text, TITLE_FONT, 16);
    }

    void writeHeader(String text) throws IOException {
      write(text, HEADER_FONT, 12);
    }

    void writeBody(String text) throws IOException {
      write(text, BODY_FONT, 11);
    }

    void writeAverage(String text) throws IOException {
      write(text, AVERAGE_FONT, 12);
    }

    void writeBlank() throws IOException {
      y -= LINE_HEIGHT / 2;
      if (y < MARGIN_BOTTOM) {
        newPage();
      }
    }

    private void write(String text, PDType1Font font, float size) throws IOException {
      ensureSpace();
      contentStream.beginText();
      contentStream.setFont(font, size);
      contentStream.newLineAtOffset(MARGIN_X, y);
      contentStream.showText(text);
      contentStream.endText();
      y -= LINE_HEIGHT;
    }

    private void ensureSpace() throws IOException {
      if (y < MARGIN_BOTTOM + LINE_HEIGHT) {
        newPage();
      }
    }

    private void newPage() throws IOException {
      if (contentStream != null) {
        contentStream.close();
      }
      PDPage page = new PDPage();
      document.addPage(page);
      contentStream = new PDPageContentStream(document, page);
      y = START_Y;
    }

    @Override
    public void close() throws IOException {
      if (contentStream != null) {
        contentStream.close();
      }
    }
  }
}
