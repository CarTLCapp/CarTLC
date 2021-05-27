# --- Database schema

# --- !Ups

create table entry_fail (
  id                       int auto_increment primary key,
  entry_id                 int,
  report_time              datetime,
  problem_desc             text,
  times_encountered        int
);

alter table entry_fail add constraint cf_e_entry_id foreign key (entry_id) references entry (id) on delete restrict on update restrict;

# --- !Downs

alter table entry_fail drop foreign key cf_e_entry_id;
drop table if exists entry_fail;