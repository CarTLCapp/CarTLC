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

create table client (
    id          int auto_increment primary key,
    name        varchar(128),
    password    varchar(128),
    is_admin    bit default 0
);

create table client_project_association (
    id             int auto_increment primary key,
    client_id      int,
    project_id     int
);

alter table client rename technician;
alter table client_project_association add constraint c_cpa_user_info_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_project_association add constraint c_cpa_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table picture_collection add note varchar(1028);

# --- !Downs

drop table if exists message;
drop table if exists client_project_association;
drop table if exists client;
alter table technician rename client;
alter table picture_collection drop column note;