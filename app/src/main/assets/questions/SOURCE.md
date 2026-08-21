# Question data provenance

The question catalogue in this directory (`general.json` + `bundesland_<CODE>.json`) and the
images in `../images/` were adapted from:

- **Repository:** [flexsurfer/einburgerungstest](https://github.com/flexsurfer/einburgerungstest) (MIT License)
- **Underlying source:** BAMF (Bundesamt für Migration und Flüchtlinge) — *Gesamtfragenkatalog zum
  Test „Leben in Deutschland" und zum „Einbürgerungstest"*, Stand: 07.05.2025
  (https://www.bamf.de/SharedDocs/Anlagen/DE/Integration/Einbuergerung/gesamtfragenkatalog-lebenindeutschland.pdf)

300 general questions + 16 Bundesländer × 10 state-specific questions = 460 total, reshaped from
the source's flat `{question, answers[4], correct, category, img?}` format into this app's
`Question` schema (`id`, `category: GENERAL|BUNDESLAND`, `bundesland`, `topicId`, `textDe`,
`answerA-D`, `correctAnswerIndex`, `explanationDe`, `imageAsset`, `imageCaption`).

`explanationDe` is `null` for every question — the source catalogue (like the official BAMF one)
has no per-question explanation text. If per-question explanations are wanted later (see the
Result screen design), they'll need to be authored separately.

MIT License copyright notice (flexsurfer/einburgerungstest):

```
MIT License
Copyright (c) 2025 flexsurfer
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
