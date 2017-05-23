# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table company (
  id                            bigint not null,
  name                          varchar(255),
  constraint pk_company primary key (id)
);
create sequence company_seq;

create table computer (
  id                            bigint not null,
  name                          varchar(255),
  introduced                    timestamp,
  discontinued                  timestamp,
  company_id                    bigint,
  constraint pk_computer primary key (id)
);
create sequence computer_seq;

alter table computer add constraint fk_computer_company_id foreign key (company_id) references company (id) on delete restrict on update restrict;
create index ix_computer_company_id on computer (company_id);


# --- !Downs

alter table computer drop constraint if exists fk_computer_company_id;
drop index if exists ix_computer_company_id;

drop table if exists company;
drop sequence if exists company_seq;

drop table if exists computer;
drop sequence if exists computer_seq;

