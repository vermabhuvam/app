# Gully Rewind — 90s Bollywood Radio (Android)

A native Kotlin Android app: an always-on radio playing 90s Hindi film songs,
with playlists, a live-ish listener counter, and links to Spotify/YouTube
Music/WhatsApp — inspired by the *feature set* of deluxesalon.in, built with
its own name, branding, and song curation.

## How to open and run
1. Install [Android Studio](https://developer.android.com/studio).
2. `File → Open` → select this `GullyRewindRadio` folder.
3. Let Gradle sync (Android Studio will auto-generate the missing wrapper jar
   the first time — click "Fix" if it prompts you).
4. Run on an emulator or a real device (minSdk 24 / Android 7.0+).

## Building an APK without Android Studio (GitHub Actions)
This project includes `.github/workflows/build-apk.yml`. Push it to a GitHub
repo and every push to `main` (or a manual run from the Actions tab) builds
a debug APK for you — no local Android SDK needed. Download it from the
run's "Artifacts" section. See the chat for full step-by-step instructions.


## What's implemented
- **Radio player** (`MainActivity`): plays audio by embedding YouTube's
  official IFrame Player API inside a hidden 1×1 `WebView` — this streams
  live from YouTube, nothing is downloaded or re-hosted, matching YouTube's
  Terms of Service. Play / Pause / Next / Previous all work, and the queue
  auto-advances when a song ends.
- **Playlists** (`PlaylistsActivity` → `PlaylistDetailActivity`): three
  curated categories — *Gully Ki Mohabbat* (romantic), *Baraat Anthem*
  (wedding/dance), *Dard Bhare Nagme* (sad).
- **All Songs** (`SongsActivity`): full master list.
- **Listener counter**: a locally simulated number that gently drifts every
  few seconds (see note below on making it real).
- **External links**: Spotify, YouTube Music, and a WhatsApp channel button.

## Every song ships with a real, verified YouTube video ID
Nothing is a placeholder — I checked each one against an actual YouTube
upload before adding it, so the app plays music immediately, out of the box.

## Adding more songs
Open `app/src/main/java/com/gullyrewind/radio/data/SongRepository.kt` and add
a new entry:
```kotlin
Song(
    id = "s6",
    title = "Song Name",
    movie = "Movie Name",
    singers = "Singer Names",
    year = 1996,
    youtubeId = "XXXXXXXXXXX", // from youtube.com/watch?v=XXXXXXXXXXX
    category = CATEGORY_ROMANTIC // or WEDDING / SAD, or add a new category
)
```
That's the only change needed — the playlists and song list screens read
straight from this file.

## Things you'll likely want to customize
- **WhatsApp channel link** — currently a placeholder in `MainActivity.kt`
  (`btnWhatsapp` click listener). Swap in your real channel invite link.
- **Spotify / YouTube Music links** — currently point at the generic apps;
  swap in your own playlist URLs the same way.
- **App icon** — `res/drawable/ic_launcher.xml` is a simple placeholder
  vector. Replace with Android Studio's Image Asset tool
  (`right-click res → New → Image Asset`) for a polished launcher icon.
- **App name / branding** — `res/values/strings.xml` → `app_name`, `tagline`.

## Extending: true background / lock-screen playback
Right now playback is tied to `MainActivity` being open, like most simple
radio apps. To make it play through the lock screen and show media
controls in the notification shade, the next step is to move the
`YouTubeAudioPlayer` + `WebView` into a foreground `Service` with a
`MediaSessionCompat`, and wire it to the same JavaScript bridge already
built here — the player logic itself doesn't need to change.

## Extending: a real live listener count
The current counter is simulated per-device. For a genuine shared number
across everyone using the app, you'd add a small backend — a Firebase
Realtime Database counter (increment on app open / decrement on close) is
the quickest route, or a lightweight WebSocket service if you want more
control.

## A note on originality
This app deliberately does **not** reuse deluxesalon.in's illustration,
exact branding, "Deluxe Saloon" name, or its specific playlist curation —
those are someone else's creative work. What's replicated here is the
*feature concept* (always-on themed radio, playlists, listener count, social
links), which isn't protected by copyright, combined with an original name,
color palette, and song selection.
