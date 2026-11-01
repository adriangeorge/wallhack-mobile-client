# Reflection: Wallhacker M1 prototype
## What AI did well

- Produced a working two-screen prototype quickly: CameraX preview and capture, a camera permission flow with rationale and a settings fallback, a mock server, a plan-view map, and an overlay of object markers on the camera preview.
- Put the server behind a `WallhackerServer` interface with a `FakeWallhackerServer`, so a real backend can replace it later.
- Generated mock data. Objects are placed by bearing and distance, so the "in field of view" flag agrees with the camera pose.
- Did the projection math for the overlay (pose and object position to screen position, size shrinking with distance), which I would have spent much longer on.

## What broke when we stressed it

- **Kill the app and reopen:** the last result is gone . Nothing is saved, the ViewModel does not use `SavedStateHandle`, and there are no local records. Captured JPEGs are left in the cache directory and never deleted.
- **Airplane mode:** nothing changes, because the "server" is a local function with a `delay`. The prototype has no real network request, no error handling for one, and no idea of being offline.
- **500 items:** the list on the map screen is lazy, so it would stay smooth. The overlay and the plan view draw every object on every frame with no culling or clustering. The POV view is unreadable.
- **Two people editing the same data:** there is no shared data to edit. The app is read-only, but there is also no sync, no way to tell which result is newer, and no audit log of the queries sent. The app idea calls for one.
- **Moving the phone after taking the picture:** the overlay uses the pose from the moment of capture, so the markers stay where they were instead of following the scene. No compass or gyroscope is read.
- **Accuracy:** the mock pose is a fixed heading, field of view and 1.6 m camera height. The EXIF data is invented and never read from the file. None of this has been checked against a real scene.

## What is missing for production

Most urgent first:

1. **A real backend and network layer:** upload the photo and EXIF, parse the pose and object list, handle slow or failed requests and timeouts (Lab 5).
2. **Persistence:** keep recent results and a query log on the device, and survive process death (Lab 6). This is also where offline matching or a cached scene model would live.
3. **Offline behavior and sync:** show the last known objects when there is no signal, queue or retry work with WorkManager, and mark results as stale (Lab 7).
4. **Architecture:** a UI state model per screen, a repository between the ViewModel and the server, and navigation with a back stack that is restored (Lab 4).
5. **Dependency injection and tests:** inject the server and clock, add JVM tests with fakes (for example the projection math and the in-view check) and an instrumented test, and set up CI (Labs 8 and 9).
6. **The integrated concept and placement decision:** which of image matching, YOLO detection or a compressed scene model runs on the device versus in the cloud, and what that costs in latency, energy, privacy and bandwidth (Lab 10).
7. **Live tracking:** use live sensors so the overlay follows the phone instead of showing a snapshot.

## Map of gaps to labs

| Gap | Lab |
|---|---|
| Architecture, state, navigation restore | 4 |
| Real network and error handling | 5 |
| Persistence and process death | 6 |
| Offline use and sync | 7 |
| Dependency injection | 8 |
| Tests and CI | 9 |
| On-device vs cloud placement | 10 |

## One sentence

AI gave me screens and a convincing demo, but it could not decide where the world model and the matching should run, what the app does with no signal, or what a real backend returns, because those depend on the field operators' constraints and not on code patterns.
