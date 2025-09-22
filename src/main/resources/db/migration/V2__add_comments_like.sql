
create table comments (
  id          bigserial primary key,
  recipe_id   bigint not null references recipes(id) on delete cascade,
  author_id   bigint not null references users(id) on delete cascade,
  parent_id   bigint references comments(id) on delete cascade,
  body        text   not null,
  created_at  timestamp not null default now(),
  updated_at  timestamp
);

create index if not exists idx_comments_recipe on comments(recipe_id);
create index if not exists idx_comments_parent on comments(parent_id);

create table recipe_likes (
  recipe_id bigint not null references recipes(id) on delete cascade,
  user_id   bigint not null references users(id) on delete cascade,
  primary key (recipe_id, user_id)
);

create table comment_likes (
   comment_id bigint not null references comments(id) on delete cascade,
   user_id    bigint not null references users(id) on delete cascade,
   primary key (comment_id, user_id)
);