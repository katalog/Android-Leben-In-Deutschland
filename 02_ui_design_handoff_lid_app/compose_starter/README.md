# Compose starter

Three files carrying the design tokens. Everything else — screens, navigation, data — you build.

## Install
1. Copy `Color.kt`, `Type.kt`, `Theme.kt` into `app/src/main/java/<your>/<pkg>/ui/theme/`
   and change the `package` line in each to match.
2. `build.gradle.kts` (app module):
   ```kotlin
   implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")
   implementation("androidx.compose.material3:material3")
   ```
3. Add `app/src/main/res/values/font_certs.xml` from the Google Fonts in Compose docs
   (the `com_google_android_gms_fonts_certs` string array). Without it `Type.kt` will not compile.
4. Wrap your root composable: `LidTheme { AppNavHost() }`.

## Three rules that are easy to get wrong
- **Radius 0 everywhere.** `LidShapes` already enforces it — do not pass your own
  `RoundedCornerShape` to anything.
- **No elevation on content.** Structure comes from `Rule()` (2 dp) and nothing else.
  Don't use `Card` with default elevation.
- **Translated text must use `LidType.translation` / `translationSmall`** (family `I18n`).
  Archivo has no Hangul or Arabic glyphs — Korean and Arabic will render as boxes or fall back
  inconsistently if you use `Archivo` for a translated string.

## Suggested structure
```
ui/
  theme/       Color.kt, Type.kt, Theme.kt
  language/    SpracheScreen.kt
  home/        StartScreen.kt
  quiz/        FrageScreen.kt, AnswerRow.kt, QuizViewModel.kt
  result/      ErgebnisScreen.kt
data/
  Question.kt, Translation.kt, QuestionDao.kt, LidDatabase.kt   // Room, offline
```

## Where to start
`FrageScreen.kt`. It is the screen the whole product hangs on, it holds all the state, and the
other three are simpler variations of the same list-plus-rules layout. The parent README's
"3. Question" section is a complete spec for it, including every answer-row state.

## RTL
Arabic and Farsi need `LayoutDirection.Rtl` for the whole screen, but the German question and
answer text must stay `TextDirection.Ltr` and left-aligned inside it. Wrap the German `Text`
composables in a `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`.
