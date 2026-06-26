# PipeXtend — Compose Migration Guide

> **Purpose:** Panduan dan tracker progres migrasi dari Android View System (XML + Java/Kotlin Fragments) ke Jetpack Compose.
> **Dibuat:** 2026-06-26
> **Last Updated:** 2026-06-26

---

## 📍 Konteks Proyek

PipeXtend adalah **fork NewPipe** — aplikasi Android streaming multi-platform (YouTube, SoundCloud, Bandcamp, PeerTube, media.ccc.de).

### Arsitektur Saat Ini (Legacy)
- **Language:** Java + Kotlin (campur)
- **UI:** Android View System — XML Layouts + ViewBinding + Fragment-based navigation
- **Navigation:** Fragment back-stack manual di `MainActivity.java`
- **DI:** Koin (di shared module)
- **Player:** ExoPlayer 2.x via custom `Player.java` (99KB!)
- **Database:** Room + RxJava3

### Arsitektur Target (Compose)
- **Language:** Kotlin murni
- **UI:** Jetpack Compose (Multiplatform via shared module)
- **Navigation:** Navigation3 + `NavDisplay` (sudah ada scaffolding!)
- **DI:** Koin (sudah ada, perlu expand)
- **Player:** ExoPlayer + AndroidView wrapper (interim), lalu Compose player UI
- **Database:** Room + Coroutines (ganti RxJava3 bertahap)

### Stack Compose yang Sudah Ada di Proyek
| Library | Versi | Status |
|---------|-------|--------|
| Compose Multiplatform | 1.11.1 | ✅ Ada |
| Material3 Expressive | 1.11.0-alpha07 | ✅ Ada |
| Navigation3 | 1.1.1 | ✅ Ada |
| Koin Compose | 4.2.2 | ✅ Ada |
| Coil3 Compose | 3.5.0 | ✅ Ada |
| Coroutines | 1.11.0 | ✅ Ada |
| MaterialExpressiveTheme | — | ✅ Ada di Theme.kt |

---

## 📊 Scope & Statistik

| Kategori | Jumlah |
|----------|--------|
| Total source files (.java + .kt) | **431** |
| XML Layout files | **113** |
| Files sudah pakai ViewBinding | **62** |
| Files sudah pakai Compose/setContent | **26** |
| Activities (legacy) | **8** |
| Fragments (legacy) | **~35** |
| Custom View classes | **16** |
| RecyclerView Holders | **~30** |
| Adapters | **~12** |

### File Paling Kompleks (harus dikerjakan hati-hati)
| File | Ukuran | Kompleksitas |
|------|--------|--------------|
| Player.java | 99 KB | 🔴 EXTREME — Inti player, banyak state |
| RouterActivity.java | 47 KB | 🔴 HIGH — Intent routing logic |
| MainActivity.java | 46 KB | 🔴 HIGH — Navigation host utama |
| VideoDetailFragment.java | 107 KB | 🔴 EXTREME — Detail video page |
| fragment_video_detail.xml | 39 KB / 708 baris | 🔴 HIGH |
| player.xml | 38 KB / 813 baris | 🔴 HIGH |
| dialog_playback_parameter.xml | 23 KB | 🟠 MEDIUM-HIGH |

---

## 🗺️ Struktur Target Compose

