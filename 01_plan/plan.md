# Leben in Deutschland — Android 앱 개발 계획

> 이 문서는 개발 진행 상황을 계속 추적하는 살아있는 문서입니다.
> 작업할 때마다 "진행 로그"와 "다음 할 일"을 갱신합니다.
> 화면 디자인 스펙 원본은 [`02_ui_design_handoff_lid_app/README.md`](../02_ui_design_handoff_lid_app/README.md) 참고.

## 앱 목표 (사용자 요구사항)
1. **문제를 제대로 가져오기** — 일반 300문제 + 연방주(Bundesland)별 추가 10문제, 공식 카탈로그 기준 정확하게
2. **가상 시험(모의고사)** — 실제 시험 규정과 동일하게 (33문제, 60분, 17개 이상 정답 합격)
3. **오답 다시 연습하기** — 시험에서 틀린 문제만 모아서 재연습
4. **시험 통계** — 응시 이력, 점수 추이, 주제별 정답률
5. **완전 로컬 AI 번역** — 인터넷 없이 기기 내에서 독일어 문장을 여러 언어로 번역

## 기술 스택
- Jetpack Compose (Material3, 커스텀 디자인 시스템 — `compose_starter/` 참고)
- Room (오프라인 DB: 문제, 시험 이력, 번역 캐시)
- **ML Kit Translation (on-device)** — 번역 엔진 (아래 "핵심 기능 설계" 참고)
- 폰트: Archivo(기본), Noto Sans KR / Noto Sans Arabic(번역 텍스트 전용)
- Radius 0, elevation 없음, 2dp divider로 구조 표현 (디자인 시스템 규칙)

## 디자인 참고
`02_ui_design_handoff_lid_app/`의 파일들을 반드시 참고:
- `Leben in Deutschland App.dc.html` — 전체 화면 프로토타입 (브라우저로 열기)
- `styles.css` — 디자인 토큰 원본
- `compose_starter/Color.kt`, `Type.kt`, `Theme.kt` — 토큰을 Kotlin으로 변환한 시작 파일 (그대로 복사해서 사용)
- `compose_starter/README.md` — 설치 방법 및 주의사항 (Radius 0, elevation 금지, 번역 텍스트 폰트 규칙 등)
- `README.md` — 언어선택/홈/문제/결과 4개 화면의 완전한 스펙 (색상, 타이포, 간격, 상태별 스타일)
- ⚠️ 원래 디자인 문서는 "번역은 정적 데이터"를 가정했지만, 실제로는 **ML Kit 온디바이스 번역 + 캐시**로 대체한다 (아래 참고). 화면 레이아웃/스타일 스펙 자체는 그대로 유효함.

---

## 핵심 기능 설계

### 1. 문제 데이터 — 공식 카탈로그 기반
- 출처: BAMF(연방이민난민청)가 공개하는 **Gemeinsamer Fragenkatalog** (일반 300문제 + 16개 연방주 × 10문제)
- 웹 조사로 구조화된 데이터(JSON)로 정리 → `assets/questions/general.json`, `assets/questions/bundesland_<code>.json`
- 일부 문제는 이미지 포함(주 깃발, 지도, 역사 사진 등) — 이미지 에셋 처리 필요 (drawable 또는 assets 번들)
- Room 테이블: `Question(id, category: GENERAL|BUNDESLAND, bundesland, topicId, textDe, answerA/B/C/D, correctAnswerIndex, explanationDe, imageAsset?)`
- 앱 최초 실행 시 1회 Room prepopulate (asset DB 또는 JSON import worker)

### 2. 모의고사(가상 시험) 엔진
- 실제 시험 규정: **총 33문제 = 일반 30문제 + 선택한 연방주 3문제**, 60분, **17개 이상 정답 시 합격**
  (※ 30+3 비율은 일반적으로 알려진 공식 규정 — 데이터 조사 단계에서 BAMF 원문으로 재확인)
