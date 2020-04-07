# Changelog
This file gives a general overview, look into the git log for more information.

## unreleased
- Rewrite edit activity to use androidx.ViewModel
- Add entry "unknown <#>" to spinners on simple edit fragment

## 0.7.0
- Migrate to AndroidX
- Add bulk write activity

## 0.6.1
- Allow users to write Special2 field on EditExtended form

## 0.6.0
- Add support for reading and writing MifareUltralight NFC tags

## 0.5.2
- Allow Android 4.0.0 users to install the app

## 0.5.0
- Add support for Tonuino 2.1 (dev) tags
  - DONE: Modifier (Admin) tags
  - TODO: Play card

## 0.4.1
- Add signing for github release

## 0.4.0
- Add English translation
- Preparation for fdroid listing

## 0.3.1
- Display build number on main activity
- Fixed minor typos
- Disable write button if no tag is available or if it is removed

## 0.3.0
- Show error dialog when reading of a tag fails

## 0.2.0
- Add fragment to directly set the bytes to write on a tag (in Hexadecimal representation)
- Show result dialog after writing a tag

## 0.1.1
- Improve user guidance if NFC is disabled or unavailable
- Minor changes to improve stability

## 0.1.0
- Read and write MifareClassic NFC tags (if device and tag support the standard)
- Two different fragments to edit Tag content (simple and extended), both designed for Tonuino 2.0.1
