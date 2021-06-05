# --- Database schema

# --- !Ups

create table daar (
    id                  int auto_increment primary key,
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
alter table flow add flags int default 0;

create table entry_recovery (
  id                       int auto_increment primary key,
  entry_id                 int,
  tech_id                  int,
  entry_time               datetime,
  project_id               int,
  company_id               int,
  equipment_collection_id  int,
  note_collection_id       int,
  picture_collection_id    int,
  truck_id                 int,
  comparison_flags         int,
  status                   smallint,
  time_zone                varchar(32),
  error                    varchar(256)
);

alter table entry_recovery add constraint c_er_entry_id foreign key (entry_id) references entry (id) on delete restrict on update restrict;
alter table entry_recovery add constraint c_er_entry_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table entry_recovery add constraint c_er_entry_company_id foreign key (company_id) references company (id) on delete restrict on update restrict;
alter table entry_recovery add constraint c_er_tech_id foreign key (tech_id) references technician (id) on delete restrict on update restrict;

create table repaired (
    id                  int auto_increment primary key,
    entry_id            int,
    instance_id         int,
    flags               int
);

alter table repaired add constraint c_r_entry_id foreign key (entry_id) references entry (id) on delete restrict on update restrict;

# --- !Downs

alter table flow drop flags;
alter table daar drop foreign key d_e_tech_id;
drop table if exists daar;

alter table entry_recovery drop foreign key c_er_entry_id;
alter table entry_recovery drop foreign key c_er_entry_project_id;
alter table entry_recovery drop foreign key c_er_entry_company_id;
alter table entry_recovery drop foreign key c_er_tech_id;

drop table if exists entry_recovery;
