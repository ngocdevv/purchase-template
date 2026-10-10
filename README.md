## 📱 Demo

<div align="center">
  
![Demo](video.gif)

</div>

## Native ports

Two standalone apps reproduce the purchase UI in the original React Native source and [the X demo](https://x.com/ngocdevv/status/1982074052136337769):

| Platform | Project | Build/run guide |
| --- | --- | --- |
| iOS | SwiftUI + Metal | [ios-swift/README.md](ios-swift/README.md) |
| Android | Kotlin + native Views + AGSL | [android-kotlin/README.md](android-kotlin/README.md) |

Both apps run independently of Expo, React Native, and Metro. The original source remains in `src/`.

The ports reproduce the bottom sheet, staggered spring entrance, Yearly `$25` / Monthly `$18` selection, sliding segment highlight, digit flips, animated fabric, benefit list, and glossy press feedback. They use the white launch screen and black subtitle observed in the video, where the checked-in React Native styling differs.

`Start free trial` is a UI demo, just as in the original source: no StoreKit, Google Play Billing, or purchase request is connected. Prices and the `Save 25%` badge are copied from the reference.

Simulator/emulator findings and captures are recorded in [NATIVE_VALIDATION.md](NATIVE_VALIDATION.md).
