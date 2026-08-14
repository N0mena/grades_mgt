create table if not exists exam
(
    id            uuid primary key,
    exam_date     timestamp not null,
    coefficient   float,
    course_id     uuid not null references course (id)
);

create table if not exists exam_group
(
    exam_id  uuid not null references exam (id),
    group_id uuid not null references "group" (id),
    primary key (exam_id, group_id)
);