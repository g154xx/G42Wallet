# G42Wallet

EMV Offline POS Tester & Force Tool

## Setup

```bash
gradle assembleDebug
```

## Project Structure

- `engine/` - OfflineTester (Passive/Active detection) & OfflineForce
- `emv/` - EMV constants, TLV parser, APDU builder
- `nfc/` - HCE Card Service with captured real-card responses
- `utils/` - Hex utilities, Track2 generator, Logger

## Features

1. **Diagnostic** - Test if POS supports offline mode
2. **Load Keys** - Load your dynamic EMV keys
3. **Generate Track2** - Generate valid Track2 with Luhn check digit
4. **Offline Force** - Execute offline transaction using captured card responses

## GitHub Actions

The included workflow builds a debug APK on every push to `main` or `master`.
