create table if not exists course_assignement
(
    id         uuid primary key,
    course_id  uuid not null references course (id),
    teacher_id uuid not null references "user" (id),
    group_id   uuid not null references "group" (id),
    unique (course_id, teacher_id, group_id)
    );