```
shared/src/commonMain/kotlin/net/newpipe/app/
├── navigation/
│   ├── Destination.kt          ← Tambah semua destinations
│   ├── NavDisplay.kt           ← ✅ Sudah ada
│   ├── NavModule.kt            ← ✅ Sudah ada
│   └── Navigator.kt            ← Perlu dibuat
├── screen/
│   ├── about/                  ← ✅ Sudah ada
│   ├── home/                   ← 🔲 Perlu dibuat (MainFragment)
│   ├── feed/                   ← 🔲 Perlu dibuat (FeedFragment)
│   ├── search/                 ← 🔲 Perlu dibuat (SearchFragment)
│   ├── subscription/           ← 🔲 Perlu dibuat (SubscriptionFragment)
│   ├── bookmarks/              ← 🔲 Perlu dibuat (BookmarkFragment)
│   ├── channel/                ← 🔲 Perlu dibuat (ChannelFragment)
│   ├── playlist/               ← 🔲 Perlu dibuat (PlaylistFragment)
│   ├── videodetail/            ← 🔲 Perlu dibuat (VideoDetailFragment)
│   ├── kiosk/                  ← 🔲 Perlu dibuat (KioskFragment)
│   ├── download/               ← 🔲 Perlu dibuat (MissionsFragment)
│   ├── settings/               ← 🔲 Perlu dibuat (SettingsActivity + Fragments)
│   └── player/                 ← 🔲 Perlu dibuat (Player UI)
├── composable/
│   ├── StreamItemCard.kt       ← 🔲 (pengganti list_stream_item.xml)
│   ├── StreamGridItem.kt       ← 🔲
│   ├── ChannelItem.kt          ← 🔲
│   ├── PlaylistItem.kt         ← 🔲
│   ├── CommentItem.kt          ← 🔲
│   ├── ErrorPanel.kt           ← 🔲
│   └── LoadingState.kt         ← 🔲
├── viewmodel/
│   ├── about/                  ← ✅ Sudah ada
│   ├── HomeViewModel.kt        ← 🔲
│   ├── FeedViewModel.kt        ← 🔲 (ada di app module, perlu dipindah ke shared)
│   ├── SearchViewModel.kt      ← 🔲
│   ├── ChannelViewModel.kt     ← 🔲
│   ├── VideoDetailViewModel.kt ← 🔲
│   └── PlayerViewModel.kt      ← 🔲
└── theme/
    ├── Color.kt                ← ✅ Sudah ada
    ├── Theme.kt                ← ✅ Sudah ada
    ├── Dimens.kt               ← ✅ Sudah ada
    └── ServiceTheme.kt         ← ✅ Sudah ada
```

---

## 🏁 Fase Migrasi

### Fase 0 — Persiapan & Fondasi
> **Tujuan:** Setup scaffolding, pastikan Compose dan Fragment bisa co-exist. Tidak ada yang rusak.

- [x] **0.1** Tambah `ComposeView` ke `MainActivity.java` sebagai container sementara (melalui BaseComposeFragment)
- [x] **0.2** Buat `Navigator.kt` di `shared/navigation/` — wrapper navigasi
- [x] **0.3** Expand `Destination.kt` dengan semua destinations
- [x] **0.4** Setup `CompositionLocal` untuk shared dependencies (menggunakan Koin injection di Compose)
- [x] **0.5** Buat `BaseComposeFragment.kt` — Fragment wrapper yang host `ComposeView`
- [x] **0.6** Buat Compose Design System tokens di `Dimens.kt`

---

### Fase 1 — Komponen Dasar (Atomic Components)
> **Tujuan:** Buat semua building-block composables yang dipakai di mana-mana.

#### 1.1 Stream Item Components
- [x] `StreamItemRow` — pengganti list_stream_item.xml + StreamInfoItemHolder.java
- [x] `StreamItemGrid` — pengganti list_stream_grid_item.xml
- [x] `StreamItemCard` — pengganti list_stream_card_item.xml
- [x] `StreamItemMini` — pengganti list_stream_mini_item.xml
- [x] `ThumbnailWithDuration` — thumbnail + badge durasi (reusable)
- [x] `AnimatedWatchProgress` — pengganti AnimatedProgressBar.java

#### 1.2 Channel & Playlist Item Components
- [x] `ChannelItemRow` — pengganti list_channel_item.xml
- [x] `ChannelItemGrid` — pengganti list_channel_grid_item.xml
- [x] `ChannelItemCard` — pengganti list_channel_card_item.xml
- [x] `PlaylistItemRow` — pengganti list_playlist_item.xml
- [x] `PlaylistItemGrid` — pengganti list_playlist_grid_item.xml

