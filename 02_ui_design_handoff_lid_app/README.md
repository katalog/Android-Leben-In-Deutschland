# Handoff: Leben in Deutschland — multilingual test app (Android)

## Overview
A study app for the German "Leben in Deutschland" / Einbürgerungstest. Fixed catalogue:
300 general questions + 10 per Bundesland. Exam simulation is regulation-accurate:
33 questions, 60 minutes, 17 correct to pass.

The distinguishing feature is the language model: **German is always the primary text.**
Translations are a second layer the learner can switch on and off, because the real exam
is German-only. Supported translation languages in the prototype: Türkçe, 한국어, العربية,
Русский, Українська, English, فارسی, Română, Español.

## About the design files
`Leben in Deutschland App.dc.html` in this bundle is a **design reference built in HTML** —
a prototype showing intended look and behavior. It is not production code to port line by line.
The task is to **recreate these screens natively in Android** (Jetpack Compose recommended;
`compose_starter/` contains the theme translated to Kotlin already). Use the codebase's existing
patterns where they exist.

To view the prototype: it renders in a browser. `ios-frame.jsx` is only the iPhone bezel used for
presentation — it is not part of the product and must not be reproduced.

## Fidelity
**High-fidelity.** Colors, type, spacing and rules are final and come from the Modernist design
system (`styles.css`, tokens listed below). Recreate them exactly. What is *not* final: icons
(none drawn yet — use Material symbols or Lucide), and the illustration/photography treatment.

---

## Design tokens

### Color
| Token | Hex | Use |
|---|---|---|
| bg | `#F3F2F2` | every screen background |
| surface | `#EAE9E9` | cards, raised fills |
| text | `#201E1D` | all body and heading ink |
| accent | `#EC3013` | primary action, correct-answer bar, red poster panels |
| accent100 | `#FFF2EF` | correct-answer row tint, hover tint |
| accent700 | `#AE1800` | accent-colored *small* text (accessible step) |
| accent800 | `#7C1405` | translation text inside a tinted row |
| neutral200 | `#EAE7E7` | picked-but-wrong answer row fill |
| neutral300 | `#D7D3D3` | progress-bar track |
| neutral600 | `#7D7979` | de-emphasised ink |
| neutral700 | `#605D5D` | secondary/translation ink |
| neutral800 | `#444141` | filled progress bar (non-accent) |
| divider | `#201E1D` at 40% alpha | **2 dp** rules |

Rules: never use `accent` for paragraph-size text — use `accent700`. Small accent text on the
tinted row uses `accent800`.

### Type
Single family: **Archivo** (Google Fonts; weights 400/500/600/700/800).
Translation text additionally needs **Noto Sans KR** and **Noto Sans Arabic** fallbacks.

| Role | Size / line-height | Weight | Tracking |
|---|---|---|---|
| Screen display (home greeting, question) | 30–32 sp / 1.06 | 800 | -0.035em |
| Question (quiz) | 26 sp / 1.10 | 800 | -0.03em |
| Poster heading (red panels) | 27 sp / 1.05 | 800 | -0.035em |
| Score numeral | 74 sp / 0.82 | 800 | -0.05em, tabular |
| Answer text | 15 sp / 1.35 | 500 (600 when correct) | 0 |
| Answer letter A–D | 14 sp / 1.35 | 800 | 0 |
| Translation line | 15 sp / 1.45 | 500 | 0 |
| Answer translation | 13.5 sp / 1.5 | 400 | 0 |
| Label / kicker (all caps) | 11–12 sp / 1.0 | 700 | 0.10em |
| Explanation body | 13 sp / 1.55 | 400 | 0 |
| List row title | 15–17 sp / 1.25 | 600–700 | 0 |

Numerals in counters and scores use tabular figures (`fontFeatureSettings = "tnum"`).

### Spacing / shape
4, 8, 12, 16, 24, 32 dp. Screen horizontal padding is **20 dp** throughout.
**Corner radius is 0 dp everywhere. No rounded corners, no elevation shadows on content.**
Structure is drawn with 2 dp dividers instead. Bottom tab bar height 42 dp + system inset.

---

## Screens

### 1. Language picker — `SprachePicker`
Purpose: choose the translation language before anything else. First launch entry point.

