create
extension if not exists pgcrypto;
create
extension if not exists pg_trgm;

create table user_account
(
    id                      serial primary key,
    slug                    varchar(12)              not null,
    profile_name            varchar(60)              not null,
    profile_avatar_metadata json,
    username                varchar(40)              not null,
    email                   varchar(255)             not null,
    password                text                     not null,
    created_at              timestamp with time zone not null,
    constraint user_account_slug_uq unique (slug),
    constraint user_account_username_uq unique (username),
    constraint user_account_email_uq unique (email),
    constraint user_account_profile_name_uq unique (profile_name)
);

create table user_follows
(
    id              serial primary key,
    user_id         integer                  not null,
    follows_user_id integer                  not null,
    followed_at     timestamp with time zone not null,
    constraint user_id_fk foreign key (user_id) references user_account (id),
    constraint follows_user_id_fk foreign key (follows_user_id) references user_account (id),
    constraint user_id_follows_user_id_uq unique (user_id, follows_user_id)
);

create table post
(
    id                 serial primary key,
    slug               varchar(12)              not null,
    user_id            integer                  not null,
    comment_to_post_id integer,
    content            varchar(500),
    created_at         timestamp with time zone not null,
    media_metadata     json,
    constraint post_slug_uq unique (slug),
    constraint user_id_fk foreign key (user_id) references user_account (id),
    constraint comment_to_post_id_fk foreign key (comment_to_post_id) references post (id)
);

create index post_comment_to_post_id_idx on post (comment_to_post_id);

create table repost
(
    id                  serial primary key,
    post_id             integer                  not null,
    reposted_by_user_id integer                  not null,
    reposted_at         timestamp with time zone not null,
    constraint post_id_fk foreign key (post_id) references post (id),
    constraint reposted_by_user_id_fk foreign key (reposted_by_user_id) references user_account (id),
    constraint post_id_reposted_by_user_id_uq unique (post_id, reposted_by_user_id)
);

create index repost_post_id_idx on repost (post_id);

create table post_like
(
    id               serial primary key,
    post_id          integer                  not null,
    liked_by_user_id integer                  not null,
    liked_at         timestamp with time zone not null,
    constraint post_id_fk foreign key (post_id) references post (id),
    constraint liked_by_user_id_fk foreign key (liked_by_user_id) references user_account (id),
    constraint post_like_post_id_liked_by_user_id_uq unique (post_id, liked_by_user_id)
);

create index post_like_post_id_idx on post_like (post_id);
