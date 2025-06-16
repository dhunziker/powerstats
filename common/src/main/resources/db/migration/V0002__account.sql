create table if not exists account (
    id bigint generated always as identity,
    email varchar(255) not null,
    password_hash bytea not null,
    status varchar(255) not null,
    creation_date timestamp not null,
    primary key(id)
);