#### 1.3 Comment Component
- [x] `CommentItem` — pengganti list_comment_item.xml + CommentInfoItemHolder.java

#### 1.4 State & Error Components
- [x] `ErrorPanel` — pengganti error_panel.xml
- [x] `EmptyState` — pengganti list_empty_view.xml
- [x] `LoadingIndicator` — circular + linear variants

#### 1.5 InfoList (LazyColumn/LazyGrid)
- [x] `InfoList` composable — pengganti InfoListAdapter.java
  - Support: LIST, GRID, CARD mode
  - Pagination/load-more
  - Pull-to-refresh

---

### Fase 2 — Layar Sederhana

#### 2.1 About Screen
- [x] `AboutScreen.kt` — ✅ **SUDAH ADA**

#### 2.2 Subscriptions Screen
- [x] Pindah `SubscriptionViewModel.kt` ke shared module
- [x] Buat `SubscriptionScreen.kt`
- [x] Hapus `SubscriptionFragment.kt` + `fragment_subscription.xml` (dimigrasikan ke BaseComposeFragment)

#### 2.3 Bookmarks Screen
- [x] Buat `BookmarkViewModel.kt`
- [x] Buat `BookmarkScreen.kt`
- [x] Hapus `BookmarkFragment.java` + `fragment_bookmarks.xml` (dimigrasikan ke BaseComposeFragment)

#### 2.4 Download/Missions Screen
- [x] Buat `DownloadScreen.kt`
- [x] Hapus `MissionsFragment.java` + `missions.xml` (dimigrasikan ke DownloadFragment + BaseComposeFragment)

#### 2.5 Settings Screen (semua sub-screen)
- [x] `SettingsScreen.kt` — root
- [x] `AppearanceSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `VideoAudioSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `ContentSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `HistorySettingsScreen.kt` (menggunakan placeholder screen)
- [x] `NotificationSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `DownloadSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `BackupRestoreSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `UpdateSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `DebugSettingsScreen.kt` (menggunakan placeholder screen)
- [x] `PeertubeInstanceListScreen.kt`

---

### Fase 3 — Layar List/Feed

#### 3.1 Search Screen
- [x] Buat `SearchViewModel.kt`
- [x] Buat `SearchScreen.kt` (search bar, suggestions, filters, results)
- [x] Hapus `SearchFragment.java` + `fragment_search.xml`

#### 3.2 Feed Screen
- [x] Pindah `FeedViewModel.kt` ke shared module
- [x] Buat `FeedScreen.kt` (tabs, infinite scroll, pull-to-refresh)
- [x] Hapus `FeedFragment.kt` + `fragment_feed.xml` (dimigrasikan ke BaseComposeFragment)

#### 3.3 Kiosk Screen
- [x] Buat `KioskViewModel.kt`
- [x] Buat `KioskScreen.kt`
- [x] Hapus `KioskFragment.java` + `fragment_kiosk.xml` (dimigrasikan ke BaseComposeFragment)

#### 3.4 History Screen
- [x] Buat `HistoryViewModel.kt`
- [x] Buat `HistoryScreen.kt`
- [x] Hapus `StatisticsPlaylistFragment.java`

---

### Fase 4 — Layar Detail (Channel & Playlist)

#### 4.1 Channel Screen
- [x] Buat `ChannelViewModel.kt`
- [x] Buat `ChannelScreen.kt` (collapsing header, tabs)
- [x] Hapus `ChannelFragment.java` + `fragment_channel.xml`

#### 4.2 Playlist Screen (Remote)
- [x] Buat `PlaylistViewModel.kt`
- [x] Buat `PlaylistScreen.kt`
- [x] Hapus `PlaylistFragment.java` + `fragment_playlist.xml`

#### 4.3 Local Playlist Screen
- [x] Buat `LocalPlaylistScreen.kt` (drag-to-reorder)
- [x] Hapus `LocalPlaylistFragment.java`

---

### Fase 5 — Video Detail Screen ⚠️ PALING KOMPLEKS

#### 5.1 Komponen Detail Screen
- [x] `VideoDetailScreen.kt` — container utama
- [x] `VideoThumbnailHero.kt` — thumbnail + play button + badges
- [x] `VideoInfoSection.kt` — judul, channel, views, like/dislike
- [x] `VideoDescriptionSection.kt` — deskripsi collapsible + tags
- [x] `VideoCommentsSection.kt` — daftar komentar
- [x] `RelatedVideosSection.kt` — video terkait
- [x] `StreamSegmentList.kt` — chapter list
- [x] `VideoDetailTabRow.kt` — tabs Info/Comments/Related

#### 5.2 Comment Replies
- [x] `CommentRepliesScreen.kt`
- [x] Hapus `CommentRepliesFragment.java`

#### 5.3 State Management
- [x] `VideoDetailViewModel.kt` — konsolidasi state (107KB VideoDetailFragment!)

---

### Fase 6 — Player UI ⚠️ PALING CRITICAL

#### 6.1 Player Architecture
```
PlayerService (tetap)
    └── Player.java (backend logic, minimal changes)
        └── PlayerViewModel.kt (new — bridge ke Compose)
            └── PlayerScreen.kt (Compose UI)
                ├── VideoSurface (AndroidView wrapping SurfaceView)
                ├── PlayerControlsOverlay.kt
                ├── SeekBarCompose.kt
                ├── TopControls
                ├── BottomControls
                └── GestureOverlay
