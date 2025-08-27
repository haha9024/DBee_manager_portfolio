# DBee Manager (Portfolio Version)

IPS 보안 관리 시스템의 관리자 페이지 및 로그 관리 기능을 구현한 학습 프로젝트입니다.  
실제 프로젝트에서 사용한 코드 일부를 정리하여 포트폴리오 용도로 공개했습니다.  
민감한 설정(DB 계정, 서버 환경 변수 등)은 제외되었습니다.

## 사용 기술
- Java (Spring Boot, MyBatis)
- MariaDB
- JavaScript (Fetch API, Chart.js)
- C/C++ (TCP Socket 프로그래밍)  
  ※ 엔진 코드는 보안이 필요해 제외되었습니다.
- Linux (MariaDB 설치 및 운영 경험)

## 주요 기능
- 정책 테이블 및 로그 테이블 설계 (일 단위 파티션 구조)
- 동적 조건 검색 (MyBatis if 분기 + 파라미터 맵)
- 관리자 페이지에서 통계 차트 및 로그 조회
- 0 값 표시 오류 수정 (JS nullish 처리)
- TCP Socket 프로그래밍 기반 네트워크 기초 구현

## 참고
※ 본 저장소는 포트폴리오 제출용으로, 실제 프로젝트의 민감한 정보(DB 계정, 환경 설정 등)는 제외했습니다.
