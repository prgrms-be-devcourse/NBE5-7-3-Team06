INSERT INTO code (code_id, group_code, code, name, created_at, updated_at)
VALUES (1, 'POSITION', '01', '사원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO member_info (member_info_id, birth, email, password, created_at, updated_at)
VALUES (1, '1995-01-01', 'leader@dev.com', '$2b$12$Yu66yyMjz2EIvQWLhvpOi.Er05Q9JfhMLLweIjuQWw6BRuPrFv5fW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
/* password : password1234 */

INSERT INTO member (member_id, name, join_date, role, dept_id, member_info_id, position_id, created_at, updated_at)
VALUES (1, '리더', CURRENT_TIMESTAMP, 'ADMIN', NULL, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO dept (dept_id, dept_name, dept_leader_id, created_at, updated_at)
VALUES (1, '개발팀', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
