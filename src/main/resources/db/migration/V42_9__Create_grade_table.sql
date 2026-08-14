create table if not exists grade
(
    id          uuid primary key,
    value       float,
    student_id  uuid not null references "user" (id),
    teacher_id  uuid not null references "user" (id),
    created_at  timestamp not null,
    course_id   uuid not null references course (id),
    exam_id     uuid references exam (id)
);