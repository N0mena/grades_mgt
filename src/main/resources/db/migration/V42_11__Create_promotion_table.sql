create table if not exists promotion
(
    id         uuid primary key,
    ref        varchar,
    name       varchar,
    start_date timestamp,
    end_date   timestamp
);