Layout, top to bottom:
- 22/20/18 dp padding block: kicker `SCHRITT 1 VON 2` (accent700, 11 sp, 0.12em) →
  display heading "In welcher Sprache sollen wir übersetzen?" (32 sp/1.06, w800, -0.035em) →
  sub copy "Die Fragen bleiben immer auf Deutsch. Die Prüfung ist auf Deutsch."
  (14 sp/1.5, neutral700).
- 2 dp divider, then a scrolling list. Each row: 13/20 dp padding, 2 dp bottom divider,
  space-between. Left = native name (17 sp/1.35, w700) over German name (12 sp, neutral600).
  Right = 2-letter code chip, 5/7 dp padding, 11 sp w700, 0.06em.
- Selected row: background `accent100` + **4 dp accent bar inset on the leading edge**;
  its code chip inverts to accent fill / white text. Unselected chip = 2 dp divider border,
  neutral700 text.
- Footer above the nav bar: 2 dp divider, 14/20 dp padding, full-width primary button "Weiter".

Rows (native / German / code): Türkçe·Türkisch·TR, 한국어·Koreanisch·KO, العربية·Arabisch·AR,
Русский·Russisch·RU, Українська·Ukrainisch·UK, English·Englisch·EN, فارسی·Persisch·FA,
Română·Rumänisch·RO, Español·Spanisch·ES.

### 2. Home — `Start`
- Header 20 dp: "Guten Tag,\nAmina" (30 sp/1.06, w800) with the active language code as an
  outline tag on the trailing baseline.
- Progress band between two 2 dp dividers, 16/20 dp: label `GELERNT` / value `214 / 310`
  (15 sp w800, tabular) on one row, then a 10 dp bar — neutral300 track, accent fill at 69%.
- **Red poster panel** (accent fill, white ink), 22/20/24 dp, tappable → exam:
  "Prüfung\nsimulieren" (27 sp/1.05 w800) + `33 FRAGEN · 60 MIN · 17 ZUM BESTEHEN`
  (12 sp w600, 0.08em). 2 dp bottom divider. This is the only red field on the screen.
- `THEMEN ÜBEN` label, then topic rows (2 dp top divider each, 14/20 dp, space-between):
  Politik in der Demokratie 38/50 · Geschichte und Verantwortung 52/60 ·
  Mensch und Gesellschaft 44/44 · Bayern — Landesfragen 3/10. Tapping any row starts practice.
- Bundesland footer block: label `BUNDESLAND` → "Bayern" (20 sp w800), trailing
  `+10 FRAGEN` in accent700.
- Bottom bar: 4 equal cells separated by 2 dp vertical rules, labels 10 sp w700 0.08em:
  ÜBEN (accent700 when active) · PRÜFUNG · FORTSCHRITT · MEHR.

### 3. Question — `Frage` (practice and exam use the same screen)
This is the screen that matters. Two modes differ only in the header and the exit condition.

- Header row 12/20/10 dp: `✕ ABBRECHEN` (12 sp w700) · centre counter
  `ÜBEN 4 / 12` or `PRÜFUNG 12 / 33` (12 sp w700, 0.10em, tabular) · trailing meta
  `2 RICHTIG` in practice, `44 MIN` in exam (accent700).
- 4 dp progress bar, neutral300 track, accent fill = position/total.
- Question block between 2 dp dividers, 20 dp padding: German question 26 sp/1.10 w800
  -0.03em. When translation is on, below it: 12 dp gap, **2 dp accent rule on the leading
  edge**, 12 dp inset, translation 15 sp/1.45 w500 neutral700.
- Answer rows: 14/20/15 dp, 2 dp bottom divider. Row = letter (16 dp wide column, 14 sp w800)
  + 12 dp gap + German answer (15 sp/1.35 w500). When translation is on, the translation sits
  under it: 9 dp top margin, 28 dp leading indent, 13.5 sp/1.5 w400 neutral700.
- Row states after the user picks:
  - correct row → `accent100` fill, 4 dp accent leading bar, answer text w600,
    letter accent700, translation accent800.
  - picked-and-wrong row → `neutral200` fill, text and letter neutral600, translation neutral500.
  - untouched rows → unchanged.
  - hover/pressed (before answering) → `accent100`.
