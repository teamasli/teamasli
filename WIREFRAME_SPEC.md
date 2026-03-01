# Bad Habit Tracker (No Login, No Notifications)

## Product constraints
- No login/registration flow.
- No push, in-app, or scheduled notifications.
- Fully local-first UX (single device).

## Information architecture
1. Splash
2. Welcome / First-time setup
3. Home Dashboard
4. Add Habit
5. Habit Detail
6. Log Urge
7. Log Relapse
8. Coping Tools
9. Insights
10. History / Timeline
11. Settings
12. Export Data

---

## 1) Splash Screen
**Goal:** Brand loading and fast app entry.

**Wireframe layout:**
- Top: App logo mark
- Center: App name (e.g., "IRON Will Lite")
- Bottom: subtle loading indicator

**Primary actions:**
- Auto-navigate to Welcome (first launch) or Home (returning user)

**Notes:**
- No auth gate.

---

## 2) Welcome / First-time Setup
**Goal:** Initialize first habit and motivation in under 60 seconds.

**Wireframe layout:**
- Header: "Take control of one habit at a time"
- Step indicator: `Step 1 of 2`
- Input: Habit name field (e.g., "Smoking")
- Optional chips: common habits
- CTA button: `Continue`

Step 2:
- Step indicator: `Step 2 of 2`
- Input: "Why do you want to quit?" multiline
- Date selector: quit start date (default: today)
- CTA button: `Start Tracking`

**Primary actions:**
- Continue
- Start Tracking

**Secondary actions:**
- Skip reason

**Notes:**
- No account creation.

---

## 3) Home Dashboard
**Goal:** Show current momentum and quick logging actions.

**Wireframe layout:**
- Top app bar: app name + settings icon
- Main card:
  - Habit name
  - Current streak (days/hours)
  - Best streak
- Stats row:
  - Urges today
  - Relapses this week
  - Clean days total
- Quick action buttons:
  - `I have an urge`
  - `I relapsed`
  - `Coping tools`
- Bottom section: "Your reason" pinned quote text
- Bottom navigation:
  - Home
  - Insights
  - History

**Primary actions:**
- Open logging flows quickly (1 tap)
- Navigate to detail/insights/history

**Notes:**
- No notification panel entry points.

---

## 4) Add Habit Screen
**Goal:** Add additional habits to track.

**Wireframe layout:**
- Header: `Add Habit`
- Inputs:
  - Habit name
  - Category dropdown
  - Start date picker
  - Motivation note
- Toggles:
  - "Track urges" (on by default)
  - "Track relapses" (on by default)
- CTA: `Save Habit`

**Primary actions:**
- Save new habit

**Secondary actions:**
- Cancel

---

## 5) Habit Detail Screen
**Goal:** Deep view for one habit.

**Wireframe layout:**
- Header: habit name + overflow menu
- KPI strip:
  - Current streak
  - Best streak
  - Total urges
  - Total relapses
- Graph block:
  - 7/30 day trend mini chart
- Action row:
  - `Log urge`
  - `Log relapse`
  - `Open tools`
- Notes section:
  - personal reason / reflections

**Primary actions:**
- Log events
- Edit habit

**Secondary actions:**
- Archive/delete habit via overflow

---

## 6) Log Urge Screen
**Goal:** Capture urge in <10 seconds.

**Wireframe layout:**
- Header: `Log Urge`
- Intensity slider: 1–10
- Trigger chips: Stress, Boredom, Social, Anxiety, Other
- Optional note field
- Optional selector: coping action used
- CTA: `Save Urge`

**Primary actions:**
- Save urge

**Secondary actions:**
- Dismiss

---

## 7) Log Relapse Screen
**Goal:** Record relapse without shame and continue.

**Wireframe layout:**
- Header: `Log Relapse`
- Text prompt: "Setbacks happen. Let’s learn and move forward."
- Trigger chips
- Optional reflection note
- Checkbox: "I want to restart streak now" (default checked)
- CTA: `Save & Continue`

**Primary actions:**
- Save relapse

**Secondary actions:**
- Back

---

## 8) Coping Tools Screen
**Goal:** Offer immediate support during cravings.

**Wireframe layout:**
- Header: `Coping Tools`
- Tool cards:
  1. `2-Minute Breathing` (start timer)
  2. `Delay 10 Minutes` (countdown)
  3. `Read My Reasons` (shows motivation notes)
  4. `Grounding Exercise (5-4-3-2-1)`
- Footer quick CTA: `I’m okay now`

**Primary actions:**
- Start coping activity
- Return to dashboard

---

## 9) Insights Screen
**Goal:** Visualize progress trends over time.

**Wireframe layout:**
- Header: `Insights`
- Time range segmented control: 7D / 30D / 90D
- Charts:
  - Urges trend line
  - Relapses bar chart
- Heatmap block: urge time-of-day pattern
- Summary cards:
  - Most common trigger
  - Best streak
  - Improvement vs previous period

**Primary actions:**
- Change timeframe
- Tap chart point for details

---

## 10) History / Timeline Screen
**Goal:** View all logged events chronologically.

**Wireframe layout:**
- Header: `History`
- Filter chips: All / Urges / Relapses / Wins
- Date-grouped timeline list:
  - Event type icon
  - Timestamp
  - Trigger + note preview
- Search field (optional)

**Primary actions:**
- Filter
- Open event details / edit

---

## 11) Settings Screen (No Login, No Notifications)
**Goal:** Manage app behavior and privacy locally.

**Wireframe layout:**
- Header: `Settings`
- Section: Preferences
  - Theme (System/Light/Dark)
  - Week starts on (Mon/Sun)
- Section: Privacy
  - App lock PIN toggle
  - Hide sensitive text toggle
- Section: Data
  - Export data
  - Import backup
  - Clear all data
- Section: About
  - Version
  - Privacy policy

**Explicitly excluded rows:**
- Login / Account
- Notification settings

---

## 12) Export Data Screen
**Goal:** Let user keep ownership of local progress.

**Wireframe layout:**
- Header: `Export Data`
- Format options:
  - JSON
  - CSV
- Include options:
  - Habits
  - Urges
  - Relapses
  - Notes
- CTA: `Generate Export`
- Result area: share/save file button

**Primary actions:**
- Export/share backup

---

## Navigation Map
- Splash → Welcome Setup → Home
- Home → (Add Habit, Habit Detail, Log Urge, Log Relapse, Coping Tools, Insights, History, Settings)
- Habit Detail → Log Urge / Log Relapse / Coping Tools
- Settings → Export Data

---

## Empty States
- Home: "No habits yet" + `Add first habit`
- Insights: "Not enough data yet" + `Log your first urge`
- History: "No events recorded"

---

## Component Inventory (for design handoff)
- App bar
- Bottom nav (3 tabs)
- KPI stat cards
- Trend chart cards
- Chip groups (triggers/filters)
- Primary/secondary CTA buttons
- Timeline list item
- Modal confirm dialog (delete/clear data)

---

## Copy tone guidelines
- Supportive, non-judgmental, concise.
- Replace failure language with progress language.
  - Example: "Relapse saved. You can restart now." 

---

## Out-of-scope for this version
- Authentication / cloud sync
- Notifications/reminders
- Social/community features
- Gamified leaderboards
