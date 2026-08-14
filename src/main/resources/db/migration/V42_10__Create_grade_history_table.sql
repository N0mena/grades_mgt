create table if not exists grade_history
(
    id           uuid primary key,
    grade_id     uuid not null references grade (id),
    old_value    float,
    new_value    float,
    modified_at  timestamp not null,
    reason       varchar not null,
    modified_by  uuid not null references "user" (id)
);