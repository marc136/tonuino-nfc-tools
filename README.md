# TonUINO NFC Tools

Android app to write and read NFC tags that can be used on the [TonUINO DIY music box](https://tonuino.de/).

For discussion, see [this thread (in German)](http://discourse.voss.earth/t/android-app-um-tonuino-karten-zu-beschreiben/2151), or use github issues.

The app is available [for download on github](https://github.com/marc136/tonuino-nfc-tools/releases), 
installable using the [Google Play Store](https://play.google.com/store/apps/details?id=de.mw136.tonuino) or the [F-Droid Store](https://f-droid.org/packages/de.mw136.tonuino/).

The github releases are built on [CircleCI ![CircleCI](https://circleci.com/gh/marc136/tonuino-nfc-tools/tree/master.svg?style=svg)](https://circleci.com/gh/marc136/tonuino-nfc-tools/tree/master).  
Google Play releases are built using [fastlane](https://docs.fastlane.tools/getting-started/android/setup/).

## TODO

- [ ] Hide on-screen-keyboard when switching between edit fragments
- [x] Add entry "unknown <#>" to mode spinner on EditSimple fragment
- [ ] Allow users to write a note or title to the NFC tag
- [ ] Allow users to keep a list of tags
- [x] Allow users to write a bulk list of tags
- [x] Add support for further NFC tag technologies
  - [x] Mifare Ultralight
  - ~[ ] Generic NfcA~
- [ ] Simulate NFC tag
- [ ] Use proper Material design theming instead of faking it
    - MainActivity if NFC is not available
- [x] Use data binding
  - [x] on BulkWriteActivity
  - [x] on EditActivity

## Tonuino 2.0.x
[Repository](https://github.com/xfjx/TonUINO/blob/d15df6c7bb53bc970e4def43fd3e93fd82c13086/Tonuino.ino)

### Daten

| # | name | range | description |
| --- | --- | --- | --- |
| 0 | cookie | uint32 | Identifiziert Tonuino, erwartet `0x1337b347` |
| 4 | version | uint8 | immer 1 |
| 5 | folder | uint8 | Werte von 1 bis 99, siehe [dfplayer doku](https://wiki.dfrobot.com/DFPlayer_Mini_SKU_DFR0299#target_1)  |
| 6 | mode | uint8 | Werte von 1 bis 5, siehe nächste Tabelle |
| 7 | special | uint8 | |


**Verschiedene Modi**  
Basierend auf dem Wert von *mode* und was geschieht, wenn die *vorwärts* und *zurück* Tasten betätigt werden

| # | Titel | Beschreibung | vorwärts| zurück |
| --- | --- | --- | --- | --- |
| 1 | Hörspiel | eine zufällige Datei aus dem Ordner | stop | Titel von vorne spielen |
| 2 | Album | kompletten Ordner spielen | nächster Titel | vorherigen Titel spielen |
| 3 | Party | Ordner in zufälliger Reihenfolge abspielen | zufälliger Titel | Titel von vorne spielen  |
| 4 | Einzel | Eine Datei aus dem Ordner abspielen | stop | Titel von vorne spielen |
| 5 | Hörbuch | kompletten Ordner spielen und Fortschritt merken | nächster Titel | vorherigen Titel spielen |
| 6 | Admin | Noch ohne Funktion | - | - |

**Special**  
Wird nur in bestimmten Modi benutzt für:
- mode 4: Titelnummer
- mode 6: Platzhalter, bisher ohne Funktion

## Tonuino 2.1.0
[Repository](https://github.com/xfjx/TonUINO/blob/DEV/Tonuino.ino)


#### Normal Tags
| # | name | range | description |
| --- | --- | --- | --- |
| 0 | cookie | uint32 | Identifies a Tonuino tag, usually `0x1337b347` |
| 4 | version | uint8 | always 2 |
| 5 | folder | uint8 | Values from 1 to 99, see [dfplayer docs](https://wiki.dfrobot.com/DFPlayer_Mini_SKU_DFR0299#target_1)  |
| 6 | mode | uint8 | Values from 1 to 5, see next table |
| 7 | special | uint8 | |
| 8 | special2 | uint8 | |

**Different Modes**  
Neue Modi, die in Tonuino 2.1 dazugekommen sind.

| # | Titel | Beschreibung | vorwärts| zurück |
| --- | --- | --- | --- | --- |
| 7 | Von-Bis Hörspiel | Eine zufällige Datei aus dem Ordner zwischen special und special2 | stop | Titel von vorne spielen |
| 8 | Von-Bis Album | Alle Titel zwischen special und special2 | nächster Titel | vorherigen Titel spielen |
| 9 | Von-Bis Party | Alle Titel zwischen special und special2 in zufälliger Reihenfolge abspielen | zufälliger Titel | Titel von vorne spielen  |

#### Modifier Tags
All have a folder value of 0

| Modifier | byte 6 | byte 7 | Description |
| --- | --- | --- | --- |
| SleepTimer | 1 | Play duration in minutes (255 max) | Pauses playback after timer |
| FreezeDance | 2 | - | Randomly pauses the track after 5 to 30 seconds |
| Locked | 3 | - | All buttons are locked and no new card will be read |
| ToddlerMode | 4 | - | All buttons are locked |
| KindergartenMode | 5 | - | Previous and back buttons are locked. Adding a new card will not stop the current track but will schedule it as next track |
| RepeatSingleModifier | 6 | - | Repeat current track |
| FeedbackModifier | 7 | - | Will e.g. tell volume before changing it |


## NFC
How discovered NFC tags are dispatched to activities: 
https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#dispatching

Currently only Mifare Classic and Ultralight, but I want to look into supporting generic NfcA in the future.
Links:

- [Android Tag Technology (NfcA, NfcB, ..)](https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc)
- [Stackoverflow answer that also contains a command overview of NfcA commands](https://stackoverflow.com/a/42915018)
- [Stackoverflow answer with general help on NfcA](https://stackoverflow.com/a/40303293)
- [Summary of Open Source projects using Android's NfcA API](https://www.programcreek.com/java-api-examples/index.php?api=android.nfc.tech.NfcA)
    - [Example from above's list that actually uses NfcA (and not Mifare)](https://www.programcreek.com/java-api-examples/?code=ProjectMAXS/maxs/maxs-master/module-nfc/src/org/projectmaxs/module/nfc/tech/NfcAHandler.java#)
- [Example of writing NTAG215 tags (for cloning Amiibos)](https://github.com/HiddenRamblings/TagMo)
  - ...but they [use Mifare Ultralight](https://github.com/HiddenRamblings/TagMo/blob/master/app/src/main/java/com/hiddenramblings/tagmo/NfcActivity.java#L152)
  - [Explanation how to use it](https://www.reddit.com/r/Amiibomb/comments/5ywlol/howto_the_easy_guide_to_making_your_own_amiibo/)

## NFC Host Card Emulation
It would be great if the app also can act as an NFC tag to simulate TonUINO tags, some links to this:
[Official Android docs](https://developer.android.com/guide/topics/connectivity/nfc/hce.html#HCE)

From a [question on Mifare support forum](https://www.mifare.net/support/forum/topic/creating-nfc-android-app-to-act-as-mifare-card-to-interact-with-mifare-readers/):
>  Here is a very good example https://github.com/grundid/host-card-emulation-sample which will write to you "Hello Desktop!" The App emulates a MIFARE card with this AID 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 You should make your self familiar with the HCE code.
>
> In the other side you should have an NFC reader (in your case MIFARE NFC) which will be looking for the same AID as in the HCE App. Normally you should send this cmd to the Android phone from your reader after you have brought the two devices close enough 0x00 [CLA], 0xA4 [INS], 0x04, 0x00, 0x07 [Lc], 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00 [Le]