- 각 시도(attempt)마다 중복 없이 랜덤 추출
- 60:00 카운트다운 타이머 — ViewModel + coroutine, 프로세스 종료돼도 `startedAt` 기준으로 남은 시간 재계산, 0초 시 자동 제출
- Room 테이블: `Attempt(id, mode: PRACTICE|EXAM, bundesland, startedAt, finishedAt, correctCount, totalQuestions, passed, durationSeconds)`,
  `AttemptAnswer(attemptId, questionId, pickedAnswerIndex, isCorrect)`

### 3. 오답 재연습
- "틀린 문제" = 각 문제별 **가장 최근 AttemptAnswer가 오답**인 것들의 집합 (시험 결과 화면의 "N개 틀린 문제 연습하기" 버튼과 연결 — 기존 디자인에 이미 있음)
- 별도 연습 모드(`REVIEW`)로 FrageScreen 재사용, 문제 소스만 오답 큐로 교체
- 정답을 맞히면 해당 문제는 오답 큐에서 제거(단, 이력은 통계용으로 계속 보존)

### 4. 통계 (기존 "FORTSCHRITT" 탭 확장)
- 응시 이력 리스트: 날짜, 점수(X/33), 합격 여부, 소요 시간
- 점수 추이 그래프 (회차별 점수)
- 주제별 정답률 (Result 화면에 이미 있는 막대 스타일 재사용 — accent = 75% 미만 취약 주제)
- 연습 모드 통계와 시험 모드 통계는 구분해서 볼 수 있게

### 5. 온디바이스 AI 번역 — ML Kit Translation
- **확인 완료: ML Kit 온디바이스 API는 완전 무료** (사용량 과금 없음, Google 공식 문서 확인)
- `com.google.mlkit:translate` 라이브러리, 100개 이상 언어 지원 — 필요한 9개 언어(TR/KO/AR/RU/UK/EN/FA/RO/ES) + 독일어 전부 지원 확인
- 동작 방식:
  1. 언어 선택 시 해당 DE↔언어 모델이 기기에 있는지 확인
  2. 없으면 다운로드 필요(1회, 언어당 약 30MB, 이때만 인터넷 필요 — Wi-Fi 전용 옵션 제공 가능)
  3. 다운로드 완료 후 **번역 연산 자체는 완전히 기기 내부에서, 인터넷 없이** 수행됨
- **캐싱 전략**: 문제 데이터는 고정/유한(약 460문제 × 6개 텍스트 필드)이므로, 언어 모델 다운로드 직후 백그라운드(WorkManager)로 전체 콘텐츠를 한 번에 미리 번역해서 Room `TranslationCache` 테이블에 저장 → 이후에는 즉시 조회, 재번역 없음, 완전 오프라인
  - Room 테이블: `TranslationCache(contentId, contentType, languageCode, translatedText)` — PK: (contentId, contentType, languageCode)
  - 사전 번역이 아직 안 끝났을 경우를 대비해 온디맨드(그때그때) 번역 fallback도 유지
- "MEHR" 탭에 번역 언어팩 관리 화면 추가 (다운로드 상태, 용량, 삭제)

---

## 화면 목록 (기존 4개 + 신규)
1. **SprachePicker** (언어 선택) — 최초 실행 진입점, 선택 시 ML Kit 모델 다운로드 트리거
2. **Start** (홈) — 진행률, 시험 시작, 주제별 연습, Bundesland
3. **Frage** (문제) — 연습/시험/오답복습 공용 화면, 앱의 핵심
4. **Ergebnis** (결과) — 점수, 주제별 성적, 오답 복습 유도
5. **(신규) Statistik/Fortschritt** — 응시 이력 + 추이 그래프 + 주제별 통계 (FORTSCHRITT 탭 확장)
6. **(신규) Sprachmodelle** — 번역 언어팩 다운로드/관리 (MEHR 탭 하위)

## 개발 단계 (Phase)

### Phase 0 — 프로젝트 셋업
- [ ] Android Studio 프로젝트 생성 (Compose 템플릿, 패키지명 결정)
- [ ] `compose_starter/`의 `Color.kt`, `Type.kt`, `Theme.kt`를 `ui/theme/`로 복사, 패키지명 수정
- [ ] Google Fonts (Archivo, Noto Sans KR, Noto Sans Arabic) 연동 + `font_certs.xml` 추가
- [ ] build.gradle.kts에 Compose Material3, Room, Navigation-Compose, ML Kit Translate, WorkManager 의존성 추가
- [ ] `LidTheme { AppNavHost() }` 뼈대 구성

