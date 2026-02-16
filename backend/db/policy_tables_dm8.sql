-- 政策文档主表（达梦数据库兼容）
create table t_policy_document (
  policy_id number(19) primary key,
  user_id number(19) not null,
  title varchar(256) not null,
  oss_url varchar(512) not null,
  oss_key varchar(512) not null,
  file_name varchar(256),
  file_size number(19),
  file_type varchar(32),
  content_length number(10),
  status varchar(16) default 'ACTIVE',
  created_at timestamp not null,
  updated_at timestamp not null,
  constraint fk_policy_user foreign key (user_id) references t_user(user_id)
);

comment on table t_policy_document is '政策文档主表';
comment on column t_policy_document.policy_id is '政策ID';
comment on column t_policy_document.user_id is '用户ID';
comment on column t_policy_document.title is '政策标题';
comment on column t_policy_document.oss_url is 'OSS文件URL';
comment on column t_policy_document.oss_key is 'OSS对象Key';
comment on column t_policy_document.file_name is '原始文件名';
comment on column t_policy_document.file_size is '文件大小(字节)';
comment on column t_policy_document.file_type is '文件类型';
comment on column t_policy_document.content_length is '文本字符数';
comment on column t_policy_document.status is '状态:ACTIVE/ARCHIVED';

-- 政策分析版本表
-- 需要先创建序列用于自增ID
create sequence seq_policy_analysis start with 1 increment by 1;

create table t_policy_analysis (
  analysis_id number(19) primary key,
  policy_id number(19) not null,
  version number(10) not null,
  is_latest char(1) default 'N',
  conditions_json clob,
  explanation varchar(2048),
  analyzed_segments number(10),
  total_segments number(10),
  created_at timestamp not null,
  constraint fk_analysis_policy foreign key (policy_id) references t_policy_document(policy_id),
  constraint chk_is_latest check (is_latest in ('Y', 'N'))
);

comment on table t_policy_analysis is '政策分析版本表';
comment on column t_policy_analysis.analysis_id is '分析ID';
comment on column t_policy_analysis.policy_id is '政策ID';
comment on column t_policy_analysis.version is '版本号';
comment on column t_policy_analysis.is_latest is '是否最新版本:Y/N';
comment on column t_policy_analysis.conditions_json is '提取的条件JSON';
comment on column t_policy_analysis.explanation is 'AI解释说明';
comment on column t_policy_analysis.analyzed_segments is '已分析段数';
comment on column t_policy_analysis.total_segments is '总段数';

-- 政策查询记录表
create sequence seq_policy_query_log start with 1 increment by 1;

create table t_policy_query_log (
  log_id number(19) primary key,
  analysis_id number(19) not null,
  filters_json clob,
  total_results number(10),
  executed_at timestamp not null,
  constraint fk_query_analysis foreign key (analysis_id) references t_policy_analysis(analysis_id)
);

comment on table t_policy_query_log is '政策查询记录表';
comment on column t_policy_query_log.log_id is '日志ID';
comment on column t_policy_query_log.analysis_id is '分析ID';
comment on column t_policy_query_log.filters_json is '实际使用的条件';
comment on column t_policy_query_log.total_results is '查询结果数';
comment on column t_policy_query_log.executed_at is '执行时间';

-- 索引
create index ix_policy_user on t_policy_document(user_id);
create index ix_policy_status on t_policy_document(status);
create index ix_policy_created on t_policy_document(created_at);
create index ix_analysis_policy on t_policy_analysis(policy_id);
create index ix_analysis_latest on t_policy_analysis(policy_id, is_latest);
create index ix_query_analysis on t_policy_query_log(analysis_id);

-- H2兼容版本（开发环境使用）
-- 注意：生产环境请使用上面的达梦版本

/*
-- H2版本
-- 政策文档主表
create table if not exists t_policy_document (
  policy_id bigint primary key,
  user_id bigint not null,
  title varchar(256) not null,
  oss_url varchar(512) not null,
  oss_key varchar(512) not null,
  file_name varchar(256),
  file_size bigint,
  file_type varchar(32),
  content_length int,
  status varchar(16) default 'ACTIVE',
  created_at timestamp not null,
  updated_at timestamp not null
);

-- 政策分析版本表
create table if not exists t_policy_analysis (
  analysis_id bigint primary key,
  policy_id bigint not null,
  version int not null,
  is_latest boolean default false,
  conditions_json clob,
  explanation varchar(2048),
  analyzed_segments int,
  total_segments int,
  created_at timestamp not null
);

-- 政策查询记录表
create table if not exists t_policy_query_log (
  log_id bigint primary key,
  analysis_id bigint not null,
  filters_json clob,
  total_results int,
  executed_at timestamp not null
);
*/
