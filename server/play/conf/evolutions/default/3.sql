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

create table user_info (
    id          int auto_increment primary key,
    name        varchar(128),
    password    varchar(128),
    is_admin    bit default 0
);

create table project_client_association (
    id           int auto_increment primary key,
    user_info_id int,
    project_id   int
);

alter table project_client_association add constraint c_pca_user_info_id foreign key (user_info_id) references user_info (id) on delete restrict on update restrict;
alter table project_client_association add constraint c_pca_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;

# --- !Downs

drop table if exists message;
drop table if exists user_info;
drop table if exists project_client_association;
