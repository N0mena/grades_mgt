create table if not exists "user"
(
    id         uuid primary key,
    first_name varchar,
    last_name  varchar,
    role       varchar,
    email      varchar unique,
    password   varchar
);


