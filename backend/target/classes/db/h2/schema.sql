create table if not exists t_user (
  user_id bigint primary key,
  username varchar(64) not null unique,
  password_hash varchar(255) not null,
  display_name varchar(128) not null,
  phone varchar(32),
  status int not null,
  pwd_changed_at timestamp,
  last_login_at timestamp,
  failed_count int not null,
  locked_until timestamp,
  created_at timestamp not null,
  updated_at timestamp not null
);

create table if not exists t_role (
  role_id bigint primary key,
  role_code varchar(64) not null unique,
  name varchar(128) not null,
  status int not null,
  created_at timestamp not null
);

create table if not exists t_permission (
  perm_id bigint primary key,
  perm_code varchar(128) not null unique,
  perm_type varchar(16) not null,
  name varchar(128) not null,
  path varchar(256),
  method varchar(16),
  parent_id bigint,
  sort int not null,
  status int not null
);

create table if not exists t_user_role (
  user_id bigint not null,
  role_id bigint not null,
  primary key (user_id, role_id)
);

create table if not exists t_role_permission (
  role_id bigint not null,
  perm_id bigint not null,
  primary key (role_id, perm_id)
);

create table if not exists t_user_data_scope (
  user_id bigint not null,
  district_id bigint not null,
  primary key (user_id, district_id)
);

create table if not exists t_refresh_token (
  id bigint primary key,
  user_id bigint not null,
  token_hash varchar(128) not null,
  expires_at timestamp not null,
  revoked_at timestamp,
  created_at timestamp not null,
  ip varchar(64),
  ua varchar(256)
);

create table if not exists t_audit_log (
  id bigint primary key,
  user_id bigint,
  action varchar(64) not null,
  resource varchar(256),
  request_clob clob,
  result_code varchar(32),
  cost_ms bigint,
  created_at timestamp not null,
  ip varchar(64),
  ua varchar(256)
);

create table if not exists t_district (
  id bigint primary key,
  name varchar(128) not null,
  code varchar(64)
);

create table if not exists t_street (
  id bigint primary key,
  district_id bigint not null,
  name varchar(128) not null,
  code varchar(64)
);

create table if not exists t_dict (
  dict_type varchar(64) not null,
  dict_code varchar(64) not null,
  dict_name varchar(128) not null,
  sort int not null,
  status int not null,
  primary key (dict_type, dict_code)
);

create table if not exists t_person (
  person_id bigint primary key,
  name varchar(64) not null,
  id_no varchar(32) not null,
  gender varchar(8),
  birthday date,
  phone varchar(32),
  district_id bigint,
  street_id bigint,
  address varchar(256),
  created_at timestamp not null,
  updated_at timestamp not null
);

create table if not exists t_disability_card (
  card_id bigint primary key,
  person_id bigint not null,
  card_no varchar(64) not null,
  category_code varchar(32),
  level_code varchar(32),
  issue_date date,
  status varchar(16),
  expire_date date,
  cancel_date date
);

create table if not exists t_car_benefit (
  person_id bigint primary key,
  has_car int,
  plate_no varchar(32),
  annual_inspection_status varchar(16),
  car_owner_id_no varchar(32),
  last_inspection_date date,
  updated_at timestamp
);

create table if not exists t_medical_benefit (
  person_id bigint primary key,
  enabled int,
  status varchar(16),
  last_pay_date date,
  updated_at timestamp
);

create table if not exists t_pension_benefit (
  person_id bigint primary key,
  enabled int,
  status varchar(16),
  last_pay_date date,
  updated_at timestamp
);

create table if not exists t_blind_card (
  person_id bigint primary key,
  card_no varchar(64),
  status varchar(16),
  issue_date date,
  updated_at timestamp
);

create table if not exists t_rule (
  rule_id bigint primary key,
  rule_code varchar(64) not null,
  name varchar(128) not null,
  category varchar(64),
  enabled int,
  severity varchar(16),
  params_json clob,
  description varchar(512),
  updated_at timestamp
);

create table if not exists t_anomaly (
  anomaly_id bigint primary key,
  rule_id bigint,
  person_id bigint not null,
  anomaly_type varchar(32) not null,
  hit_time timestamp not null,
  status varchar(16) not null,
  handler_user_id bigint,
  handle_note varchar(512),
  handled_at timestamp
);

create table if not exists t_anomaly_snapshot (
  anomaly_id bigint primary key,
  snapshot_json clob,
  created_at timestamp not null
);
