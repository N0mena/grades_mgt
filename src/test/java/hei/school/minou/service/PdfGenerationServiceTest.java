package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.Exam;
import hei.school.minou.entity.Grade;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfGenerationServiceTest {

  private final PdfGenerationService pdfGenerationService = new PdfGenerationService();

  @TempDir File tempDir;

  @Test
  void generatesTranscriptWithCourseAveragesAndOverallAverage() throws Exception {
    Course math =
        Course.builder().id(UUID.randomUUID()).ref("MATH").title("Mathématiques").credit(2).build();
    Course phys =
        Course.builder().id(UUID.randomUUID()).ref("PHYS").title("Physique").credit(3).build();
    Exam mathCoef2 = Exam.builder().id(UUID.randomUUID()).coefficient(2f).build();
    Exam mathCoef3 = Exam.builder().id(UUID.randomUUID()).coefficient(3f).build();

    List<Grade> grades =
        List.of(grade(10f, math, mathCoef2), grade(14f, math, mathCoef3), grade(12f, phys, null));

    File output = new File(tempDir, "releve.pdf");
    pdfGenerationService.generateTranscript("Alice Dupont", grades, output);

    String text = extractText(output);
    assertThat(text).contains("Relevé de notes - Alice Dupont");
    assertThat(text).contains("Mathématiques (crédit : 2)");
    assertThat(text).contains("10.00/20 (coef 2.00)");
    assertThat(text).contains("14.00/20 (coef 3.00)");
    assertThat(text).contains("Moyenne du cours : 12.40/20");
    assertThat(text).contains("Physique (crédit : 3)");
    assertThat(text).contains("Moyenne générale (pondérée par crédit) : 12.16/20");
  }

  @Test
  void usesSimpleMeanWhenNoExamCoefficientIsPresent() throws Exception {
    Course math =
        Course.builder().id(UUID.randomUUID()).ref("MATH").title("Mathématiques").credit(2).build();

    File output = new File(tempDir, "simple.pdf");
    pdfGenerationService.generateTranscript(
        "Bob Martin", List.of(grade(10f, math, null), grade(14f, math, null)), output);

    assertThat(extractText(output)).contains("Moyenne du cours : 12.00/20");
  }

  @Test
  void handlesNullCreditAsOne() throws Exception {
    Course math =
        Course.builder()
            .id(UUID.randomUUID())
            .ref("MATH")
            .title("Mathématiques")
            .credit(null)
            .build();

    File output = new File(tempDir, "null-credit.pdf");
    pdfGenerationService.generateTranscript("Bob Martin", List.of(grade(16f, math, null)), output);

    assertThat(extractText(output))
        .contains("Mathématiques (crédit : 1)")
        .contains("Moyenne générale (pondérée par crédit) : 16.00/20");
  }

  @Test
  void handlesNoGrades() throws Exception {
    File output = new File(tempDir, "vide.pdf");
    pdfGenerationService.generateTranscript("Bob Martin", List.of(), output);

    assertThat(extractText(output)).contains("Aucune note disponible.");
  }

  private static Grade grade(Float value, Course course, Exam exam) {
    return Grade.builder().id(UUID.randomUUID()).value(value).course(course).exam(exam).build();
  }

  private static String extractText(File pdf) throws Exception {
    try (PDDocument document = Loader.loadPDF(pdf)) {
      return new PDFTextStripper().getText(document);
    }
  }
}
