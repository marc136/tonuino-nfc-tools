# Changelog
This file gives a general overview, look into the git log for more information.  
The list contains the version number and then in brackets the app version code (which is important for Google Play).

## 1.0.3 (#28)
- Add new [tags types](https://github.com/marc136/tonuino-nfc-tools/issues/23)

## 1.0.2 (#27)
- Add new [tags types for Tonuino TNG 3.1](https://github.com/marc136/tonuino-nfc-tools/blob/05c0f6577ecbde7859022346c89ee3fe366b14cf/README.md#tonuino-tng-31x)
- Make Tonuino 2.1 and TNG 3.1 tag format the default
- Add support for Android 14
  
## 1.0.1 (#26)
- Fix support for Android 12

## 1.0.0 (#24, #25)
- Add support for writing tags that do not fully comply with Mifare Ultralight (often NFC sticker tags) using the NfcA standard.

## 0.9.1 (#21, #22, #23)
- Prevent appcrash when reading modifier cards
- Update to support Android 12 (SDK 31)

## 0.9.0 (#20)
- Add a QR code scanner to the bulk edit view
- Fix another bug displaying TonUINO 2.1 mode description texts

## 0.8.1 (#19)
- Fix the display of TonUINO 2.1 mode description texts

## 0.8.0 (#18)
- Rewrite edit activity to use androidx.ViewModel
- Add entry "unknown <#>" to spinners on simple edit fragment

## 0.7.0 (#16)
- Migrate to AndroidX
- Add bulk write activity

## 0.6.1 (#14)
- Allow users to write Special2 field on EditExtended form

## 0.6.0 (#13)
- Add support for reading and writing MifareUltralight NFC tags

## 0.5.3 (#12)
- Allow Android 4.0.0 users to install the app

## 0.5.0 (#9)
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
