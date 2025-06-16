create table if not exists api_key (
    id bigint generated always as identity,
    account_id bigint not null,
    name varchar(255) not null,
    public_key varchar(255) not null,
    secret_key_hash bytea not null,
    creation_date timestamp not null,
    expiry_date timestamp,
    primary key(id),
    foreign key(account_id) references account(id)
);
