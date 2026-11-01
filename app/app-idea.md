# App idea

This app will be able to overlay objects of interest in the mobile device user's point of view.
The object of interest location data is caputred by another mobile device (drone) from the sky.
The raw user picture is matched against a scene representation stored on the backend (built beforehadn)

## The basics

- **App name:** Wallhacker
- **One-line pitch:** Lets field operators see indicators for objects of interest in their field of view
- **The problem:** Improving situational awareness by using visual indicators overlaid on the user's POV, after prototype this will be done real-time
- **Target users:** Any field operator that can use better situational awareness (soldiers, forest police, earthquake response teams)

## The prototype scope

- **Core screen(s):** Current POV, here the user can capture a picture with their camera and the backend (currently mocked) will be able to tell what is in their surroundings
- **The main "thing" the app manages:** 
  - It doesn't really manage any resources, since the usecase for the mobile app side of the project is mostly read-only
  - In a future iteration we could use the raw user pictures to detect and register additional points of interest
- **A few fields of that thing:**
  - for the additional point of interest registration example: EXIF data

## The data

- **What records does the app create or collect?**
  - mainly audit logs of queries asked to the server are created: "I am here, what objects of interest are in my POV?"
  - the server takes the raw picture (with EXIF data) from the user
  - the server checks against the data stored on the backend 
  - the server replies with the determined camera pose and what objects should be renedered in the POV
- **Where does data come from?** (user input, a sensor, an API, a server)
  - phone/AR headset camera is used to capture the field operator's POV
  - the server answers with the object of interest location data
  - mobile applications "edits" the pictures so relevant information is overlayed
- **Where does data need to go?** (stays local, syncs to a server, shared with others)
    - the data stays local
    - in future iterations we could maybe detect objects of interest using a lightweight YOLO model on the mobile device,
    - or keep compressed scene representations on device for local matching
## Mobile-computing framing (required)

This is what makes it a mobile-computing project rather than just an app. Answer honestly; rough
answers are fine for now.

- **What must persist on the device** (survive a restart)?
  - not really applicable for the current prototype since, as mentioned above the app is mostly read-only
- **What crosses the network** (comes from or goes to a server or API)?
  - image data of the real world scene goes to the backend server
  - location tags of objects of interest and user's camera pose are sent to the backend server
- **What must still work with no signal**, and how should it behave offline?
  - if I implement the feature where we are able to compress a scene model for local matching, then we should still be able to relay information about objects of interest at the time the signal was lost
- **The placement question:** is there any computation that could run on-device instead of in the
  cloud (for example a model, a search, image processing)? If you moved it on-device, what would you
  trade in latency, energy, privacy, and bandwidth?
  - using the placement of the world model on-device would mean increases in latency and device cost but also better privacy, bandwidth is still limited by computing capability
  - reason: in a datacenter we can have multiple GPUs doing the matching and adding more details to a scene, we can't fit those in a phone

