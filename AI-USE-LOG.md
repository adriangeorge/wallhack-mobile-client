# AI-use log

Tool: Claude Code (Sonnet 5.5) inside Android Studio. Project: Wallhacker (Lab 3 / M1 prototype).

| Date | What I was trying to do | What I prompted (short) | What the model produced | What I accepted or rejected, and WHY |
|---|---|---|---|---|
| 2026-10-31 | Get oriented: turn `app-idea.md` plus the course handout into a prototype scope | Attached the handout, lab README, example and this log template. Asked for a camera screen, a mocked server reply (camera pose, 2 objects in view, 1 outside) and a map screen | Read all the docs and the project, then proposed a small structure: `data/` (models, mock server, geo helpers), a shared ViewModel, `CameraScreen`, `MapScreen`, tabs in `MainActivity` | Accepted the structure. It matches the handout (Compose + M3, state in a ViewModel, server behind an interface so a fake can be swapped for the real backend in Labs 4/5). Kept it to two screens as the lab says |
| 2026-10-31 | Dependencies and manifest | (part of the same prompt) | Added CameraX 1.6.2, lifecycle-viewmodel-compose and exifinterface to the version catalog and `build.gradle.kts`. Added the CAMERA permission and an optional camera `uses-feature`. Raised `minSdk` 24 to 26 | Accepted. minSdk 26 is what the lab and handout require. Checked that the project syncs and builds. The exifinterface dependency is not used yet (EXIF is mocked), so it could be removed |
| 2026-10-31 | Camera screen with capture and runtime permission | (same prompt) | `CameraScreen.kt`: CameraX `PreviewView` in `AndroidView`, `ImageCapture` to a cache file, permission request with rationale, a re-request button and an "open app settings" fallback when denied | Accepted. It covers the handout's "handle a runtime permission, degrade on denial, re-request path" floor. Not yet tested on process death or with a revoked permission |
| 2026-10-31 | Mock server and fake data | (same prompt) | `FakeWallhackerServer`: 1.5 s delay, fixed pose (heading 45 deg, FOV 66 deg) near the UPB campus, 3 objects placed by bearing and distance so "in view" agrees with the pose. `mockExif()` for the fake EXIF | Accepted. Placing objects by bearing/distance instead of typing coordinates keeps the in-view flag consistent. The EXIF values and the coordinates are invented, not real measurements |
| 2026-10-31 | Map screen | "a separate screen where we can show a map of our pose and all objects of interest" | `MapScreen.kt`: a top-down plan view drawn with a Compose `Canvas` (camera triangle, FOV wedge, green/grey dots, labels) plus a list with coordinates and heights | Accepted a Canvas drawing instead of a real map SDK, because it needs no API key or network and the data is only a few points. It is not a real map; I need to pick a map SDK later if I want one |
| 2026-10-31 | Overlay of objects on the camera view | "continue with the overlay step" | `PoiOverlay.kt`: pinhole projection from the server pose to screen position, diamond marker and label for each object inside the frustum | Accepted. Learned that it assumes a static pose from the moment of capture and a camera height of 1.6 m (the server does not send this), so markers do not follow the phone as it turns |
| 2026-10-31 | Scale reticles with distance | "scale the reticles according to the distance" | Reticle size is proportional to 1/distance (20 dp at 50 m), clamped to 6..36 dp, stroke and label offset scale with it | Accepted. Constants are at the top of `PoiOverlay.kt` for tuning |
| 2026-11-01 | Complete this log | "complete the ai use log file under app" | This file | Reviewed against what actually happened in the sessions |

## How I verified

- The project syncs and `:app:assembleDebug` succeeds after each step.
- I built and ran the app from Android Studio. I have not yet checked on a device whether the marker positions look right, or tried rotation, killing the app, or airplane mode (that is the Part 4 stress test for `reflection.md`).

## Prompts worth remembering

- Attaching the handout and lab README together with the app idea worked well: the model picked up the required stack and the permission floor without being told.
- "Continue with the overlay step" worked only because the earlier turn had already listed the overlay as the next step. A cold prompt would need the pose model spelled out.

## One thing the AI got wrong or could not tell me

The overlay math looks right on paper, but the model cannot know the real field of view or camera height of my phone, or how the server will define heading, pitch and field of view. It used 66 degrees and 1.6 m as guesses and said so, but only I can check them against a real photo and server reply. It also could not tell me whether the mocked "pose" will resemble what a real scene-matching backend returns, so the data format in `Models.kt` is a guess until the backend exists.
