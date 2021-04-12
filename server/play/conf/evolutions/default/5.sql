# --- Database schema

# --- !Ups

alter table flow add flags int default 0;

# --- !Downs

alter table flow drop flags;
