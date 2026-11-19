# ML Counters - Game Data Repository

This repository hosts the counter-pick data, item builds, and hero metadata used by the **ML Counters** Android app.

## 📁 Structure

```
├── manifest.json         # Version manifest (app checks this weekly)
├── heroes.json           # All 130+ heroes with roles and tiers
├── counters.json         # 834 counter matchup relationships
├── items.json            # 67 in-game items with stats
├── counter_items.json    # 390 item recommendations per hero
├── portraits/            # Hero portrait images (multiple resolutions)
│   ├── 35px/             # For screens < 720p
│   ├── 48px/             # For 720p-1080p
│   ├── 64px/             # For 1080p-1440p
│   └── 96px/             # For > 1440p
└── CHANGELOG.md
```

## 🔄 Weekly Updates

Data is updated weekly based on:
- **mlcounter.com** — Counter matchup data
- **mlbb.fyi** — Win rate and meta statistics
- **mlbb.gg** — Official Moonton-backed statistics
- **MLBB Fandom Wiki** — Hero details and item information

### How to Update

1. Edit the relevant JSON file(s)
2. Bump `version` in `manifest.json`
3. Update `lastUpdated` timestamp
4. Push to `main` branch

The app automatically checks for updates weekly using Android WorkManager.

## 📊 Data Format

### heroes.json
```json
{
  "id": 1,
  "name": "Aamon",
  "role": "Assassin",
  "secondaryRole": null,
  "portraitUrl": "https://raw.githubusercontent.com/.../portraits/aamon.png",
  "tier": "A"
}
```

### counters.json
```json
{
  "heroId": 1,
  "counterHeroId": 45,
  "counterScore": 8,
  "reason": "Specific mechanical reason"
}
```

### items.json
```json
{
  "id": 1,
  "name": "Blade Armor",
  "type": "Defense",
  "subType": "Physical Defense",
  "description": "...",
  "iconUrl": "...",
  "stats": "{\"physDef\": 90}",
  "passive": "..."
}
```

### counter_items.json
```json
{
  "againstHeroId": 1,
  "itemId": 5,
  "priority": 1,
  "reason": "..."
}
```

## 📸 Hero Portraits

Portraits are cropped square images of heroes as they appear in the MLBB draft/ban screen.

### Resolution Variants
| Resolution | Use Case | File Size |
|---|---|---|
| 35px | Low-res devices, lowered resolution | ~2KB |
| 48px | 720p devices | ~3KB |
| 64px | 1080p devices (default) | ~5KB |
| 96px | 1440p+ devices | ~8KB |

### Adding New Hero Portraits
1. Capture the hero portrait from the draft screen
2. Crop to a perfect square
3. Convert to grayscale
4. Resize to all 4 resolutions
5. Save as PNG: `portraits/{resolution}px/{hero_name_lowercase}.png`

## 🤝 Contributing

1. Fork this repository
2. Make your changes to the JSON files
3. Validate JSON format
4. Submit a Pull Request with a description of changes

## ⚖️ Disclaimer

This is a community-driven counter-pick reference tool. All game data, hero names, and item information are property of Moonton/ByteDance. This project is not affiliated with or endorsed by Moonton.
