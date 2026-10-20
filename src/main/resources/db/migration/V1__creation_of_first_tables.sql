CREATE TYPE status_support_conversation AS ENUM ('WAITING', 'IN_PROGRESS', 'CLOSED');
CREATE TYPE sender_options AS ENUM ('ADMIN', 'CLIENT');

create table admin (
    id          bigserial primary key,
    name        varchar(100) not null,
    email       varchar(150) not null unique,
    password    varchar(100) not null
);

create table client (
    id      bigserial primary key,
    name    varchar(100) not null,
    email   varchar(150) not null,
    phone   varchar(20) not null
);

create table support_conversation (
    id           bigserial primary key,
    client_id    bigint not null references client (id),
    admin_id     bigint references admin (id),
    title        varchar(200) not null,
    status       status_support_conversation not null default 'WAITING',
    created_at   timestamptz not null,
    finished_at  timestamptz null
);

create table message (
    id                      bigserial primary key,
    support_conversation_id bigint not null references support_conversation (id),
    sender                  sender_options not null,
    content                 varchar(2000) not null,
    sent_date               timestamptz not null
);