# Vacation Management System (휴가 관리 시스템)

**Vacation Management System**은 기업의 휴가 관리를 효율적으로 도와주는 웹 기반 서비스입니다.  
휴가 신청, 승인, 통계 확인 등을 할 수 있는 구조로 설계되었습니다.

## 🏗️ 프로젝트 구조

## 시스템 구성도

[시스템 구성도.pdf](https://github.com/user-attachments/files/20358583/default.pdf)

## 🛠️ 기술 스택

### Backend

- Java 21
- Spring Boot
- Spring Security + JWT 인증
- JPA (Hibernate)
- MySQL
- Redis (토큰 블랙리스트 저장용)
- Gradle

### Frontend

- React
- React Router
- Axios

## 🙋 팀원소개

<table>
  <thead>
    <tr>
      <th align="center">팀장</th>
      <th align="center">팀원</th>
      <th align="center">팀원</th>
      <th align="center">팀원</th>
      <th align="center">팀원</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/gunwoong1630">
          <img src="https://github.com/gunwoong1630.png" width="120" height="120" alt="조건웅"/><br/>
          <sub><b>조건웅</b></sub>
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/dbogym">
          <img src="https://github.com/dbogym.png" width="120" height="120" alt="고영민"/><br/>
          <sub><b>고영민</b></sub>
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/rhwlgns4386">
          <img src="https://github.com/rhwlgns4386.png" width="120" height="120" alt="고지훈"/><br/>
          <sub><b>고지훈</b></sub>
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/min0962">
          <img src="https://github.com/min0962.png" width="120" height="120" alt="민경준"/><br/>
          <sub><b>민경준</b></sub>
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/sehee123">
          <img src="https://github.com/sehee123.png" width="120" height="120" alt="황세희"/><br/>
          <sub><b>황세희</b></sub>
        </a>
      </td>
    </tr>
  </tbody>
</table>

## 🛠️ 역할 분담

| 이름     | 담당 기능                                                        |
|--------|--------------------------------------------------------------|
| **건웅** | - 전체 휴가 신청 목록 필터링 조회 <br> - 휴가 신청 내역 조회 및 관리 <br> - 코드 분류 관리 |
| **영민** | - 본인 연차 조회 <br> - 휴가 신청 내역 조회 <br> - 휴가 신청 <br> - 대기중인 휴가 관리 |
| **지훈** | - 휴가 자동 부여 <br> - 휴가 개수 관리 <br> - 월별 사용자 휴가 사용내역             |
| **경준** | - 1, 2차 결재 <br>- 회원 승인 관리 <br>                               |
| **세희** | - Spring Security, JWT를 통한 인증,인가 <br>- 전체 휴가 캘린더             |

## 📦 주요 기능

### 인증 / 인가

- 회원가입 / 로그인 / 로그아웃
- JWT 기반 인증 및 토큰 재발급
- 리프레시 토큰 쿠키 저장 + 블랙리스트 관리 (Redis)

### 휴가 기능

- 휴가 신청 / 수정 / 취소
- 관리자 승인 / 반려
- 부서별 휴가 통계
- 월별 휴가 캘린더 조회 (FullCalendar 활용)

### 관리자 기능

- 사내 부서 및 코드(직급, 휴가 타입 등) 설정
- 전체 통계 대시보드

## 🧪 테스트

- JUnit5 + Mockito 기반 단위 테스트
- 통합 테스트 (SpringBootTest)

## 📂 화면


