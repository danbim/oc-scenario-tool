# --- !Ups

create table scenario (
  id                        bigint not null,
  title                     varchar(255),
  summary                   varchar(255),
  narrative                 varchar(255),
  constraint pk_scenario primary key (id))
;

create table token_action (
  token                     varchar(255) not null,
  user_email                varchar(255),
  token_type                integer,
  created                   timestamp,
  expires                   timestamp,
  constraint ck_token_action_token_type check (token_type in (0,1)),
  constraint pk_token_action primary key (token))
;

create table user (
  email                     varchar(255) not null,
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  gender                    varchar(255),
  email_validated           boolean,
  hashed_password           varchar(255),
  locale                    varchar(20),
  picture                   varchar(255),
  profile_link              varchar(255),
  auth_id                   varchar(255),
  auth_provider             varchar(255),
  roles                     varchar(255),
  constraint pk_user primary key (email))
;

create table user_linked_account (
  id                        bigint not null,
  user_email                varchar(255),
  provider_key              varchar(255),
  provider_user_id          varchar(255),
  email                     varchar(255),
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  picture                   varchar(255),
  gender                    varchar(255),
  locale                    varchar(20),
  profile                   varchar(255),
  constraint pk_user_linked_account primary key (id))
;

create sequence scenario_seq;

create sequence token_action_seq;

create sequence user_seq;

create sequence user_linked_account_seq;

alter table token_action add constraint fk_token_action_user_1 foreign key (user_email) references user (email) on delete restrict on update restrict;
create index ix_token_action_user_1 on token_action (user_email);
alter table user_linked_account add constraint fk_user_linked_account_user_2 foreign key (user_email) references user (email) on delete restrict on update restrict;
create index ix_user_linked_account_user_2 on user_linked_account (user_email);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists scenario;

drop table if exists token_action;

drop table if exists user;

drop table if exists user_linked_account;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists scenario_seq;

drop sequence if exists token_action_seq;

drop sequence if exists user_seq;

drop sequence if exists user_linked_account_seq;

