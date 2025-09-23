create table if not exists recipe_photos (
                                             id           bigserial primary key,
                                             recipe_id    bigint not null references recipes(id) on delete cascade,
    file_name    text   not null,
    url          text   not null,
    content_type varchar(255),
    size_bytes   bigint,
    sort_order   int    not null default 0,
    is_cover     boolean not null default false,
    created_at   timestamp not null default now()
    );

create index if not exists idx_recipe_photos_recipe on recipe_photos(recipe_id);