```

#### 6.2 Komponen Player
- [x] `PlayerScreen.kt` — container fullscreen
- [x] `PlayerMiniPlayer.kt` — mini player bottom sheet
- [x] `VideoSurface.kt` — `AndroidView` wrapping `ExpandableSurfaceView`
- [ ] `PlayerControls.kt` — top/bottom controls (incorporated inside PlayerScreen)
- [x] `SeekBarCompose.kt` — seek bar + preview thumbnail
- [ ] `FastSeekOverlay.kt` — animasi +10s/-10s (incorporated inside PlayerScreen)
- [ ] `SubtitleOverlay.kt` — subtitle display (incorporated inside PlayerScreen)
- [ ] `PlaybackParameterDialog.kt` — speed/pitch controls

#### 6.3 Play Queue
- [x] `PlayQueueScreen.kt` (drag-to-reorder)
- [x] Hapus `PlayQueueActivity.java`

---

### Fase 7 — Navigation & MainActivity Refactor

- [ ] **7.1** Expand `Destination.kt` dengan semua destinations:
  - Home, Feed, Search, Subscription, Bookmark, History, Download
  - Channel(url), Playlist(url), LocalPlaylist(id), VideoDetail(url)
  - Kiosk(serviceId, kioskId), Settings dan sub-screens
- [ ] **7.2** Expand `NavDisplay.kt` dengan entry provider semua destinations
- [ ] **7.3** Buat `AppNavigation.kt` — bottom nav + rail untuk tablet
- [ ] **7.4** Refactor `MainActivity.java` → `MainActivity.kt`
- [ ] **7.5** Buat `MainScreen.kt` — drawer + nav
- [ ] **7.6** Migrate `RouterActivity.java` → `RouterActivity.kt`

---

### Fase 8 — Cleanup

- [ ] Hapus semua Fragment files
- [ ] Hapus semua XML layout files
- [ ] Hapus semua custom View classes yang sudah diganti Compose
- [ ] Hapus `InfoListAdapter.java` dan semua Holder files
- [ ] Hapus `LocalItemListAdapter.java`
- [ ] Hapus ViewBinding dari `app/build.gradle.kts`
- [ ] Migrasi RxJava3 → Coroutines/Flow (gunakan bridge `kotlinx-coroutines-rx3` dulu)
- [ ] Hapus `kapt` (evernote state saver)
- [ ] Hapus `groupie` library

---

## 📋 Checklist Migrasi Per File

### Activities
| File | Status | Fase | Notes |
|------|--------|------|-------|
| MainActivity.java | 🔲 TODO | 7 | Navigation host utama |
| SettingsActivity.java | 🔲 TODO | 2.5 | Host settings fragments |
| DownloadActivity.java | ✅ DONE | 2.4 | Host download fragment (menggunakan DownloadFragment) |
| PlayQueueActivity.java | ✅ DONE | 6.3 | Dimigrasikan ke ComposeActivity |
| RouterActivity.java | 🔲 TODO | 7 | Intent routing |
| ReCaptchaActivity.java | 🔲 TODO | 7 | WebView recaptcha |
| ExitActivity.kt | 🔲 TODO | 7 | Simple intent |
| PanicResponderActivity.java | 🔲 TODO | 7 | Panic button |
| ErrorActivity.kt | 🔲 TODO | 7 | Error display |

### Fragments — Main
| File | Status | Fase | Notes |
|------|--------|------|-------|
| MainFragment.java | 🔲 TODO | 7 | Tab container + ViewPager |
| BaseFragment.java | 🔲 TODO | 0 | Base class |
| BaseStateFragment.java | 🔲 TODO | 0 | State handling base |
| BlankFragment.java | 🔲 TODO | 7 | Placeholder |
| EmptyFragment.java | 🔲 TODO | 7 | Empty state |

### Fragments — Detail
| File | Status | Fase | Notes |
|------|--------|------|-------|
| VideoDetailFragment.java | 🔲 TODO | 5 | 107KB — paling besar! |
| BaseDescriptionFragment.java | 🔲 TODO | 5 | Base for description |
| DescriptionFragment.java | 🔲 TODO | 5 | Video description tab |

### Fragments — List
| File | Status | Fase | Notes |
|------|--------|------|-------|
| BaseListFragment.java | 🔲 TODO | 1.5 | Base list |
| BaseListInfoFragment.java | 🔲 TODO | 1.5 | Base info list |
| SearchFragment.java | ✅ DONE | 3.1 | Dimigrasikan ke BaseComposeFragment |
| ChannelFragment.java | ✅ DONE | 4.1 | Dimigrasikan ke BaseComposeFragment |
| ChannelTabFragment.java | ✅ DONE | 4.1 | Dimigrasikan ke BaseComposeFragment |
| ChannelAboutFragment.java | ✅ DONE | 4.1 | Dimigrasikan ke BaseComposeFragment |
| CommentsFragment.java | 🔲 TODO | 5.1 | Comments tab |
| CommentRepliesFragment.java | ✅ DONE | 5.2 | Dimigrasikan ke BaseComposeFragment |
| KioskFragment.java | ✅ DONE | 3.3 | Dimigrasikan ke BaseComposeFragment |
| DefaultKioskFragment.java | ✅ DONE | 3.3 | Dimigrasikan ke BaseComposeFragment |
| PlaylistFragment.java | ✅ DONE | 4.2 | Dimigrasikan ke BaseComposeFragment |
| RelatedItemsFragment.java | 🔲 TODO | 5.1 | Related videos |

### Fragments — Local
| File | Status | Fase | Notes |
|------|--------|------|-------|
| FeedFragment.kt | ✅ DONE | 3.2 | Dimigrasikan ke BaseComposeFragment |
| BookmarkFragment.java | ✅ DONE | 2.3 | Dimigrasikan ke BaseComposeFragment |
| LocalPlaylistFragment.java | ✅ DONE | 4.3 | Dimigrasikan ke BaseComposeFragment |
| SubscriptionFragment.kt | ✅ DONE | 2.2 | Dimigrasikan ke BaseComposeFragment |
| SubscriptionsImportFragment.java | 🔲 TODO | 2.2 | Import subs |
| StatisticsPlaylistFragment.java | ✅ DONE | 3.4 | Dimigrasikan ke BaseComposeFragment |

### Fragments — Settings
| File | Status | Fase | Notes |
|------|--------|------|-------|
| MainSettingsFragment.java | ✅ DONE | 2.5 | Dimigrasikan ke BaseComposeFragment |
| AppearanceSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| VideoAudioSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| ContentSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| HistorySettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| NotificationsSettingsFragment.kt | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| DownloadSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| BackupRestoreSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| UpdateSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| DebugSettingsFragment.java | ✅ DONE | 2.5 | Menggunakan placeholder screen |
| PeertubeInstanceListFragment.java | ✅ DONE | 2.5 | Dimigrasikan ke BaseComposeFragment |
| ExoPlayerSettingsFragment.java | 🔲 TODO | 2.5 | ExoPlayer settings |
| ChooseTabsFragment.java | 🔲 TODO | 2.5 | Tab customization |
| PreferenceSearchFragment.java | 🔲 TODO | 2.5 | Pref search |
| NotificationModeConfigFragment.kt | 🔲 TODO | 2.5 | Notif mode |
| SelectChannelFragment.java | 🔲 TODO | 2.5 | Picker dialog |
| SelectPlaylistFragment.java | 🔲 TODO | 2.5 | Picker dialog |
| SelectFeedGroupFragment.java | 🔲 TODO | 2.5 | Picker dialog |
| SelectKioskFragment.java | 🔲 TODO | 2.5 | Picker dialog |

### Adapters & Holders
| File | Status | Fase | Compose Target |
|------|--------|------|----------------|
| InfoListAdapter.java | 🔲 TODO | 1.5 | InfoList composable |
| StreamInfoItemHolder.java | 🔲 TODO | 1.1 | StreamItemRow |
| StreamMiniInfoItemHolder.java | 🔲 TODO | 1.1 | StreamItemMini |
| StreamGridInfoItemHolder.java | 🔲 TODO | 1.1 | StreamItemGrid |
| StreamCardInfoItemHolder.java | 🔲 TODO | 1.1 | StreamItemCard |
| ChannelInfoItemHolder.java | 🔲 TODO | 1.2 | ChannelItemRow |
| ChannelMiniInfoItemHolder.java | 🔲 TODO | 1.2 | ChannelItemMini |
| ChannelGridInfoItemHolder.java | 🔲 TODO | 1.2 | ChannelItemGrid |
| ChannelCardInfoItemHolder.java | 🔲 TODO | 1.2 | ChannelItemCard |
| PlaylistInfoItemHolder.java | 🔲 TODO | 1.2 | PlaylistItemRow |
| PlaylistMiniInfoItemHolder.java | 🔲 TODO | 1.2 | PlaylistItemMini |
| PlaylistGridInfoItemHolder.java | 🔲 TODO | 1.2 | PlaylistItemGrid |
| PlaylistCardInfoItemHolder.java | 🔲 TODO | 1.2 | PlaylistItemCard |
| CommentInfoItemHolder.java | 🔲 TODO | 1.3 | CommentItem |
| LocalItemListAdapter.java | 🔲 TODO | 1.5 | InfoList composable |
| LocalPlaylistItemHolder.java | 🔲 TODO | 4.3 | LocalPlaylistItem |
| PlayQueueAdapter.java | 🔲 TODO | 6.3 | PlayQueueScreen |
| SuggestionListAdapter.kt | 🔲 TODO | 3.1 | SearchSuggestions |
| StreamSegmentAdapter.kt | 🔲 TODO | 5.1 | StreamSegmentList |
| NotificationModeConfigAdapter.kt | 🔲 TODO | 2.5 | NotifModeConfig |

### Custom Views
| File | Status | Fase | Compose Equivalent |
|------|--------|------|--------------------|
| AnimatedProgressBar.java | 🔲 TODO | 1.1 | Custom Canvas composable |
| CustomCollapsingToolbarLayout.java | 🔲 TODO | 4 | CollapsingToolbarScaffold |
| ExpandableSurfaceView.java | 🔲 TODO | 6.2 | AndroidView(SurfaceView) |
| FocusAwareCoordinator.java | 🔲 TODO | 7 | Native Compose focus |
| FocusAwareDrawerLayout.java | 🔲 TODO | 7 | ModalNavigationDrawer |
| FocusAwareSeekBar.java | 🔲 TODO | 6.2 | Custom Slider composable |
| FocusOverlayView.java | 🔲 TODO | 7 | Compose Indication |
| NewPipeEditText.java | 🔲 TODO | 0 | OutlinedTextField |
| NewPipeRecyclerView.java | 🔲 TODO | 1.5 | LazyColumn + empty state |
| NewPipeTextView.java | 🔲 TODO | 1 | Text() + linkify |
| ScrollableTabLayout.java | 🔲 TODO | 4 | ScrollableTabRow |
| CircleClipTapView.kt | 🔲 TODO | 6.2 | Custom Canvas |
| SecondsView.kt | 🔲 TODO | 6.2 | Animated composable |

---

## ⚠️ Tantangan & Keputusan Teknis

### 1. ExoPlayer Player Surface
**Problem:** ExoPlayer butuh SurfaceView/TextureView — tidak ada di Compose natively.
**Solution:** `AndroidView { ExpandableSurfaceView(it) }` — pattern resmi Google.

### 2. RxJava3 → Coroutines/Flow
**Problem:** Banyak kode pakai RxJava3 (Observable, Flowable, Single).
**Solution:** Migrasi bertahap menggunakan `kotlinx-coroutines-rx3` sebagai bridge (sudah ada di project).
- `Single` → `suspend fun`
- `Observable/Flowable` → `Flow`

### 3. Drag-to-Reorder dalam LazyColumn
**Problem:** LazyColumn tidak support drag-reorder natively.
**Solution:** Gunakan `reorderable` library atau custom `pointerInput`.

### 4. Settings (PreferenceFragment)
**Problem:** PreferenceFragmentCompat sangat terintegrasi dengan XML.
**Solution:** Buat Compose-based preference components sendiri.

### 5. Deep Link / Intent Routing
**Problem:** RouterActivity.java (47KB) menangani banyak intent.
**Solution:** Jaga logic routing di RouterActivity.kt, hanya ganti UI. Integrate dengan Navigation3.

### 6. Player Bottom Sheet
**Problem:** Player pakai CustomBottomSheetBehavior yang sangat custom.
**Solution:** Gunakan `BottomSheetScaffold` atau `ModalBottomSheet` dari Compose Material3.

---

## 🔄 Urutan Pengerjaan yang Disarankan

```
Fase 0 (Setup)
    ↓
