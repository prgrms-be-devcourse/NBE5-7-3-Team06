-- code table init
CREATE TABLE if not exists code
(
    code_id    BIGINT AUTO_INCREMENT NOT NULL,
    createdAt  datetime              NULL,
    updatedAt  datetime              NULL,
    group_code VARCHAR(255)          NOT NULL,
    code       VARCHAR(255)          NOT NULL,
    name       VARCHAR(255)          NOT NULL,
    CONSTRAINT uc_group_code_code UNIQUE (group_code, code),
    CONSTRAINT pk_code PRIMARY KEY (code_id)
);

-- MemberInfo table init
CREATE TABLE if not exists MemberInfo
(
    member_info_id BIGINT AUTO_INCREMENT NOT NULL,
    createdAt      datetime              NULL,
    updatedAt      datetime              NULL,
    birth          VARCHAR(255)          NOT NULL,
    email          VARCHAR(255)          NOT NULL,
    password       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_member_info PRIMARY KEY (member_info_id)
);

-- Dept table init
CREATE TABLE if not exists Dept
(
    dept_id        BIGINT AUTO_INCREMENT NOT NULL,
    createdAt      datetime              NULL,
    updatedAt      datetime              NULL,
    deptName       VARCHAR(255)          NOT NULL,
    dept_leader_id BIGINT                NULL,
    CONSTRAINT uc_dept_dept_leader UNIQUE (dept_leader_id),
    CONSTRAINT pk_dept PRIMARY KEY (dept_id)
);

-- Member table init
CREATE TABLE if not exists members
(
    member_id      BIGINT AUTO_INCREMENT NOT NULL,
    createdAt      datetime              NULL,
    updatedAt      datetime              NULL,
    name           VARCHAR(255)          NOT NULL,
    dept_id        BIGINT                NULL,
    position_id    BIGINT                NULL,
    joinDate       datetime              NOT NULL,
    `role`         VARCHAR(255)          NOT NULL,
    member_info_id BIGINT                NULL,
    CONSTRAINT uc_member_member_info UNIQUE (member_info_id),
    CONSTRAINT pk_member PRIMARY KEY (member_id)
);

-- VacationRequest table init
CREATE TABLE if not exists VacationRequest
(
    vacation_request_id BIGINT AUTO_INCREMENT NOT NULL,
    createdAt           datetime              NULL,
    updatedAt           datetime              NULL,
    member_id           BIGINT                NULL,
    from_date           datetime              NOT NULL,
    to_date             datetime              NOT NULL,
    reason              VARCHAR(255)          NULL,
    type_code           BIGINT                NULL,
    status              VARCHAR(255)          NULL,
    version             INT                   NULL,
    CONSTRAINT pk_vacation_request PRIMARY KEY (vacation_request_id)
);

-- ApprovalStep table init
CREATE TABLE if not exists ApprovalStep
(
    approval_step_id    BIGINT AUTO_INCREMENT NOT NULL,
    createdAt           datetime              NULL,
    updatedAt           datetime              NULL,
    member_id           BIGINT                NOT NULL,
    vacation_request_id BIGINT                NOT NULL,
    approval_status     VARCHAR(255)          NOT NULL,
    step                INT                   NOT NULL,
    reason              VARCHAR(255)          NULL,
    CONSTRAINT pk_approvalstep PRIMARY KEY (approval_step_id)
);

-- VacationInfo table init (수정됨)
CREATE TABLE if not exists VacationInfo
(
    vacationId   INT AUTO_INCREMENT NOT NULL, -- Java Entity와 일치
    createdAt    datetime           NULL,
    updatedAt    datetime           NULL,
    totalCount   DOUBLE             NOT NULL, -- Java Entity와 일치
    useCount     DOUBLE             NOT NULL, -- Java Entity와 일치
    vacationType VARCHAR(255)       NULL,     -- Java Entity와 일치
    memberId     BIGINT             NULL,     -- Java Entity와 일치
    version      INT                NOT NULL,
    CONSTRAINT pk_vacationinfo PRIMARY KEY (vacationId)
);

-- VacationInfoLog table init
CREATE TABLE if not exists VacationInfoLog
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    totalCount   DOUBLE                NOT NULL, -- Java Entity와 일치
    useCount     DOUBLE                NOT NULL, -- Java Entity와 일치
    vacationType VARCHAR(255)          NULL,     -- Java Entity와 일치
    memberId     BIGINT                NULL,     -- Java Entity와 일치
    logDate      datetime              NULL,
    CONSTRAINT pk_vacationinfolog PRIMARY KEY (id)
);

-- Foreign Key 제약조건들
ALTER TABLE Dept
    ADD CONSTRAINT FK_DEPT_ON_DEPT_LEADER FOREIGN KEY (dept_leader_id) REFERENCES members (member_id);

ALTER TABLE members
    ADD CONSTRAINT FK_MEMBER_ON_DEPT FOREIGN KEY (dept_id) REFERENCES Dept (dept_id);

ALTER TABLE members
    ADD CONSTRAINT FK_MEMBER_ON_MEMBER_INFO FOREIGN KEY (member_info_id) REFERENCES MemberInfo (member_info_id);

ALTER TABLE members
    ADD CONSTRAINT FK_MEMBER_ON_POSITION FOREIGN KEY (position_id) REFERENCES code (code_id);

ALTER TABLE VacationRequest
    ADD CONSTRAINT FK_VACATION_REQUEST_ON_MEMBER FOREIGN KEY (member_id) REFERENCES members (member_id);

ALTER TABLE VacationRequest
    ADD CONSTRAINT FK_VACATION_REQUEST_ON_TYPE_CODE FOREIGN KEY (type_code) REFERENCES code (code_id);

ALTER TABLE ApprovalStep
    ADD CONSTRAINT FK_APPROVALSTEP_ON_MEMBER FOREIGN KEY (member_id) REFERENCES members (member_id);

ALTER TABLE ApprovalStep
    ADD CONSTRAINT FK_APPROVALSTEP_ON_VACATION_REQUEST FOREIGN KEY (vacation_request_id) REFERENCES VacationRequest (vacation_request_id);