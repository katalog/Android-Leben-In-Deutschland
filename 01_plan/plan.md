# Leben in Deutschland — Android 앱 개발 계획

> 이 문서는 개발 진행 상황을 계속 추적하는 살아있는 문서입니다.
> 작업할 때마다 "진행 로그"와 "다음 할 일"을 갱신합니다.
> 디자인 스펙 원본은 [`02_ui_design_handoff_lid_app/README.md`](../02_ui_design_handoff_lid_app/README.md) 참고.

## 앱 개요
- 독일 귀화시험(Einbürgerungstest / "Leben in Deutschland") 대비 학습 앱
- 문제 은행: 일반 300문제 + 연방주(Bundesland)별 10문제
- 시험 모드: 33문제 / 60분 / 17개 이상 정답 시 합격 (실제 시험 규정과 동일)
- 핵심 특징: **독일어가 항상 주 언어**, 번역은 켜고 끌 수 있는 보조 레이어
  (지원 언어: TR, KO, AR, RU, UK, EN, FA, RO, ES)
- 오프라인 동작 (문제/번역/진행상황은 로컬 Room DB)

## 기술 스택
- Jetpack Compose (Material3, 커스텀 디자인 시스템 — `compose_starter/` 참고)
- Room (오프라인 DB: 문제, 번역, 주제별 진행상황)
- 폰트: Archivo(기본), Noto Sans KR / Noto Sans Arabic(번역 텍스트 전용, `LidType.translation` 사용)
- Radius 0, elevation 없음, 2dp divider로 구조 표현 (디자인 시스템 규칙)

## 디자인 참고
`02_ui_design_handoff_lid_app/`의 파일들을 반드시 참고:
- `Leben in Deutschland App.dc.html` — 전체 화면 프로토타입 (브라우저로 열기)
- `styles.css` — 디자인 토큰 원본
- `compose_starter/Color.kt`, `Type.kt`, `Theme.kt` — 토큰을 Kotlin으로 변환한 시작 파일 (그대로 복사해서 사용)
- `compose_starter/README.md` — 설치 방법 및 주의사항 (Radius 0, elevation 금지, 번역 텍스트 폰트 규칙 등)
- `README.md` — 4개 화면(언어선택/홈/문제/결과)의 완전한 스펙 (색상, 타이포, 간격, 상태별 스타일까지 상세 기술됨)

## 화면 목록
1. **SprachePicker** (언어 선택) — 최초 실행 진입점
2. **Start** (홈) — 진행률, 시험 시작, 주제별 연습, Bundesland
3. **Frage** (문제) — 연습/시험 공용 화면, 앱의 핵심
4. **Ergebnis** (결과) — 점수, 주제별 성적, 오답 복습 유도

## 개발 단계 (Phase)

### Phase 0 — 프로젝트 셋업
- [ ] Android Studio 프로젝트 생성 (Compose 템플릿, 패키지명 결정)
- [ ] `compose_starter/`의 `Color.kt`, `Type.kt`, `Theme.kt`를 `ui/theme/`로 복사, 패키지명 수정
- [ ] Google Fonts (Archivo, Noto Sans KR, Noto Sans Arabic) 연동 + `font_certs.xml` 추가
- [ ] build.gradle.kts에 Compose Material3, Room, Navigation-Compose 의존성 추가
- [ ] `LidTheme { AppNavHost() }` 뼈대 구성

### Phase 1 — 데이터 계층
- [ ] `Question`, `Translation`, `Bundesland` 데이터 모델 설계
- [ ] Room 스키마 (`QuestionDao`, `LidDatabase`)
- [ ] 300 일반문제 + Bundesland별 10문제 데이터 소스 확보/입력 (시딩 전략 결정: assets JSON → Room prepopulate)
- [ ] 9개 언어 번역 데이터 연결

### Phase 2 — 핵심 화면 구현
- [ ] `FrageScreen.kt` (문제 화면) — 스펙상 가장 먼저 만들 것을 권장 (전체 상태 로직을 담고 있음)
- [ ] `SpracheScreen.kt` (언어 선택)
- [ ] `StartScreen.kt` (홈)
- [ ] `ErgebnisScreen.kt` (결과)
- [ ] Navigation 연결 (언어선택 → 홈 → 문제 → 결과)

### Phase 3 — 상태/로직
- [ ] 전역 상태: `selectedLanguage`, `translationsVisible`, `bundesland` (영속화)
- [ ] `QuizViewModel`: 문제 진행, 정답 체크, 연습/시험 모드 분기
- [ ] 시험 타이머 (60:00 카운트다운, 0초 시 자동 종료) — 프로토타입에는 미구현, 실제 구현 필요
- [ ] 주제별 진행률 계산 및 저장

### Phase 4 — 다듬기
- [ ] RTL 레이아웃 (아랍어/페르시아어) — 독일어 텍스트는 LTR 유지
- [ ] 아이콘 세트 결정 (Material Symbols 또는 Lucide — 디자인 미확정 영역)
- [ ] 접근성 점검 (색상 대비, 폰트 크기 등 — accent 텍스트는 accent700 사용 규칙 준수)
- [ ] (선택) 대안 번역 레이아웃 — Side-by-side, Full RTL (요청 시에만)

### Phase 5 — 출시 준비
- [ ] 앱 아이콘, 스토어 리스팅 자산
- [ ] 서명/빌드 설정
- [ ] 테스트 (수동 QA, 필요 시 Compose UI 테스트)

---

## 진행 로그
> 최신 항목이 위로 오도록 기록.

### 2026-08-21
- 저장소 생성, README/.gitignore(Android) 세팅
- 디자인 핸드오프 zip 압축 해제 → `02_ui_design_handoff_lid_app/`
- 이 계획 문서(`01_plan/plan.md`) 작성. 아직 코드 작업 시작 전 (Phase 0 착수 전).

## 다음 할 일 (Next Up)
1. Android Studio 프로젝트 생성 및 패키지명 결정
2. Phase 0 셋업 진행 (테마 파일 이식, 폰트 연동)