- After answering, an explanation block appears at the end of the list: 16/20 dp,
  2 dp bottom divider, "**Erklärung.** …" 13 sp/1.55, neutral700 with bold black lead-in.
- Footer: 2 dp divider, 14/20 dp, two buttons in a row with 10 dp gap — a secondary button
  toggling the translation whose label is the language code when on and `DE` when off, and a
  full-width primary button. Primary label is "Antwort wählen" (disabled-feeling) until an
  answer is picked, then "Weiter".

Only one answer can be picked per question; picking is final (no changing).

### 4. Result — `Ergebnis`
- Red poster block, accent fill, white ink, 24/20/26 dp: kicker `MODELLTEST · 60 MIN` →
  score `28` (74 sp/0.82 w800 -0.05em tabular) with `/ 33` (20 sp w700) on the baseline →
  `BESTANDEN` (22 sp w800) → one line of the same sentence in the learner's language.
- `NACH THEMA` label, then per-topic rows (2 dp top divider, 13/20 dp): name (14 sp w600) +
  score (13 sp w800 tabular) on one row, then a 6 dp bar. Bar fill is **accent when below 75%**,
  neutral800 otherwise — red marks the weak areas.
  Rows: Politik in der Demokratie 9/13 · Geschichte und Verantwortung 10/11 ·
  Mensch und Gesellschaft 6/6 · Bayern 3/3.
- Closing paragraph, 16/20 dp, 13 sp/1.55, neutral700, with the weak topic named in
  accent700 w700.
- Footer: secondary "Start" + primary "5 falsche Fragen üben".

### Alternative translation layouts (documented, not the default)
The prototype shows two more models under turn 2. Build only if asked:
- **Side-by-side**: question and answers split into two columns by a 2 dp vertical rule,
  German left (19 sp w800 / 13.5 sp w500), translation right (15 sp / 13 sp, neutral700),
  letter in a 34 dp leading column. Denser; suited to B1 self-checking.
- **Full RTL** (Arabic, Farsi): mirror the whole layout (`LayoutDirection.Rtl`). German text
  stays LTR and left-aligned inside the mirrored frame; the accent rule moves to the trailing
  (right) edge; Arabic sets in Noto Sans Arabic at 14–16 sp / 1.6.

---

## Interactions & behavior
- Language picker → tap a row selects (does not navigate) → "Weiter" navigates to Home.
- Home → red panel or PRÜFUNG tab starts the exam; any topic row or ÜBEN starts practice;
  FORTSCHRITT opens the result screen.
- Question → tapping an answer row locks the answer, reveals correct/wrong styling and the
  explanation, and enables "Weiter". "Weiter" advances; in exam mode the final question goes to
  the result screen. `✕ ABBRECHEN` returns Home.
- Translation toggle is global and persists across questions and sessions.
- Exam timer counts down from 60:00 and ends the exam at zero (not implemented in the prototype).
- No animations beyond a ~120 ms row-background transition. Keep it flat and instant.

## State
```
selectedLanguage: LanguageCode      // persisted
translationsVisible: Boolean        // persisted, default true
bundesland: Bundesland              // persisted
mode: Practice | Exam
questionIndex: Int
pickedAnswer: Int?                  // null = unanswered
correctCount: Int
examSecondsRemaining: Int           // exam only
```
Question catalogue, translations, and per-topic progress should come from a local database
(Room) so the app works offline — the whole catalogue is static and small.

## Assets
None bundled. Fonts: Archivo, Noto Sans KR, Noto Sans Arabic (Google Fonts — add as
downloadable fonts or bundle the variable files). Icons: not yet designed; the prototype uses
text glyphs (`✕`, `▸`) as placeholders — replace with a real icon set.

## Files in this bundle
- `Leben in Deutschland App.dc.html` — the prototype (all screens + the two alternative
  translation layouts). Open in a browser.
- `ios-frame.jsx` — presentation bezel only, not product.
- `styles.css` — the Modernist design system token sheet the tokens above were taken from.
- `compose_starter/Color.kt`, `Type.kt`, `Theme.kt` — the tokens already translated to
  Jetpack Compose. Drop into `app/src/main/java/<pkg>/ui/theme/` and adjust the package line.
- `compose_starter/README.md` — how to wire the fonts and where to start.
