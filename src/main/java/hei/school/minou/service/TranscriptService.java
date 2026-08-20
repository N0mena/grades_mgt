package hei.school.minou.service;

import hei.school.minou.endpoint.event.EventProducer;
import hei.school.minou.endpoint.event.model.TranscriptRequested;
import hei.school.minou.endpoint.rest.controller.dto.TranscriptResponse;
import hei.school.minou.entity.Course;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.security.SecurityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final GradeService gradeService;
  private final UserService userService;
  private final SecurityUtils securityUtils;
  private final EventProducer<TranscriptRequested> eventProducer;

  public TranscriptResponse getTranscript(UUID studentId, User viewer) {
    securityUtils.assertCanViewStudentGrades(viewer, studentId);
    if (viewer.role() == Role.TEACHER) {
      throw new ForbiddenOperationException("A teacher cannot view a full student transcript");
    }
    User student = userService.getUserById(studentId);
    List<Grade> grades = gradeService.getGradesByStudent(studentId, viewer);
    Map<String, Float> courseAverages = courseAverages(grades);
    return TranscriptResponse.builder()
        .student(student)
        .promotion(student.promotion())
        .grades(grades)
        .courseAverages(courseAverages)
        .overallAverage(overallAverage(grades))
        .build();
  }

  public void requestTranscriptSend(UUID studentId, User actor) {
    boolean isOwner = actor.id().equals(studentId);
    boolean isAdmin = actor.role() == Role.ADMIN;
    if (!isOwner && !isAdmin) {
      throw new ForbiddenOperationException("Vous ne pouvez demander que votre propre relevé");
    }
    if (isOwner && actor.role() != Role.STUDENT && actor.role() != Role.ADMIN) {
      throw new ForbiddenOperationException("Vous ne pouvez demander que votre propre relevé");
    }
    User student = userService.getUserById(studentId);
    String recipientEmail =
        actor.role() == Role.ADMIN && !isOwner ? student.email() : actor.email();
    eventProducer.accept(
        List.of(
            TranscriptRequested.builder()
                .studentId(studentId)
                .recipientEmail(recipientEmail)
                .build()));
  }

  private static Map<String, Float> courseAverages(List<Grade> grades) {
    Map<UUID, List<Grade>> byCourse = new LinkedHashMap<>();
    Map<UUID, Course> courses = new LinkedHashMap<>();
    for (Grade grade : grades) {
      if (grade.course() == null) {
        continue;
      }
      byCourse.computeIfAbsent(grade.course().id(), unused -> new ArrayList<>()).add(grade);
      courses.putIfAbsent(grade.course().id(), grade.course());
    }
    Map<String, Float> averages = new LinkedHashMap<>();
    for (Map.Entry<UUID, List<Grade>> entry : byCourse.entrySet()) {
      Course course = courses.get(entry.getKey());
      String title =
          course.title() != null ? course.title() : (course.ref() != null ? course.ref() : "Course");
      averages.put(title, average(entry.getValue()));
    }
    return averages;
  }

  private static Float overallAverage(List<Grade> grades) {
    Map<UUID, List<Grade>> byCourse = new LinkedHashMap<>();
    Map<UUID, Course> courses = new LinkedHashMap<>();
    for (Grade grade : grades) {
      if (grade.course() == null) {
        continue;
      }
      byCourse.computeIfAbsent(grade.course().id(), unused -> new ArrayList<>()).add(grade);
      courses.putIfAbsent(grade.course().id(), grade.course());
    }
    float weightedSum = 0;
    int totalCredit = 0;
    for (Map.Entry<UUID, List<Grade>> entry : byCourse.entrySet()) {
      Course course = courses.get(entry.getKey());
      int credit = course.credit() != null ? course.credit() : 1;
      weightedSum += average(entry.getValue()) * credit;
      totalCredit += credit;
    }
    return totalCredit == 0 ? null : weightedSum / totalCredit;
  }

  private static float average(List<Grade> grades) {
    float sum = 0;
    float coeffSum = 0;
    for (Grade grade : grades) {
      float value = grade.value() != null ? grade.value() : 0f;
      float coefficient =
          grade.exam() != null && grade.exam().coefficient() != null
              ? grade.exam().coefficient()
              : 1f;
      sum += value * coefficient;
      coeffSum += coefficient;
    }
    return coeffSum == 0 ? 0 : sum / coeffSum;
  }
}
