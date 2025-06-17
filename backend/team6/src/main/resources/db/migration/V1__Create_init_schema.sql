create table if not exists approval_step
(
    step                integer                                                     not null,
    approval_step_id    bigint                                                      not null auto_increment,
    created_at          datetime(6),
    member_id           bigint                                                      not null,
    updated_at          datetime(6),
    vacation_request_id bigint                                                      not null,
    reason              varchar(255),
    approval_status     enum ('APPROVED','CANCELED','PENDING','REJECTED','WAITING') not null,
    primary key (approval_step_id)
);
create table if not exists code
(
    code_id    bigint       not null auto_increment,
    created_at datetime(6),
    updated_at datetime(6),
    code       varchar(255) not null,
    group_code varchar(255) not null,
    name       varchar(255) not null,
    primary key (code_id)
);
create table if not exists dept
(
    created_at     datetime(6),
    dept_id        bigint       not null auto_increment,
    dept_leader_id bigint,
    updated_at     datetime(6),
    dept_name      varchar(255) not null,
    primary key (dept_id)
);
create table if not exists member_info
(
    created_at     datetime(6),
    member_info_id bigint       not null auto_increment,
    updated_at     datetime(6),
    birth          varchar(255) not null,
    email          varchar(255) not null,
    password       varchar(255) not null,
    primary key (member_info_id)
);
create table if not exists members
(
    created_at     datetime(6),
    dept_id        bigint,
    join_date      datetime(6)                     not null,
    member_id      bigint                          not null auto_increment,
    member_info_id bigint,
    position_id    bigint,
    updated_at     datetime(6),
    name           varchar(255)                    not null,
    role           enum ('ADMIN','PENDING','USER') not null,
    primary key (member_id)
);
create table if not exists vacation_info
(
    total_count   float(53) not null,
    use_count     float(53) not null,
    vacation_id   integer   not null auto_increment,
    version       integer   not null,
    created_at    datetime(6),
    member_id     bigint,
    updated_at    datetime(6),
    vacation_type varchar(255),
    primary key (vacation_id)
);
create table if not exists vacation_info_log
(
    total_count   float(53) not null,
    use_count     float(53) not null,
    id            bigint    not null auto_increment,
    log_date      datetime(6),
    member_id     bigint,
    vacation_type varchar(255),
    primary key (id)
);
create table if not exists vacation_request
(
    version             integer,
    created_at          datetime(6),
    from_date           datetime(6) not null,
    member_id           bigint,
    to_date             datetime(6) not null,
    type_code           bigint,
    updated_at          datetime(6),
    vacation_request_id bigint      not null auto_increment,
    reason              varchar(255),
    status              enum ('APPROVED','CANCELED','IN_PROGRESS','REJECTED'),
    primary key (vacation_request_id)
);
alter table code
    add constraint UKq0dus79kg2bo8k3bmd0snygxt unique (group_code, code);
alter table dept
    add constraint UKilmyvjd7nxkwxbuv9qkxjc3sp unique (dept_leader_id);
alter table members
    add constraint UK9yurcta1ncy7u5847sjqgw15 unique (member_info_id);
alter table member_info
    add constraint uk_member_email unique (email);
alter table approval_step
    add constraint FKaqeemrh1cjiox9nwqxk38yqo1
        foreign key (member_id)
            references members (member_id);
alter table approval_step
    add constraint FKic5me2lucrgmxcwfbsjuecyok
        foreign key (vacation_request_id)
            references vacation_request (vacation_request_id);
alter table dept
    add constraint FKflwrd12uirfvgrx70r2rocnx8
        foreign key (dept_leader_id)
            references members (member_id);
alter table members
    add constraint FKlnsxeenxdg1pbajg9qbrupf1p
        foreign key (dept_id)
            references dept (dept_id);
alter table members
    add constraint FK9dvxgrsugxo7nyuvldtu51ws0
        foreign key (member_info_id)
            references member_info (member_info_id);
alter table members
    add constraint FKd0ocivy6x4mmxt4plq13hk9qy
        foreign key (position_id)
            references code (code_id);
alter table vacation_request
    add constraint FK6but7h70vqtaw65jxyp6pj2h0
        foreign key (member_id)
            references members (member_id);
alter table vacation_request
    add constraint FK5o059bye9jl0kpih4wp6br7n6
        foreign key (type_code)
            references code (code_id);
