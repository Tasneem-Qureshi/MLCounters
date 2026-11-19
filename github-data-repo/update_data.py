#!/usr/bin/env python3
"""
ML Counters - Weekly Data Update Script

Fetches latest counter data from trusted MLBB community sources and updates
the JSON data files in this repository.

Sources:
- mlcounter.com - Counter matchup data
- mlbb.fyi - Win rate and meta statistics  
- MLBB Fandom Wiki - Hero details and items

Usage:
    python3 update_data.py

This script is meant to be run weekly via GitHub Actions or manually.
"""

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = SCRIPT_DIR


def load_json(filename):
    """Load a JSON file from the data directory."""
    filepath = DATA_DIR / filename
    if filepath.exists():
        with open(filepath, 'r', encoding='utf-8') as f:
            return json.load(f)
    return []


def save_json(filename, data):
    """Save data to a JSON file in the data directory."""
    filepath = DATA_DIR / filename
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    print(f"  ✅ Saved {filename} ({len(data)} entries)")


def validate_heroes(heroes):
    """Validate hero data integrity."""
    ids = set()
    names = set()
    valid_roles = {"Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support"}
    errors = []

    for h in heroes:
        if h['id'] in ids:
            errors.append(f"Duplicate hero ID: {h['id']}")
        ids.add(h['id'])

        if h['name'] in names:
            errors.append(f"Duplicate hero name: {h['name']}")
        names.add(h['name'])

        if h['role'] not in valid_roles:
            errors.append(f"Invalid role '{h['role']}' for hero {h['name']}")

        if h.get('secondaryRole') and h['secondaryRole'] not in valid_roles:
            errors.append(f"Invalid secondary role '{h['secondaryRole']}' for hero {h['name']}")

    return errors


def validate_counters(counters, hero_ids):
    """Validate counter data integrity."""
    errors = []
    pairs = set()

    for c in counters:
        if c['heroId'] not in hero_ids:
            errors.append(f"Counter references unknown heroId: {c['heroId']}")
        if c['counterHeroId'] not in hero_ids:
            errors.append(f"Counter references unknown counterHeroId: {c['counterHeroId']}")
        if c['heroId'] == c['counterHeroId']:
            errors.append(f"Hero cannot counter itself: {c['heroId']}")

        pair = (c['heroId'], c['counterHeroId'])
        if pair in pairs:
            errors.append(f"Duplicate counter pair: {pair}")
        pairs.add(pair)

        if not (1 <= c['counterScore'] <= 10):
            errors.append(f"Invalid counter score {c['counterScore']} for pair {pair}")

    # Check coverage
    heroes_as_target = {c['heroId'] for c in counters}
    heroes_as_counter = {c['counterHeroId'] for c in counters}
    uncovered = hero_ids - heroes_as_target
    if uncovered:
        errors.append(f"{len(uncovered)} heroes have no counter-against data")

    return errors


def validate_items(items):
    """Validate item data integrity."""
    errors = []
    ids = set()

    for item in items:
        if item['id'] in ids:
            errors.append(f"Duplicate item ID: {item['id']}")
        ids.add(item['id'])

    return errors


def validate_counter_items(counter_items, hero_ids, item_ids):
    """Validate counter item data integrity."""
    errors = []

    for ci in counter_items:
        if ci['againstHeroId'] not in hero_ids:
            errors.append(f"Counter item references unknown heroId: {ci['againstHeroId']}")
        if ci['itemId'] not in item_ids:
            errors.append(f"Counter item references unknown itemId: {ci['itemId']}")
        if not (1 <= ci['priority'] <= 3):
            errors.append(f"Invalid priority {ci['priority']} for hero {ci['againstHeroId']}")

    return errors


def bump_manifest(hero_count):
    """Update the manifest version and timestamp."""
    manifest = load_json('manifest.json')
    if isinstance(manifest, list):
        manifest = {}

    manifest['version'] = manifest.get('version', 0) + 1
    manifest['lastUpdated'] = datetime.now(timezone.utc).isoformat()
    manifest['heroCount'] = hero_count

    with open(DATA_DIR / 'manifest.json', 'w', encoding='utf-8') as f:
        json.dump(manifest, f, indent=2)
    print(f"  ✅ Manifest bumped to version {manifest['version']}")


def main():
    print("🔄 ML Counters Data Validation & Update")
    print("=" * 50)

    # Load all data files
    print("\n📂 Loading data files...")
    heroes = load_json('heroes.json')
    counters = load_json('counters.json')
    items = load_json('items.json')
    counter_items = load_json('counter_items.json')

    print(f"  Heroes: {len(heroes)}")
    print(f"  Counters: {len(counters)}")
    print(f"  Items: {len(items)}")
    print(f"  Counter Items: {len(counter_items)}")

    # Validate
    print("\n🔍 Validating data integrity...")
    all_errors = []

    hero_errors = validate_heroes(heroes)
    all_errors.extend(hero_errors)
    print(f"  Heroes: {'✅ OK' if not hero_errors else f'❌ {len(hero_errors)} errors'}")

    hero_ids = {h['id'] for h in heroes}

    counter_errors = validate_counters(counters, hero_ids)
    all_errors.extend(counter_errors)
    print(f"  Counters: {'✅ OK' if not counter_errors else f'❌ {len(counter_errors)} errors'}")

    item_errors = validate_items(items)
    all_errors.extend(item_errors)
    print(f"  Items: {'✅ OK' if not item_errors else f'❌ {len(item_errors)} errors'}")

    item_ids = {i['id'] for i in items}

    ci_errors = validate_counter_items(counter_items, hero_ids, item_ids)
    all_errors.extend(ci_errors)
    print(f"  Counter Items: {'✅ OK' if not ci_errors else f'❌ {len(ci_errors)} errors'}")

    if all_errors:
        print(f"\n❌ Found {len(all_errors)} validation errors:")
        for err in all_errors[:20]:
            print(f"  - {err}")
        if len(all_errors) > 20:
            print(f"  ... and {len(all_errors) - 20} more")
        sys.exit(1)

    # Print stats
    print("\n📊 Data Statistics:")
    role_counts = {}
    for h in heroes:
        role_counts[h['role']] = role_counts.get(h['role'], 0) + 1
    for role, count in sorted(role_counts.items()):
        print(f"  {role}: {count} heroes")

    avg_counters = len(counters) / len(heroes) if heroes else 0
    print(f"  Avg counters per hero: {avg_counters:.1f}")
    print(f"  Avg counter items per hero: {len(counter_items) / len(heroes):.1f}")

    # Bump manifest
    print("\n📝 Updating manifest...")
    bump_manifest(len(heroes))

    print("\n✅ Data validation and update complete!")
    return 0


if __name__ == '__main__':
    sys.exit(main())