Fase 1 (Komponen Atom)
    ↓
Fase 2 (Settings + Simple Screens)
    ↓
Fase 3 (Feed + Search + Kiosk)
    ↓
Fase 4 (Channel + Playlist)
    ↓
Fase 5 (Video Detail)
    ↓
Fase 6 (Player UI)
    ↓
Fase 7 (Navigation Refactor)
    ↓
Fase 8 (Cleanup)
```

---

## 📌 Status Progres

### ✅ Sudah Ada (Tidak Perlu Dikerjakan Ulang)
- `shared/.../theme/` — Theme, Color, Dimens, ServiceTheme
- `shared/.../navigation/NavDisplay.kt`
- `shared/.../navigation/NavModule.kt`
- `shared/.../screen/about/` — About screen lengkap
- `shared/.../viewmodel/about/` — About ViewModel
- Koin DI setup dasar
- `Destination.kt` (baru ada About, perlu expand)

### 🔲 Langkah Pertama yang Harus Dikerjakan
1. Expand `Destination.kt` dengan semua destinations
2. Buat `BaseComposeFragment.kt` sebagai bridge
3. Mulai `StreamItemRow` composable (komponen paling sering dipakai)
4. Buat `InfoList` composable (LazyColumn wrapper)

---

## 📝 Catatan Session

### Session 2026-06-26
- Analisis lengkap codebase selesai
- 431 source files, 113 XML layouts, 26 files sudah pakai Compose
- Arsitektur target sudah ditentukan (Navigation3 + Koin + Material3 Expressive)
- Scaffolding Compose sudah ada: Navigation3, Theme, About screen, Koin DI
- Player.java (99KB) dan VideoDetailFragment.java (107KB) adalah file paling kompleks
- MIGRATE.md dibuat sebagai panduan dan tracker

---

## 🔗 File Referensi Penting

| File | Lokasi | Keterangan |
|------|--------|------------|
| Theme.kt | shared/.../theme/ | App theme setup |
| Color.kt | shared/.../theme/ | Color palette |
| Destination.kt | shared/.../navigation/ | Route definitions |
| NavDisplay.kt | shared/.../navigation/ | Navigation display |
| NavModule.kt | shared/.../navigation/ | Koin nav module |
| libs.versions.toml | gradle/ | All dependency versions |
| app/build.gradle.kts | app/ | App module build config |
| shared/build.gradle.kts | shared/ | Shared module build config |
| AndroidManifest.xml | app/src/main/ | Activity declarations |
| App.kt | app/src/main/.../ | Application class |
| Player.java | app/.../player/ | ⚠️ Inti player (99KB) |
| VideoDetailFragment.java | app/.../fragments/detail/ | ⚠️ Detail page (107KB) |

---

## 🔬 Detail Teknis dari Analisis Codebase

### Distribusi Metode Binding (Legacy Code)
| Metode | Jumlah Approx |
|--------|---------------|
| ViewBinding (primary) | ~15 Activities/Fragments |
| ViewBinding (partial/dialogs only) | ~10 Fragments |
| Manual inflate + findViewById | ~15 Fragments |
| PreferenceFragmentCompat (preference XML) | 14 Fragments |
| Tidak ada layout (utility activities/bases) | 5 |

> ⚠️ **PENTING:** Zero file di `app/` yang pakai `@Composable` atau `ComposeView`. Semua 100% View-based XML.

### Observasi Kunci
1. **RouterActivity.java** — tidak ada `setContentView` sama sekali, hanya transparent router yang menampilkan dialogs
2. **`fragment_playlist.xml`** — dipakai oleh 3 fragment berbeda: `PlaylistFragment`, `LocalPlaylistFragment`, `StatisticsPlaylistFragment`
3. **`fragment_comments.xml`** — dipakai bersama oleh `CommentsFragment` dan `CommentRepliesFragment`
4. **FeedFragment.kt dan SubscriptionFragment.kt** — hybrid inconsistent: import binding class tapi masih pakai `inflater.inflate(R.layout.*)`
5. **Settings fragments** — semua pakai preference XML (`res/xml/`), bukan layout XML — ini memudahkan migrasi ke Compose preference

### Compose Infrastructure yang Sudah Ada (shared module)
- ✅ `ComposeActivity.kt` (Android entry point untuk Compose) — sudah bisa terima `Destination` via Intent extras
- ✅ `Context.navigateTo(destination)` extension function
- ✅ `navModule()` DSL dengan Koin-Nav3 integration
- ✅ `App()` composable dengan KoinApplication wrapper
- ✅ Platform abstractions: `ResourceHandler`, `ShareHandler` untuk Android/iOS/JVM
- ✅ `Settings` multiplatform (SharedPreferences/NSUserDefaults/Java Preferences)

### Koin DI — Module yang Perlu Ditambahkan
Saat ini hanya ada:
- `SerializationModule` (JSON)
- `ViewModelModule` (scan viewmodel package)
- `PlatformModule` (ResourceHandler, ShareHandler)
- `SettingsModule` (multiplatform settings)
- `navModule()` DSL (navigator + About navigation)

**Yang perlu ditambahkan untuk tiap screen baru:**
```kotlin
// Di navModule() DSL:
navigation<Destination.Home> { HomeScreen() }
navigation<Destination.Feed> { FeedScreen() }
navigation<Destination.Search> { SearchScreen() }
// ... dst

// ViewModel baru pakai @KoinViewModel annotation — otomatis di-scan
@KoinViewModel
class HomeViewModel(...) : ViewModel()
```
