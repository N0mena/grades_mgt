create table if not exists group_history
(
    id         uuid primary key,
    group_id   uuid not null references "group" (id),
    student_id uuid not null references "user" (id),
    start_date timestamp not null,
    end_date   timestamp
);