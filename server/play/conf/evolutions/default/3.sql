# --- Database schema

# --- !Ups

create table message (
  id            int auto_increment primary key,
  tech_id       int,
  log_time      datetime,
  code          smallint,
  message       text,
  trace         text
);

create table userinfo {
    id          int auto_increment primary key,
    name        varchar(128),
    password    varchar(128),
    is_admin    bit default 0
};

# --- !Downs

drop table if exists message;
drop table if exists userinfo;
