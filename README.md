# public-data-crawler
공공기관 보도자료·지원사업 공고 자동 수집 시스템 (Spring Boot, Jsoup, PostgreSQL, React)

## 구성

```
public-data-crawler/
├─ backend/            # Spring Boot 3.5 (Java 17, Gradle)
├─ frontend/           # React + TypeScript (Vite)
└─ docker-compose.yml  # PostgreSQL 16
```

## 필요 환경

- JDK 17 이상
- Node.js 20.19 이상 또는 22.12 이상
- Docker (Docker Compose 포함)

## 실행 방법

1. DB 실행 (루트에서)

   ```bash
   docker compose up -d
   ```

2. 백엔드 실행

   ```bash
   cd backend
   ./gradlew bootRun      # Windows: gradlew.bat bootRun
   ```

   확인: http://localhost:8080/api/health → `ok`

3. 프론트 실행

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   확인: http://localhost:5173 → "백엔드 상태: 연결됨"

DB 접속 정보는 기본값 `crawler / crawler` (localhost:5432/crawler)이며,
환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 바꿀 수 있습니다.

## 정책브리핑 보도자료 수집

대상: [정책브리핑 보도자료](https://www.korea.kr/briefing/pressReleaseList.do)
(robots.txt `User-agent: * Allow: /`, 공공누리 제1유형)

- 수집 전에 robots.txt를 읽고, 허용되지 않으면 요청하지 않음
- 요청 사이 최소 1.5초 간격 (`crawler.delay-millis`)
- `newsId` 유니크 제약 + 저장 전 존재 확인으로 같은 글을 다시 받지 않음
- 한 페이지가 전부 이미 저장된 글이면 다음 페이지로 넘어가지 않음

| 메서드 | 주소 | 설명 |
|---|---|---|
| POST | `/api/crawl/korea-policy?maxPages=1` | 수동 수집 실행 (최대 10페이지), 결과 건수 반환 |
| GET | `/api/press-releases?keyword=&ministry=&page=0&size=20` | 목록 (게시일 최신순) |
| GET | `/api/press-releases/{id}` | 상세 (본문 포함) |
