
alter table users add column if not exists email_verified boolean not null default false;

create table if not exists email_verification_tokens (
    id          bigserial primary key,
    user_id     bigint not null references users(id) on delete cascade,
    token       varchar(64) not null unique,
    expires_at  timestamp not null,
    used        boolean not null default false,
    created_at  timestamp not null default now()
);

create index if not exists idx_evt_user on email_verification_tokens(user_id);
create index if not exists idx_evt_expires on email_verification_tokens(expires_at);