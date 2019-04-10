# --- Database schema

# --- !Ups

create table root_project (
    id                int auto_increment primary key,
    name              varchar(64),
    disabled          bit default 0
);

alter table project add root_project_id int;
alter table project add constraint c_p_root_project_id foreign key (root_project_id) references root_project (id) on delete restrict on update restrict;

# --- !Downs

alter table project drop foreign key c_p_root_project_id;
alter table project drop root_project_id;
drop table if exists root_project;