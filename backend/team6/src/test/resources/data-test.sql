INSERT INTO code ( group_code, code, name, created_at, updated_at)
VALUES ('POSITION', '01', '사원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO member_info ( birth, email, password, created_at, updated_at)
VALUES ( '1995-01-01', 'leader@dev.com', '$2b$12$Yu66yyMjz2EIvQWLhvpOi.Er05Q9JfhMLLweIjuQWw6BRuPrFv5fW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
SET @member_info_id = LAST_INSERT_ID();
/* password : password1234 */

INSERT INTO members ( name, join_date, role, dept_id, member_info_id, position_id, created_at, updated_at)
VALUES ( '리더', CURRENT_TIMESTAMP, 'ADMIN', NULL,  @member_info_id, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO dept ( dept_name, dept_leader_id, created_at, updated_at)
VALUES ('개발팀', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
