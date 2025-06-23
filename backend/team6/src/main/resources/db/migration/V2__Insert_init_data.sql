-- 직급 코드 삽입
INSERT IGNORE INTO code (group_code, code, name,created_at,updated_at) VALUES
                                                                           ('POSITION', '01', '사원',SYSDATE(),SYSDATE()),
                                                                           ('POSITION', '02', '대리',SYSDATE(),SYSDATE()),
                                                                           ('POSITION', '03', '과장',SYSDATE(),SYSDATE()),
                                                                           ('POSITION', '04', '부장',SYSDATE(),SYSDATE());

-- 휴가 유형 코드 삽입
INSERT IGNORE INTO code (group_code, code, name,created_at,updated_at) VALUES
                                                                           ('VACATION_TYPE', '01', '연차',SYSDATE(),SYSDATE()),
                                                                           ('VACATION_TYPE', '02', '포상 휴가',SYSDATE(),SYSDATE()),
                                                                           ('VACATION_TYPE', '03', '공가',SYSDATE(),SYSDATE()),
                                                                           ('VACATION_TYPE', '04', '경조사 휴가',SYSDATE(),SYSDATE()),
                                                                           ('VACATION_TYPE', '05', '반차',SYSDATE(),SYSDATE());

-- 부서 데이터 삽입
INSERT IGNORE INTO dept (dept_name,created_at,updated_at) VALUES
                                                              ('인사팀',SYSDATE(),SYSDATE()),
                                                              ('개발팀',SYSDATE(),SYSDATE()),
                                                              ('영업팀',SYSDATE(),SYSDATE());

-- 관리자 멤버 정보 삽입 (비밀번호: '123456q!')
INSERT IGNORE INTO member_info (birth, email, password,created_at,updated_at) VALUES
    ('2000-01-01 00:00:00', 'admin@a.com', '$2a$10$tpePuser/dmAY1s3OHEgjO6ocaJfEP4IJKyIyJgpKoEBDVVWXkCeq',SYSDATE(),SYSDATE());

-- 관리자 멤버 삽입 (position_id는 code_id를 참조)
INSERT IGNORE INTO members (name, dept_id, position_id, join_date, role, member_info_id,created_at,updated_at) VALUES
    ('관리자', 1, (SELECT code_id FROM code WHERE group_code = 'POSITION' AND code = '04'), '2023-05-15 00:00:00', 'ADMIN', 1,SYSDATE(),SYSDATE());

-- 일반 멤버 정보들 삽입
INSERT IGNORE INTO member_info (birth, email, password,created_at,updated_at) VALUES
                                                                                  ('1985-01-01 00:00:00', 'l1@a.com', '$2a$10$tpePuser/dmAY1s3OHEgjO6ocaJfEP4IJKyIyJgpKoEBDVVWXkCeq',SYSDATE(),SYSDATE()),
                                                                                  ('1983-11-11 00:00:00', 'l2@a.com', '$2a$10$tpePuser/dmAY1s3OHEgjO6ocaJfEP4IJKyIyJgpKoEBDVVWXkCeq',SYSDATE(),SYSDATE()),
                                                                                  ('1987-11-01 00:00:00', 'l3@a.com', '$2a$10$tpePuser/dmAY1s3OHEgjO6ocaJfEP4IJKyIyJgpKoEBDVVWXkCeq',SYSDATE(),SYSDATE());

-- 일반 멤버들 삽입
INSERT IGNORE INTO members (name, dept_id, position_id, join_date, role, member_info_id,created_at,updated_at) VALUES
                                                                                                                   ('김부장', 1, (SELECT code_id FROM code WHERE group_code = 'POSITION' AND code = '04'), '2023-05-15 00:00:00', 'PENDING', 2,SYSDATE(),SYSDATE()),
                                                                                                                   ('이부장', 2, (SELECT code_id FROM code WHERE group_code = 'POSITION' AND code = '04'), '2023-05-15 00:00:00', 'PENDING', 3,SYSDATE(),SYSDATE()),
                                                                                                                   ('박부장', 3, (SELECT code_id FROM code WHERE group_code = 'POSITION' AND code = '04'), '2023-05-15 00:00:00', 'PENDING', 4,SYSDATE(),SYSDATE());

-- 부서 리더 설정
UPDATE dept SET dept_leader_id = 2 WHERE dept_leader_id IS NULL AND dept_id = 1 ;
UPDATE dept SET dept_leader_id = 3 WHERE dept_leader_id IS NULL AND dept_id = 2;
UPDATE dept SET dept_leader_id = 4 WHERE dept_leader_id IS NULL AND dept_id = 3;