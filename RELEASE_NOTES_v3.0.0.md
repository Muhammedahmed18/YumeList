# YumeList 3.0.0 ✨

The biggest YumeList update yet. The UI has been redesigned from the ground up, the networking layer rewritten and everything under the hood modernized. This release covers all changes since **1.1.0**.

## Highlights

- 🎨 **Material 3 Expressive redesign:** new theme, motion, shapes and components on every screen, with a pure black (OLED) mode.
- ⚡ **Faster, more reliable networking:** rebuilt on Retrofit 3 + OkHttp 5, with a shared HTTP cache, automatic token refresh and clear error messages.
- 📦 **Smaller, leaner app:** bundled icons, fewer libraries and per-device APKs, so you only download what your phone needs.

---

## 🎨 New UI

- Redesigned with **Material 3 Expressive** (Material 1.5): expressive motion, shapes, button groups and loading indicators
- **New onboarding:** a welcome screen, a theme picker (follow system / light / dark + pure black) and MyAnimeList login, all before you start
- **Pure black mode** for OLED screens
- **Redesigned Home:** Discover shortcuts (Anime Ranking, Seasonal Chart, Calendar, Manga Ranking), a **Today** carousel with airing times and countdowns, and This Season
- **Redesigned Details screen:**
  - A hero header with the poster, format, year and episode chips
  - An airing card showing the next broadcast day and time
  - A split status button to change your list status quickly
  - Stat tiles for score, rank, popularity and members
  - Rebuilt sections for genres, synopsis, info, characters, related titles, opening/ending themes and status distribution
- **Redesigned lists:** status chips, a filter dropdown, quick sort, one-tap **+1** progress buttons, progress bars and "Airing in X days" badges
- **Seasonal Chart:** a poster grid with list-status icons and scores, and a floating toolbar for switching seasons, choosing the format and filtering
- **Calendar:** a day-chip selector with a poster grid and air times
- **Rankings:** top-3 badges, status markers on posters and cleaner cards
- **Smooth shared-element transitions** between posters and details
- Rolling-number animations, skeleton loading and a new snackbar
- New **List Style** settings screen and a refreshed Appearance settings screen
- A refreshed About page

## ✨ New and improved features

- Live "Airing in…" countdowns on your list and on Home
- Today carousel with broadcast times
- Season chart filters and format options
- More list sort options
- Manga statistics and profile stats powered by **Jikan**

## ⚡ Networking

- Replaced Ktor with **Retrofit 3 + OkHttp 5**
- **Fixed token refresh:** the old client never actually refreshed expired MAL tokens. YumeList now refreshes them automatically and retries the request, so you're logged out less often.
- If many requests fail at once, YumeList refreshes the token **only once**
- One shared connection pool and a **10 MB HTTP cache** for faster repeat loads
- Sensible timeouts (15 s connect, 30 s read/write)
- **Jikan (v4)** support, through a separate client that never sends your MAL token to third parties
- Friendly error messages for network problems, expired sessions, server errors and missing pages instead of raw error codes, with retry states on every screen
- Images (Coil 3) share the same network stack

## 📦 Performance and size

- Removed `material-icons-extended`: the ~110 icons the app uses are now bundled directly
- Removed Accompanist (permissions now use the standard AndroidX API) and the placeholder library (replaced with a lightweight built-in skeleton)
- **Per-ABI APKs** (arm64-v8a, armeabi-v7a, x86, x86_64) plus a universal APK
- R8 minification and resource shrinking for release builds

## 🔧 Under the hood

- compileSdk **37**, targetSdk **36**, minSdk **24** (Android 7.0+)
- Every dependency updated to the latest stable version: Compose BOM 2026.09, Navigation 2.10, Lifecycle 2.11, Room 2.8, Koin 4.2, Coil 3.6, kotlinx.serialization 1.11, Coroutines 1.11 and more
- Java 17 with core library desugaring

---

## 📥 Which APK should I download?

| File | For |
|---|---|
| `…-arm64-v8a-release.apk` | Most phones (recommended) |
| `…-armeabi-v7a-release.apk` | Older 32-bit phones |
| `…-x86_64-release.apk` / `…-x86-release.apk` | Emulators, Chromebooks |
| `…-universal-release.apk` | Not sure? This one works everywhere |

**Updating from 1.1.0?** Install it over the old version. There's no need to uninstall.

YumeList is a fork of [MoeList](https://github.com/axiel7/MoeList) by axiel7, licensed under GPL-3.0.
