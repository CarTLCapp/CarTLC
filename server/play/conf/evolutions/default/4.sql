# --- Database schema

# --- !Ups

create table daar (
    id                int auto_increment primary key,
    tech_id             int,
    entry_time          datetime,
    start_time          datetime,
    project_id          int,
    project_desc        text,
    work_completed_desc text,
    missed_units_desc   text,
    issues_desc         text,
    injuries_desc       text,
    time_zone           varchar(32),
    viewed              tinyint default 0
);

alter table daar add constraint d_e_tech_id foreign key (tech_id) references technician (id) on delete restrict on update restrict;

# --- !Downs

alter table daar drop foreign key d_e_tech_id;
drop table if exists daar;