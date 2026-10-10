# Đối chiếu bản Swift và Kotlin

Kiểm tra ngày 10/10/2026. Reference: [video anh đăng trên X](https://x.com/ngocdevv/status/1982074052136337769). Đã tải và xem MP4 gốc 720×1562, 30 fps, dài 34.77 giây; đối chiếu các frame Yearly ở giây 18 và Monthly ở giây 25 với source React Native.

## Kết quả

Hai project native tái hiện bố cục, nội dung, giá, lựa chọn gói và các loại hiệu ứng chính của video. Cả hai đã build và chạy trên simulator/emulator. Bản iOS dùng SwiftUI + Metal; bản Android dùng Kotlin Views + AGSL, chạy độc lập với Expo/Metro.

| Kiểm tra | iOS | Android |
| --- | --- | --- |
| Build | Xcode 27.1, simulator build thành công | Gradle assembleDebug thành công |
| Kiểm tra tĩnh Android | — | Android Lint thành công |
| Mở Purchase → sheet | Đạt | Đạt |
| Yearly, Save 25%, `$25`, card hồng | Đạt | Đạt |
| Monthly, `$18`, card xanh navy | Đạt | Đạt |
| Đổi gói liên tục, giữ lựa chọn cuối | Đạt, 4 tap cách nhau khoảng 130–150 ms | Đạt, 4 tap luân phiên |
| Highlight đổi vị trí/chiều rộng, giá flip | Đạt | Đạt |
| Nền vải chuyển động, đổi màu đúng gói | Đạt | Đạt trên API 36 |
| Đóng bằng backdrop và kéo handle | Đạt | Đạt |
| Android Back | — | Đạt |
| Mở lại giữ gói Monthly | Đạt | Đạt |
| CTA nhấn và ở lại màn hình demo | Đạt | Đạt |
| App xuống nền rồi mở lại giữ gói | Đạt | Đạt |
| Nút Purchase phía sau không còn trong hierarchy khi sheet mở | Đạt | Đạt |
| Chữ lớn 1.6 lần: badge một dòng, giá rõ, cuộn tới CTA | Chưa chạy trực tiếp | Đạt; đã trả font scale về 1.0 |

Thiết bị kiểm tra: iPhone 18 Pro simulator, iOS 27.2, màn hình 402×874 pt; Android emulator `OTPInputPerfAPI36`, API 36. Các thao tác cài app, mở app, tap, swipe và chụp/quay màn hình dùng Argent MCP. Kiểm tra bằng ảnh/video và accessibility tree; đây là kiểm tra UI trực tiếp, chưa tạo bộ E2E regression tự động.

## Phần khớp và phần còn khác video

- Nội dung giữ nguyên: `One price.One plan`, subtitle, năm quyền lợi, `All round secure payment`, `Start free trial`; Yearly `$25` và Monthly `$18`.
- Card cao 240, lề ngang 32, radius 28; sheet có góc trên 42, backdrop opacity 0.5. Bố cục iOS ở cỡ chữ mặc định gần reference, sai lệch đo trên ảnh khoảng 1–2 pt khi quy đổi về cùng chiều rộng.
- Shader giữ nguyên biểu thức wave, wrinkle, diffuse, specular, AO và màu của Skia. Tọa độ local card chia cho kích thước **toàn màn hình**, nên không bị đổi sang các nếp vải nhỏ và dày hơn. Màu chụp thực tế nằm trong cùng dải rose/navy của reference.
- Màn hình đầu trắng và subtitle đen theo video. Source `App.tsx` có nền đen; subtitle có style opacity 0.6 nhưng animation trong clip kết thúc với chữ đen.
- **Tốc độ animation còn khác clip X**: trong bản quay iOS, nội dung gần ổn định sau khoảng 2–3 giây kể từ mở; clip X mất khoảng 6–10 giây. Android có các lớp fade/translate/zoom chậm hơn iOS, khoảng 5–6 giây để gần ổn định trong bản quay kiểm tra. Native giữ các thông số spring và thời lượng 1 giây cho segment, 350 ms/stagger 40 ms cho giá từ source; đường cong duration spring được ánh xạ sang animation native. Clip có thể đã dùng chế độ animation chậm hoặc thay đổi tốc độ playback, nhưng chưa xác minh được nguyên nhân này. Chưa khẳng định giống từng frame hoặc cùng thời gian tuyệt đối.
- Font iOS/Roboto và shadow/corner rendering có khác biệt nhỏ. Checkmark Android được vẽ trong khung 16dp; khoảng cách dòng quyền lợi khoảng 34–35dp như reference. Android sheet cao 89.5% màn hình: trên emulator 411×914dp, khoảng trống phía trên danh sách quyền lợi lớn hơn reference khoảng 46dp; CTA vẫn cách đáy 36dp. Đây là điểm bố cục còn khác trên màn hình cao. Shader chạy theo thời gian và phase khác nhau giữa các lần chụp; không dùng pixel diff của hai frame khác phase để kết luận shader sai.
- API 26–32 Android dùng cùng công thức qua bitmap Canvas 84×60, thấp hơn về chi tiết so với GPU AGSL; chưa chạy trên emulator API thấp. iOS yêu cầu iOS 17 trở lên.
- Đã viết hỗ trợ Reduce Motion, Dynamic Type/font scale và cuộn khi thiếu chiều cao. Chưa kiểm tra trực tiếp iOS ở cỡ chữ accessibility lớn, màn hình compact, VoiceOver hoặc Android TalkBack; chưa kiểm tra máy thật.
- `Start free trial` vẫn là demo như source gốc, chưa tích hợp StoreKit/Google Play Billing hay tạo giao dịch.

## Ảnh và video

### Sửa bóng của hai nút Swift

Đã sửa `GlossyButton.swift` cho cả `Purchase` và `Start free trial`: shadow chỉ áp lên capsule ngoài, không đổ bóng riêng lên chữ và highlight. Mặt nút/shine được clip trong capsule; viền khi nhấn đổi sang `#E1E1E7` như source React Native. Giữ nguyên thông số shadow, shine và spring của source.

Build iOS thành công và kiểm tra lại trên iPhone 18 Pro simulator: Purchase mở sheet; CTA co về 0.97 khi giữ nhấn, giảm shadow và trở lại kích thước ban đầu khi thả. Mặt nút sau sửa đo được RGB `(249,249,251)` và viền `(231,231,235)`, đúng màu source; trước sửa mặt nút bị bóng chồng làm tối xuống khoảng `(238,238,240)`. Frame accessibility của hai nút không đổi. Ảnh Yearly/Monthly bên dưới đã cập nhật sau sửa.

Ảnh đối chiếu: [Purchase trước sửa](artifacts/native-qa/button-fix/ios-home-before.png), [Purchase sau sửa](artifacts/native-qa/button-fix/ios-home-after.png), [CTA sau sửa](artifacts/native-qa/button-fix/ios-sheet-after.png), [CTA đang nhấn](artifacts/native-qa/button-fix/ios-trial-pressed.png).

Kiểm tra lại trực tiếp ngày 10/10/2026 sau yêu cầu chạy simulator: iOS đang chạy bản đã build sau thay đổi `GlossyButton.swift`. Cả hai nút có mặt `(249,249,251)` và viền `(231,231,235)`, không còn bóng chồng lên chữ/shine. Khi giữ nhấn, frame Purchase co từ chiều rộng chuẩn hóa `0.435` xuống `0.422`, CTA từ `0.841` xuống `0.816` (cùng tỉ lệ khoảng `0.97`). Thả Purchase mở lại sheet và giữ gói Monthly; thả CTA trở lại frame ban đầu và giữ màn hình demo. Ảnh mới: [Purchase](artifacts/native-qa/button-recheck/ios-home.png), [Purchase đang nhấn](artifacts/native-qa/button-recheck/ios-purchase-pressed.png), [sheet/CTA](artifacts/native-qa/button-recheck/ios-sheet.png), [CTA đang nhấn](artifacts/native-qa/button-recheck/ios-trial-pressed.png).

Android cũng đã kiểm tra lại trên API 36: APK đang cài trùng SHA256 với file build hiện tại. Purchase và CTA thu về khoảng `0.97` khi giữ nhấn, trở lại khi thả; Purchase mở sheet, CTA ở lại màn hình demo. Đóng/mở lại giữ lựa chọn Yearly; mặt nút và viền không bị bóng chồng. Ảnh mới: [Purchase Android](artifacts/native-qa/button-recheck/android-home.png), [sheet/CTA Android](artifacts/native-qa/button-recheck/android-sheet.png). Hai app vẫn để mở trên simulator/emulator; chỉ dừng các service điều khiển Argent đã dùng.

Theo yêu cầu giữ bóng như React Native, shadow và dải highlight trắng được giữ ở cả hai bản. Dải trắng là lớp highlight có sẵn trong `button-scale.tsx` (`height: 16`, trắng opacity `0.85`), không phải viền lỗi. Shadow Swift vẫn dùng màu `#7C7C7F`, opacity `0.25`, radius `12`, offset `(0,8)`; khi nhấn là `0.12`, `6`, `(0,3)`. Đối chiếu ảnh ở cùng chiều rộng cho độ sáng bóng dưới nút rất gần video RN; không tăng/giảm bóng Swift thêm.

Kotlin đã đổi mặt gradient sang nền phẳng `#F9F9FB`, đặt màu ambient/spot shadow `#7C7C7F` trên API 28+, và đổi viền sang `#E1E1E7` khi nhấn. Giữ elevation `6dp` khi nghỉ; khi nhấn dùng elevation `3dp` và outline alpha `0.12/0.25` để bóng mềm và nhẹ hơn. Android tính blur/offset từ elevation, nên đây là ánh xạ gần đúng của shadow iOS; API 26–27 giữ màu bóng hệ thống vì chưa có API đổi màu shadow. Highlight giữ nguyên theo source RN.

Sau thay đổi Kotlin này, `assembleDebug` và `lintDebug` đều thành công, đã cài lại và kiểm tra trên Android API 36: cả hai nút nhấn thu khoảng `0.97`, thả phục hồi viền/bóng; Purchase mở sheet, CTA giữ màn hình demo. Ảnh hiện tại có cả shadow và highlight: [iOS Purchase](artifacts/native-qa/button-shadow/ios-home.png), [iOS CTA](artifacts/native-qa/button-shadow/ios-sheet.png), [Android Purchase](artifacts/native-qa/button-shadow/android-home.png), [Android CTA](artifacts/native-qa/button-shadow/android-sheet.png). Các ảnh Android ở lần kiểm tra trước còn dùng gradient/bóng hệ thống; dùng nhóm `button-shadow` để xem nút hiện tại.

| Trạng thái | Reference X | iOS | Android |
| --- | --- | --- | --- |
| Yearly | [Ảnh reference](artifacts/native-qa/reference-yearly.png) | [Ảnh iOS](artifacts/native-qa/ios-yearly.png) | [Ảnh Android](artifacts/native-qa/android-yearly.png) |
| Monthly | [Ảnh reference](artifacts/native-qa/reference-monthly.png) | [Ảnh iOS](artifacts/native-qa/ios-monthly.png) | [Ảnh Android](artifacts/native-qa/android-monthly.png) |

Video lưu cục bộ: [iOS mở sheet/chuyển gói](artifacts/native-qa/ios-demo.mp4), [iOS chuyển gói/đóng/mở lại/resume](artifacts/native-qa/ios-final-demo.mp4), [Android](artifacts/native-qa/android-demo.mp4). Các video iOS được quay trước lần sửa shadow của nút; dùng PNG mới để đối chiếu diện mạo nút hiện tại. MP4 được ignore trong Git để tránh thêm file quay lớn; PNG và báo cáo có thể commit cùng source.

Android chữ lớn 1.6 lần: [phần trên và badge](artifacts/native-qa/android-large-text-top.png), [cuộn tới CTA](artifacts/native-qa/android-large-text.png). Các ảnh chữ lớn ghi lại kiểm tra layout trước lần tinh chỉnh màu backdrop cuối; cỡ chữ đã được trả về 1.0 sau khi thử.

## Build lại

```sh
(cd ios-swift && ./scripts/build.sh)
```

```sh
(cd android-kotlin && ./scripts/build.sh)
```

Ở máy kiểm tra, Xcode ban đầu thiếu Metal Toolchain. Đã cài bằng `xcodebuild -downloadComponent MetalToolchain` và build lại thành công. Hướng dẫn đầy đủ ở [README iOS](ios-swift/README.md) và [README Android](android-kotlin/README.md).