### Phase 1 — 문제 데이터 확보 & 데이터 계층
- [ ] BAMF 공식 Fragenkatalog 웹 조사, 일반 300 + 연방주 16×10 문제를 JSON으로 구조화
- [ ] 이미지 포함 문제 처리 방식 확정 (에셋 번들)
- [ ] Room 스키마: `Question`, `Topic`, `Attempt`, `AttemptAnswer`, `TranslationCache`
- [ ] 최초 실행 시 데이터 prepopulate 로직

### Phase 2 — 핵심 화면 구현 (기존 4화면)
- [ ] `FrageScreen.kt` — 가장 먼저 (전체 상태 로직을 담고 있음)
- [ ] `SpracheScreen.kt`, `StartScreen.kt`, `ErgebnisScreen.kt`
- [ ] Navigation 연결

### Phase 3 — 모의고사 엔진
- [ ] 33문제(30+3) 랜덤 추출 로직
- [ ] 60분 카운트다운 타이머 (프로세스 종료 대응)
- [ ] 채점 및 `Attempt`/`AttemptAnswer` 저장

### Phase 4 — 오답 재연습
- [ ] 오답 큐 조회 로직 (최신 시도 기준 오답)
- [ ] REVIEW 모드로 FrageScreen 연결

### Phase 5 — 통계 화면
- [ ] 응시 이력 리스트 + 점수 추이 그래프
- [ ] 주제별 정답률 집계

### Phase 6 — 온디바이스 번역
- [ ] ML Kit Translate 연동, 언어 모델 다운로드 플로우
- [ ] WorkManager 기반 전체 콘텐츠 사전 번역 + `TranslationCache`
- [ ] 온디맨드 fallback 번역
- [ ] 언어팩 관리 화면 (MEHR 탭)

### Phase 7 — 다듬기
- [ ] RTL 레이아웃 (아랍어/페르시아어) — 독일어 텍스트는 LTR 유지
- [ ] 아이콘 세트 결정 (Material Symbols 또는 Lucide)
- [ ] 접근성 점검 (색상 대비, 폰트 크기 등)

### Phase 8 — 출시 준비
- [ ] 앱 아이콘, 스토어 리스팅 자산
- [ ] 서명/빌드 설정
- [ ] 테스트 (수동 QA, 필요 시 Compose UI 테스트)

---

## 진행 로그
> 최신 항목이 위로 오도록 기록.

### 2026-08-21
- 핵심 기능 5가지 확정: 정확한 문제 데이터(일반+주별), 모의고사, 오답 재연습, 통계, 온디바이스 AI 번역
- 번역 엔진 결정: **ML Kit Translation** (무료 확인 완료, 필요 언어 전부 지원) — 앱 내장 커스텀 모델 대신 채택
- 번역 캐싱 전략 확정: 언어팩 다운로드 후 전체 콘텐츠 백그라운드 사전 번역 → Room 캐시, 이후 완전 오프라인
- 데이터 소스 결정: BAMF 공식 카탈로그 웹 조사로 진행
- Phase를 0~8로 재구성 (데이터 확보 → 화면 → 시험엔진 → 오답연습 → 통계 → 번역 → 다듬기 → 출시)
- 아직 코드 작업 시작 전.

### (이전) 2026-08-21
- 저장소 생성, README/.gitignore(Android) 세팅, 디자인 zip 압축 해제, 최초 plan.md 작성

## 다음 할 일 (Next Up)
1. BAMF 공식 Fragenkatalog 웹 조사 → 문제 데이터 JSON 구조화 (Phase 1 착수)
2. Android Studio 프로젝트 생성 및 패키지명 결정 (Phase 0)
3. 33문제 = 30+3 비율, 이미지 포함 문제 존재 여부 등 조사 단계에서 재확인
