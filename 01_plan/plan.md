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

### Phase 0 — 프로젝트 셋업 ✅ 완료 (2026-08-21)
- [x] Gradle 프로젝트 생성 (패키지명 `com.moonkata.lebenindeutschland`, 모듈 `app`)
- [x] `compose_starter/`의 `Color.kt`, `Type.kt`, `Theme.kt`를 `ui/theme/`로 복사, 패키지명 수정
- [x] Google Fonts (Archivo, Noto Sans KR, Noto Sans Arabic) 연동 + `font_certs.xml` 추가
- [x] build.gradle.kts에 Compose Material3, Room(런타임만, 컴파일러는 Phase 1), Navigation-Compose, ML Kit Translate, WorkManager 의존성 추가
- [x] `LidTheme { AppNavHost() }` 뼈대 구성, placeholder 화면으로 `assembleDebug` 빌드 성공 확인
- 버전: AGP 9.3.1(빌트인 Kotlin, `org.jetbrains.kotlin.android` 플러그인 미적용) · Gradle 9.5.0 · Kotlin/Compose 컴파일러 2.3.21 · compileSdk/targetSdk 37 · minSdk 26
- 알아둘 것: AGP 9.x부터 Kotlin이 내장되어 `kotlin-android` 플러그인을 따로 적용하면 `Cannot add extension with name 'kotlin'` 에러 발생 — `com.android.application` + `org.jetbrains.kotlin.plugin.compose`만 적용
- 알아둘 것: `androidx.compose.ui:ui-text-google-fonts` 1.11.0부터 `GoogleFontProvider` → `GoogleFont.Provider`(중첩 클래스)로 이름 변경됨
- Room `ksp(libs.room.compiler)`는 Phase 1에서 실제 Entity/DAO 작성 시 KSP 플러그인과 함께 추가 예정 (Kotlin 버전과 KSP 버전 매칭 필요)

### Phase 1 — 문제 데이터 확보 & 데이터 계층 ✅ 완료 (2026-08-21)
- [x] 일반 300 + 연방주 16×10 = 460문제를 JSON으로 구조화
- [x] 이미지 포함 문제 처리 (에셋 번들, 38개 PNG)
- [x] Room 스키마: `Question`, `Attempt`, `AttemptAnswer`, `TranslationCacheEntry` (`Topic`은 테이블 대신 정적 객체 `Topics`로 단순화 — 10개 일반 주제 + 16개 주가 고정/소규모라 DB 테이블이 과함)
- [x] 최초 실행 시 데이터 prepopulate 로직 (`LidDatabase.ensureSeeded()` → `QuestionAssetLoader`; 최초 Room `Callback.onCreate` 방식은 비동기라 화면의 첫 조회와 레이스가 나서 Phase 2 중 suspend 함수로 교체)
- [x] 에뮬레이터(Pixel_6, API 36)에 실제 설치해 검증: "문제 460개 로드됨" 확인, 크래시 없음, 독일어 움라우트 정상 렌더링

