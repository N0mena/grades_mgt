create table if not exists course
(
    id     uuid primary key,
    ref    varchar,
    title  varchar,
    credit integer
);

