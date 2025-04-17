create table if not exists account (
    id bigint generated always as identity,
    email varchar(255) not null,
    password_hash bytea not null,
    status varchar(255) not null,
    creation_date timestamp not null,
    primary key(id)
);

create table if not exists api_key (
    id bigint generated always as identity,
    account_id bigint not null,
    name varchar(255) not null,
    key_hash bytea not null,
    creation_date timestamp not null,
    expiry_date timestamp,
    primary key(id),
    foreign key(account_id) references account(id)
);