**데이터 출처:** 직접 BAMF PDF를 조사해 460문제를 처음부터 옮겨 적는 대신,
[flexsurfer/einburgerungstest](https://github.com/flexsurfer/einburgerungstest) (MIT 라이선스) 저장소의
`data.json`을 사용 — 이 저장소 자체가 "BAMF Gesamtfragenkatalog, Stand: 07.05.2025" 공식 데이터를 기반으로
이미 구조화해둔 것이어서 훨씬 안전하고 정확함 (LLM이 검색 스니펫만 보고 460문제를 손으로 재구성하는 것보다
훨씬 신뢰도 높음). 출처/라이선스 고지는 [`app/src/main/assets/questions/SOURCE.md`](../app/src/main/assets/questions/SOURCE.md) 참고.
- 33문제 = 30(일반) + 3(연방주) 구성 웹 조사로 재확인 완료 (기존 가정 맞음)
- `explanationDe`는 전부 `null` — 소스 데이터(공식 BAMF 카탈로그와 동일)에 문항별 해설이 없음. 필요하면 나중에 별도 작성해야 함
- 이미지 문제(예: "어느 문장이 브란덴부르크 주의 문장입니까?")는 4개 보기 이미지가 한 장의 PNG에 합쳐진 형태 — 실제 시험 인쇄물과 동일한 방식, 별도 처리 불필요
- Room이 Kotlin enum(`QuestionCategory`, `AttemptMode`, `TranslationContentType`)을 TypeConverter 없이 TEXT 컬럼으로 네이티브 지원 확인 (Room 2.8.4)
- KSP는 Kotlin 버전과 분리된 자체 버전 체계로 전환됨(`2.3.11`) — AGP 빌트인 Kotlin(2.3.21)과 무관하게 붙여도 정상 동작

### Phase 2 — 핵심 화면 구현 ✅ 완료 (2026-08-21)
- [x] `FrageScreen.kt` + `AnswerRow.kt` + `QuizViewModel.kt` — 정답/오답 상태, 이미지 문제, 진행바, 헤더 전부 스펙대로 구현·실기기 검증
- [x] `SpracheScreen.kt` (언어 9개, 선택 후 Weiter) · `StartScreen.kt` (진행률, 주제 목록, 시험 포스터, Bundesland 블록) · `ErgebnisScreen.kt` (점수 포스터, 주제별 막대, 오답 연습 버튼)
- [x] Navigation 연결 (Sprache → Start → Frage → Ergebnis, 실제 `NavHost`로 교체, 이전 임시 하네스 제거)
- [x] `UserPrefs`(SharedPreferences)로 언어/Bundesland 저장
- [x] 에뮬레이터에서 전체 플로우 실측 검증: 언어선택→홈→주제연습(10문제)→결과화면→오답연습→홈 복귀 시 진행률(GELERNT, 주제별) 반영까지 확인

**진행 중 발견/수정한 버그:**
- Material3 `Button`이 테마의 `shapes`를 무시하고 항상 완전 둥근 pill 모양을 강제함 → `LidButton`(`ui/theme/Theme.kt`)으로 `shape = RectangleShape` 고정해서 전역 사용
- `AnswerRow`의 리딩 accent bar에 `fillMaxHeight()`만 쓰면 부모(Column)의 느슨한 제약 때문에 첫 번째 행이 화면 전체 높이를 먹어버리고 나머지 보기가 화면 밖으로 밀려남 → `Modifier.height(IntrinsicSize.Min)`으로 고정
- `enableEdgeToEdge()` 사용 시 `safeDrawingPadding()`을 안 주면 헤더가 상태바에 가려짐
- Room `Callback.onCreate`의 비동기 prepopulate와 화면의 첫 조회 사이에 레이스 컨디션 발생(설치 직후 "0/0" 표시) → `ensureSeeded()` suspend 함수 + Mutex로 교체, 실제 콘텐츠 렌더링 전 확실히 대기하도록 변경

### Phase 3 — 모의고사 엔진 ✅ 완료 (2026-08-21)
- [x] 33문제(30+3) 랜덤 추출 로직 (`QuestionRepository.examQuestions`) — Home의 "Prüfung simulieren" 포스터에 연결됨
- [x] 60분 카운트다운 타이머 — `QuizViewModel`에서 생성 시점(`examStartedAtMillis`) 기준으로 1초마다 재계산, 헤더에 "N MIN" 표시, 0되면 자동 제출. 회전 등 설정 변경에는 살아남음(ViewModel 특성). **다만 프로세스가 통째로 죽었다가 복귀하는 경우(진짜 강제종료)는 아직 복구 안 됨** — SavedStateHandle이나 DB 기반 재개 로직이 필요, 지금은 알려진 제약사항으로 남겨둠
- [x] 채점 및 `Attempt`/`AttemptAnswer` 저장, 17개 기준 합격/불합격 판정(`QuizViewModel.passed`)까지 구현·검증됨
- [x] 에뮬레이터 실측: Bundesland(Bayern) 선택 후 시험 시작 → "PRÜFUNG 1/33" + "59 MIN"(타이머 감소 확인) 정상 동작

### Phase 4 — 오답 재연습 ✅ 완료 (2026-08-21)
- [x] REVIEW 모드로 FrageScreen 연결 — Ergebnis 화면의 "N falsche Fragen üben"에서 방금 틀린 문제로 재연습 가능
- [x] 홈에서 바로 진입 가능한 **전역** 오답 큐 (`QuestionRepository.globalWrongQuestions()` — 모든 시도 이력 통틀어 최신 답이 오답인 문제) — Home에 "Falsche Fragen üben" 행으로 노출, 개수도 표시. 실기기에서 앱 재시작 후에도 유지되는 것까지 확인

### Phase 5 — 통계 화면 ✅ 완료 (2026-08-21)
- [x] 응시 이력 리스트(`StatistikScreen` VERLAUF) + 시험 점수 추이 막대그래프(최근 12회)
- [x] 주제별 정답률 집계 — 일반 10개 주제 + 연방주 16개 전부, Home과 같은 로직 재사용
- [x] **디자인 스펙에 없던 하단 탭바(ÜBEN/PRÜFUNG/FORTSCHRITT/MEHR)를 이번에 처음 구현** (`LidBottomBar`) — Start/Statistik/Mehr 세 화면에서 공용으로 사용, 4개 탭 전부 실기기에서 동작 확인
- [x] `MehrScreen` 신규 추가(디자인에 없던 화면) — 현재 번역 언어/Bundesland 표시, 언어 재선택 진입점. Phase 6의 언어팩 관리 UI가 여기에 들어갈 자리

### Phase 6 — 온디바이스 번역 ✅ 완료 (2026-08-21)
- [x] ML Kit Translate 연동 (`TranslationEngine` — Task를 코루틴으로 래핑), 언어 모델 다운로드 플로우
- [x] WorkManager 기반 전체 콘텐츠 사전 번역 (`PreTranslateWorker`) + `TranslationCache` — 460문제 × 5필드 = 2,300개 텍스트, 실측 완료까지 약 1분
- [x] 온디맨드 fallback 번역 — 캐시 미스 시 `FrageScreen`에서 즉석 번역 후 캐시에 기록
- [x] 언어팩 관리 화면 (MEHR 탭, `Sprachmodelle` 섹션) — 9개 언어 전부 다운로드/삭제 가능, 진행률(%) 표시
- [x] `FrageScreen`에 번역 토글 버튼(선택 언어 코드 ↔ "DE") + 질문/보기 아래 번역 텍스트 표시, 디자인 스펙대로 accent rule/들여쓰기/색상 적용
- [x] **실기기 검증**: 한국어 선택 → 자동 다운로드+사전번역 → 문제 화면에서 정확한 한국어 번역 확인, 토글 ON/OFF 정상 동작, 터키어 등 추가 언어팩 동시 다운로드도 확인 (여러 언어 지원)
- 사용자의 원래 요구사항인 "여러 나라 언어로 번역해서 볼 수 있게" — 9개 언어 전부 지원, 완전 오프라인(모델 다운로드 후에는 인터넷 불필요) 요구사항 충족

### Phase 7 — 다듬기 (진행 중)
- [x] RTL 레이아웃 (아랍어/페르시아어) — `DirectionalContent`/`GermanText`(`ui/theme/Rtl.kt`)로 구현. 번역 토글이 켜져 있고 언어가 AR/FA일 때만 화면 전체를 `LayoutDirection.Rtl`로 감싸고, 독일어 질문/보기 텍스트만 강제로 LTR 유지. Row의 start→end 순서를 그대로 활용해서 accent bar가 자동으로 trailing(오른쪽) 끝으로 이동 — 별도 분기 코드 없이 자연스럽게 해결됨
- [x] **실기기 검증**: 아랍어로 전환 후 헤더(ABBRECHEN이 오른쪽으로), 진행바, 질문 번역의 accent rule(오른쪽 끝), 보기별 번역까지 전부 미러링되면서 독일어 텍스트(Thüringen 등)는 좌→우 그대로 유지되는 것 확인
- [x] 문제 이미지에 `contentDescription` 추가 (스크린리더용) — 이전엔 `null`이라 TalkBack에서 완전히 무시됐음. 캡션이 있으면 캡션, 없으면 질문 텍스트를 설명으로 사용
- [ ] 아이콘 세트 결정 (Material Symbols 또는 Lucide) — 현재 "✕" 같은 텍스트 글리프만 사용 중, 아직 실제 아이콘 필요성이 크지 않아 보류
- [ ] 나머지 접근성 점검(TalkBack 전체 내비게이션, 터치 타깃 크기 등)은 실기기 스크린리더 세션으로 별도 확인 필요 — 색상 대비는 디자인 스펙(accent700 규칙 등)을 그대로 따르고 있어 기본은 지켜지고 있음

### Phase 8 — 출시 준비
- [ ] 앱 아이콘, 스토어 리스팅 자산
- [ ] 서명/빌드 설정
- [ ] 테스트 (수동 QA, 필요 시 Compose UI 테스트)

---

## 진행 로그
> 최신 항목이 위로 오도록 기록.

### 2026-08-21 (Phase 7 진행 — RTL)
- 아랍어/페르시아어 RTL 레이아웃 구현 (`ui/theme/Rtl.kt`: `DirectionalContent`, `GermanText`, `isRtlLanguage`)
- 핵심 아이디어: Compose의 Row가 이미 `LayoutDirection`을 따라 start→end로 자식을 배치하므로, 화면 전체를 `LayoutDirection.Rtl`로 감싸기만 하면 accent bar 같은 요소들이 자동으로 trailing(오른쪽) 끝으로 이동함 — 별도 "RTL이면 반대로" 분기 코드가 거의 필요 없었음. 독일어 텍스트만 `GermanText`로 강제 LTR
- 실기기에서 아랍어로 전환해 헤더/진행바/번역 accent rule/보기별 번역까지 전부 미러링되고 독일어는 그대로인 것 확인

### 2026-08-21 (Phase 6 완료 — 온디바이스 번역)
- `TranslationEngine`: ML Kit Translate의 `Task<T>` 결과를 `suspendCancellableCoroutine`으로 코루틴화. ML Kit API를 실제 aar(`translate-17.0.3.aar`, `common-18.11.0.aar`)에서 javap로 직접 확인 후 구현(추측 대신 실물 확인)
- `PreTranslateWorker`(CoroutineWorker): 언어 선택 시 자동으로 모델 다운로드 + 460문제×5필드(질문+보기4개) 사전 번역, 50개씩 배치로 Room에 저장. 실측 약 2,300개 텍스트를 1분 내 완료
- `FrageScreen`에 번역 토글 버튼과 번역 텍스트 표시 추가 — 질문 아래는 accent rule + 12dp 들여쓰기, 보기 아래는 28dp 들여쓰기로 디자인 스펙 그대로 구현. 캐시 미스 시 온디맨드로 즉석 번역 후 캐시 기록
- `Sprachmodelle`(언어팩 관리) 섹션을 Mehr 화면에 추가 — 9개 언어 전부 다운로드/삭제 가능(진행률 %), 한 언어에만 묶이지 않고 여러 언어 동시 지원 확인
- **실기기 전체 플로우 검증**: 한국어 선택 → 모델 자동 다운로드 → 백그라운드 사전 번역(로그로 실제 NMT 엔진 동작 확인) → 문제 화면에서 정확한 한글 번역 렌더링 → 토글 OFF 시 사라짐 → 언어팩 관리에서 터키어 추가 다운로드도 동시 진행 확인
- 원래 사용자 요구사항("여러 나라 언어로 번역, 완전 오프라인")을 충족하는 핵심 기능이 이걸로 전부 구현됨

### 2026-08-21 (Phase 5 완료)
- 하단 탭바(`LidBottomBar`) 신규 구현 — 원래 4화면 디자인 스펙에는 있었지만 Phase 2에서는 의도적으로 미룬 항목. ÜBEN(Start)/PRÜFUNG(시험 즉시 시작)/FORTSCHRITT(통계)/MEHR(설정) 4탭 모두 실기기 확인
- `StatistikScreen` 신규: 응시 이력(모드/날짜/점수/합격여부), 최근 시험 12회 점수 막대그래프, 일반+연방주 전체 26개 주제 정답률
- `MehrScreen` 신규(디자인에 없던 화면): 현재 번역 언어·Bundesland 표시, 언어 재선택
- Start/Statistik/Mehr 세 화면 모두 `Column(weight(1f).verticalScroll) + LidBottomBar` 구조로 통일, `statusBarsPadding()`으로 변경(바텀바가 자체적으로 `navigationBarsPadding()` 처리하므로 기존 `safeDrawingPadding()`은 중복이라 교체)
- 언어 재선택 시(Mehr → Sprache) 네비게이션 백스택이 완전히 깔끔하게 정리되지 않는 사소한 known issue 있음 — 크래시나 데이터 손실은 없고 뒤로가기 동작만 살짝 어색할 수 있음

### 2026-08-21 (Phase 3, 4 완료)
- 60분 시험 타이머 구현 (`QuizViewModel` 내 코루틴, `examStartedAtMillis` 기준 1초마다 재계산), 헤더 "N MIN" 표시, 0초 시 자동 제출
- 전역 오답 큐 구현 (`QuestionRepository.globalWrongQuestions()`), Home에 개수와 함께 노출
- 디자인 번들에 없던 **Bundesland 선택 화면**(`BundeslandPickerScreen`)을 새로 만듦 — 16개 주 단순 목록, 탭하면 즉시 저장 후 뒤로. 이게 있어야 시험의 30+3 구성과 주(州)별 연습이 실제로 동작함
- Home의 BUNDESLAND 블록을 "미선택 시 선택 화면으로, 선택 시 연습 시작"으로 분기하도록 수정
- 에뮬레이터 실측: Bundesland 선택 → 시험 시작 → "PRÜFUNG 1/33" + "59 MIN"(타이머 감소) 확인, 전역 오답 큐가 앱 재시작 후에도 유지되는 것 확인

### 2026-08-21 (Phase 2 완료 + Phase 3/4 상당 부분)
- 4개 화면(Sprache/Start/Frage/Ergebnis) 전부 구현, 실제 NavHost로 연결, 임시 테스트 하네스 제거
- 주제 연습은 전체 문제(최대 70개)가 아니라 10문제로 세션을 제한하도록 수정 (Bundesland 연습과 동일한 세션 크기)
- 33=30+3 시험 문제 구성, 17개 합격 판정까지 구현됨 — 남은 건 60분 타이머뿐이라 Phase 3 대부분 완료
- 세션 단위 오답 재연습(Ergebnis → "N falsche Fragen üben")까지 구현됨 — Phase 4는 홈에서 진입하는 전역 오답 큐만 남음
- 에뮬레이터에서 전체 사용자 플로우 실측: 언어선택 → 홈 → 주제연습 → 결과 → 오답연습 → 홈 복귀 시 진행률 반영까지 한 번에 확인
- 버그 4개 발견·수정: Material3 Button이 테마 shape 무시(전역 `LidButton`으로 해결), `fillMaxHeight()`가 부모의 느슨한 제약 때문에 답변 행 하나가 화면을 다 먹는 레이아웃 버그, edge-to-edge 상태바 겹침, Room prepopulate 레이스 컨디션

### 2026-08-21 (Phase 1 완료)
- BAMF 공식 데이터 기반 MIT 라이선스 오픈 데이터셋(flexsurfer/einburgerungstest)을 찾아 우리 스키마로 변환 — 460문제 + 이미지 38개
- Room 엔티티/DAO 4종 구현 (`Question`, `Attempt`, `AttemptAnswer`, `TranslationCacheEntry`), `Topic`은 정적 객체로 단순화
- `LidDatabase`의 `Callback.onCreate`에서 최초 실행 시 assets JSON을 파싱해 자동 prepopulate
- 패키지명을 `com.moonkata.lebenindeutschland`로 변경 (사용자 요청)
- 에뮬레이터(Pixel_6) 실제 설치·실행으로 460문제 로드 및 크래시 없음 확인
- Room 스키마 export 활성화 (`app/schemas/`)

### 2026-08-21 (Phase 0 완료)
- Android 프로젝트 뼈대 생성 완료, `./gradlew assembleDebug` 빌드 성공 (디버그 APK 출력 확인)
- 패키지명 `com.moonkata.lebenindeutschland`, Compose 테마(`LidTheme`)와 `AppNavHost` 뼈대 구성
- 버전 조합: AGP 9.3.1(빌트인 Kotlin) · Gradle 9.5.0 · Kotlin/Compose 컴파일러 2.3.21 · compileSdk/targetSdk 37 · minSdk 26
- Room/Navigation/WorkManager/ML Kit Translate 의존성 선언 완료 (Room의 KSP 컴파일러 연결은 Phase 1으로 미룸)
- `local.properties`(SDK 경로)는 gitignore로 제외, 커밋에는 포함 안 됨

### 2026-08-21 (설계)
- 핵심 기능 5가지 확정: 정확한 문제 데이터(일반+주별), 모의고사, 오답 재연습, 통계, 온디바이스 AI 번역
- 번역 엔진 결정: **ML Kit Translation** (무료 확인 완료, 필요 언어 전부 지원) — 앱 내장 커스텀 모델 대신 채택
- 번역 캐싱 전략 확정: 언어팩 다운로드 후 전체 콘텐츠 백그라운드 사전 번역 → Room 캐시, 이후 완전 오프라인
- 데이터 소스 결정: BAMF 공식 카탈로그 웹 조사로 진행
- Phase를 0~8로 재구성 (데이터 확보 → 화면 → 시험엔진 → 오답연습 → 통계 → 번역 → 다듬기 → 출시)

### (이전) 2026-08-21
- 저장소 생성, README/.gitignore(Android) 세팅, 디자인 zip 압축 해제, 최초 plan.md 작성

## 다음 할 일 (Next Up)
1. **핵심 기능 5가지(문제 데이터, 모의고사, 오답 재연습, 통계, 온디바이스 번역) 전부 구현 완료.** RTL, 이미지 접근성, Bundesland 재선택까지 완료. 남은 Phase 7 항목: 아이콘 세트, TalkBack 전체 감사
2. Phase 8(출시 준비: 앱 아이콘, 스토어 자산, 서명, 테스트)은 아직 시작 전 — 이 단계는 브랜딩/스토어 문구 등 사용자 결정이 필요한 항목이 많음
3. 알려진 제약: 시험 도중 앱 프로세스가 완전히 강제종료되면 타이머/진행상황이 복구되지 않음 (화면 회전은 문제없음)
4. 언어 재선택 시 네비게이션 백스택이 완전히 정리되지 않는 사소한 이슈
5. 번역 텍스트가 길 경우 레이아웃 줄바꿈/여러 줄 처리는 육안 확인만 했고 극단적으로 긴 문자열에 대한 별도 검증은 아직 안 함
6. 자동화된 테스트(Compose UI 테스트 등)는 아직 없음 — 지금까지는 전부 에뮬레이터 수동/스크립트 조작으로 검증
