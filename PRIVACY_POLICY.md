# Privacy Policy for ML Counters

**Last updated:** January 15, 2025

## Overview
ML Counters ("the App") is a free mobile application that provides counter-pick recommendations for Mobile Legends: Bang Bang. This privacy policy explains how the App handles user data.

## Data We Collect

### Data Stored Locally on Your Device
- **Hero Mastery Preferences:** Your hero mastery levels and notes are stored locally in the app's database on your device.
- **App Settings:** Detection sensitivity, overlay opacity, and other preferences are stored locally.
- **No Account Required:** The App does not require user registration or login.

### Screen Capture Data
- The App uses Android's MediaProjection API to capture your screen for hero detection during gameplay.
- **Screen captures are processed entirely on-device** using OpenCV template matching.
- **No screenshots are saved, stored, or transmitted** to any server.
- Screen capture data is immediately discarded after hero detection processing.

### Network Data
- The App periodically downloads updated counter data from a public GitHub repository.
- **No personal data is transmitted** during these updates.
- Standard HTTP request metadata (IP address, user agent) may be logged by GitHub's servers per their own privacy policy.

## Data We Do NOT Collect
- ❌ Personal information (name, email, phone)
- ❌ Location data
- ❌ Device identifiers
- ❌ Usage analytics or telemetry
- ❌ Screenshots or screen recordings
- ❌ Game account information
- ❌ Advertising data

## Third-Party Services
- **GitHub (Microsoft):** Used to host counter data files. Subject to [GitHub's Privacy Policy](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).
- **Coil Image Loading:** Loads hero portrait images from URLs. No tracking or analytics.

## Data Security
All user data (mastery levels, settings) is stored locally on your device using Android's encrypted storage. The App does not transmit any user-generated data.

## Children's Privacy
The App does not knowingly collect data from children under 13. The App contains no advertising, in-app purchases, or social features.

## Changes to This Policy
We may update this privacy policy from time to time. Changes will be noted in the app's Settings screen.

## Contact
For questions about this privacy policy, please open an issue on our GitHub repository.

## Permissions Used
| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Display floating overlay during gameplay |
| `FOREGROUND_SERVICE` | Keep screen capture and overlay running |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Capture screen for hero detection |
| `INTERNET` | Download counter data updates |
| `ACCESS_NETWORK_STATE` | Check connectivity before sync |
| `POST_NOTIFICATIONS` | Show notification when overlay is active |
