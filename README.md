# TonUINO NFC Tools

Android app to write and read NFC tags that can be used on the [TonUINO DIY music box](https://tonuino.de/).

For discussion, see [this thread (in German)](http://discourse.voss.earth/t/android-app-um-tonuino-karten-zu-beschreiben/2151), or use github issues.

The app is available [for download on github](https://github.com/marc136/tonuino-nfc-tools/releases), 
installable using the [Google Play Store](https://play.google.com/store/apps/details?id=de.mw136.tonuino) or the [F-Droid Store](https://f-droid.org/packages/de.mw136.tonuino/).

The github releases are built on [CircleCI ![CircleCI](https://circleci.com/gh/marc136/tonuino-nfc-tools/tree/main.svg?style=svg)](https://circleci.com/gh/marc136/tonuino-nfc-tools/tree/main).  
Google Play releases are built using [fastlane](https://docs.fastlane.tools/getting-started/android/setup/).

## Getting started

Install [Android studio](https://developer.android.com/studio#downloads), and then [run this app on a real Android device](https://developer.android.com/training/basics/firstapp/running-app#RealDevice).

If you want more control check the docs on [building](https://developer.android.com/studio/build/building-cmdline) and [testing](https://developer.android.com/studio/test/command-line) from cli.

Follow the instructions on how to [run apps on a real hardware Android device](https://developer.android.com/studio/run/device).

These are a few commands I find useful:

```sh
# Create a clean build
./gradlew clean bundle

# If something fails, check the output of
./gradlew --version

# Run the linter
./gradlew lint

# Run the test suite with
./gradlew test
# Or
bundle exec fastlane test

# Deploy a debug build to a connected Android device
./gradlew installDebug

# Check if the Android device was properly connected
./gradlew connectedCheck

# Get a list of available gradle tasks
./gradlew tasks
```

## TODO
- [ ] Migrate away from deprecated `kotlin-android-extensions` plugin
> Warning: The 'kotlin-android-extensions' Gradle plugin is deprecated. Please use this migration guide (https://goo.gle/kotlin-android-extensions-deprecation) to start working with View Binding (https://developer.android.com/topic/libraries/view-binding) and the 'kotlin-parcelize' plugin.
    - [ ] Recreate the UI with [Jetpack compose](https://developer.android.com/jetpack/compose)
    - [ ] Use KTX extension libs https://developer.android.com/kotlin/ktx
- [ ] Replace qr code scanner lib as it is no longer maintained
- [ ] Get BytesFormatter to work without issues
- [ ] Hide on-screen-keyboard when switching between edit fragments
- [ ] Allow users to write a note or title to the NFC tag
- [ ] Allow users to keep a list of tags
- [ ] Simulate NFC tag
- [ ] Use proper Material design theming instead of faking it
    - On MainActivity if NFC is not available

## Tonuino 2.0.x
[Repository](https://github.com/xfjx/TonUINO/blob/d15df6c7bb53bc970e4def43fd3e93fd82c13086/Tonuino.ino)

### Data

| # | name | range | description |
| --- | --- | --- | --- |
| 0 | cookie | uint32 | identifies TonUINO, expected `0x1337b347` |
| 4 | version | uint8 | always 1 |
| 5 | folder | uint8 | values between 1 and 99, see [dfplayer docs](https://wiki.dfrobot.com/DFPlayer_Mini_SKU_DFR0299#target_1)  |
| 6 | mode | uint8 | values between 1 and 5, see next table |
| 7 | special | uint8 | |


**Different Modes**  
Based on the value of byte *mode* and what happens, when the *next* and *previous* buttons are pressed

| # | title | description | next | previous |
| --- | --- | --- | --- | --- |
| 1 | Hörspiel<br/>audio book (single file) | plays a single file in the folder | stop | Start title again from the beginning |
| 2 | Album | play every file in the folder | next file | previous file |
| 3 | Party | play files in the folder in random order | play random file | start title again from the beginning  |
| 4 | Einzel<br/>single | play a single file in the folder | stop | start title again from the beginning |
| 5 | Hörbuch<br/>audio book (multiple files) | play all files in the folder and keep track of the progress (will start at the last file the next time | next file | previous file |
| 6 | Admin | Not functionality | - | - |

**Special**  
Is only used in specific modes to specify variables:
- mode 4: file number
- mode 6: placeholder, no functionaliy yet

## Tonuino 2.1.0
[Repository](https://github.com/xfjx/TonUINO/blob/DEV/Tonuino.ino)


#### Normal Tags
| # | name     | range  | description                                                                                           |
|---|----------|--------|-------------------------------------------------------------------------------------------------------|
| 0 | cookie   | uint32 | Identifies a Tonuino tag, usually `0x1337b347`                                                        |
| 4 | version  | uint8  | always 2                                                                                              |
| 5 | folder   | uint8  | Values from 1 to 99, see [dfplayer docs](https://wiki.dfrobot.com/DFPlayer_Mini_SKU_DFR0299#target_1) |
| 6 | mode     | uint8  | Values from 1 to 5, see next table                                                                    |
| 7 | special  | uint8  |                                                                                                       |
| 8 | special2 | uint8  |                                                                                                       |

**Different Modes**  
It supports all modes of TonUINO 2.0 and adds these

| # | title            | description                                                     | next        | previous                             |
|---|------------------|-----------------------------------------------------------------|-------------|--------------------------------------|
| 7 | Von-Bis Hörspiel | play a random file from the folder between special and special2 | stop        | start title again from the beginning |
| 8 | Von-Bis Album    | play all files between special and special2                     | next file   | start title again from the beginning |
| 9 | Von-Bis Party    | play all files between special and special2 in random order     | random file | start title again from the beginning |

#### Modifier Tags (aka admin tags)
All have a folder value of 0

| Modifier             | byte 6 | byte 7                             | Description                                                                                                                |
|----------------------|--------|------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| SleepTimer           | 1      | Play duration in minutes (255 max) | Pauses playback after timer                                                                                                |
| FreezeDance          | 2      | -                                  | Randomly pauses the track after 5 to 30 seconds                                                                            |
| Locked               | 3      | -                                  | All buttons are locked and no new card will be read                                                                        |
| ToddlerMode          | 4      | -                                  | All buttons are locked                                                                                                     |
| KindergartenMode     | 5      | -                                  | Previous and back buttons are locked. Adding a new card will not stop the current track but will schedule it as next track |
| RepeatSingleModifier | 6      | -                                  | Repeat current track                                                                                                       |
| FeedbackModifier     | 7      | -                                  | Will e.g. tell volume before changing it                                                                                   |


## NFC Host Card Emulation
It would be great if the app also can act as an NFC tag to simulate TonUINO tags, some links to this:
[Official Android docs](https://developer.android.com/guide/topics/connectivity/nfc/hce.html#HCE)

From a [question on Mifare support forum](https://www.mifare.net/support/forum/topic/creating-nfc-android-app-to-act-as-mifare-card-to-interact-with-mifare-readers/):
>  Here is a very good example https://github.com/grundid/host-card-emulation-sample which will write to you "Hello Desktop!" The App emulates a MIFARE card with this AID 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 You should make your self familiar with the HCE code.
>
> In the other side you should have an NFC reader (in your case MIFARE NFC) which will be looking for the same AID as in the HCE App. Normally you should send this cmd to the Android phone from your reader after you have brought the two devices close enough 0x00 [CLA], 0xA4 [INS], 0x04, 0x00, 0x07 [Lc], 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00 [Le]


## NFC tag type support
Explanation of [how discovered NFC tags are dispatched to activities in Android](https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#dispatching)

Currently only Mifare Classic and Ultralight are supported, but in case generic NfcA support is wished, start with these links:

- [Android Tag Technology (NfcA, NfcB, ..)](https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc)
- [Stackoverflow answer that also contains a command overview of NfcA commands](https://stackoverflow.com/a/42915018)
- [Stackoverflow answer with general help on NfcA](https://stackoverflow.com/a/40303293)
- [Summary of Open Source projects using Android's NfcA API](https://www.programcreek.com/java-api-examples/index.php?api=android.nfc.tech.NfcA)
    - [Example from above's list that actually uses NfcA (and not Mifare)](https://www.programcreek.com/java-api-examples/?code=ProjectMAXS/maxs/maxs-master/module-nfc/src/org/projectmaxs/module/nfc/tech/NfcAHandler.java#)
- [Example of writing NTAG215 tags (for cloning Amiibos)](https://github.com/HiddenRamblings/TagMo)
  - ...but they [use Mifare Ultralight](https://github.com/HiddenRamblings/TagMo/blob/master/app/src/main/java/com/hiddenramblings/tagmo/NfcActivity.java#L152)
  - [Explanation how to use it](https://www.reddit.com/r/Amiibomb/comments/5ywlol/howto_the_easy_guide_to_making_your_own_amiibo/)


