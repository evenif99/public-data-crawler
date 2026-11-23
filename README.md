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
