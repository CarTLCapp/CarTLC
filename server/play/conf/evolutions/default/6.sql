# --- Database schema

# --- !Ups

create table hours (
    id                  int auto_increment primary key,
    tech_id             int,
    entry_time          datetime,
    project_id          int,
    project_desc        text,
    start_time          int,
    end_time            int,
    lunch_time          int,
    break_time          int,
    drive_time          int,
    notes               text,
    time_zone           varchar(32),
    viewed              tinyint default 0
);

alter table hours add constraint h_e_tech_id foreign key (tech_id) references technician (id) on delete restrict on update restrict;

# --- !Downs

alter table hours drop foreign key h_e_tech_id;
drop table if exists hours;