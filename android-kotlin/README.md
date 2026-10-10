# Purchase — Kotlin Android

Bản native Kotlin dùng Android Views, không phụ thuộc React Native/Expo. Mở thư mục này bằng Android Studio hoặc dùng Gradle wrapper được commit sẵn.

## Build

Yêu cầu JDK 17+, Android SDK Platform 36 và Build Tools 36. Đặt `ANDROID_HOME` trỏ tới SDK hoặc tạo `local.properties` với `sdk.dir=/đường/dẫn/sdk` (file này không được commit).

```sh
./scripts/build.sh
# Hoặc:
./gradlew :app:assembleDebug :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Package: `com.ngocdevv.purchase`. Gradle 8.14.3 (kiểm tra SHA-256), Android Gradle Plugin 8.13.2, Kotlin 2.2.21. Min SDK 26; compile/target SDK 36. Không cần backend, Metro, npm hay quyền mạng.

## Đối chiếu source và video

Video tham chiếu: https://x.com/ngocdevv/status/1982074052136337769. Bản này giữ màn hình mở đầu trắng của video, nút Purchase, sheet cao 89.5% màn hình với góc 42dp và backdrop tối 50%; header `One price.One plan`, subtitle, hai lựa chọn với badge `Save 25%`, card 240dp/góc 28dp/lề 32dp, năm quyền lợi và nút bóng `Start free trial`.

- Yearly: `$25`, RGB `[0.9, 0.6, 0.7]`; Monthly: `$18`, RGB `[0.1, 0.2, 0.4]`.
- Android 13/API 33 trở lên chạy AGSL RuntimeShader với nguyên các biểu thức shader Skia gốc. Tọa độ local của card chia cho **kích thước cả màn hình**, amplitude 0.5 và speed 1; đổi gói không reset đồng hồ.
- API 26–32 dùng cùng các phương trình qua bitmap Canvas lấy mẫu 84×60 ở tối đa 30fps. Đây là fallback thấp hơn về độ chi tiết; chưa kiểm tra trên emulator API thấp.
- Spring xuất hiện giữ damping 10/stiffness 10: layer chung, header, subtitle120dp, segment140dp, card180dp, benefits160dp, CTA180dp; zoom card mass0.7 và fade vải mass3. Digit flip có hai mặt của chữ số mới, trễ 40ms mỗi chữ số, 350ms; đường cong native xấp xỉ spring duration của Reanimated.
- Đóng bằng backdrop, vuốt xuống, handle hoặc Android Back. Lựa chọn được giữ khi mở lại và qua Activity recreation. Choreographer dừng khi đóng sheet hoặc app đi background, tiếp tục khi resume.
- Nội dung cuộn trên màn hình thấp/ngang; kích thước segment, chữ số và nút thích ứng font scale. View controls có label/accessibility role/checked state; chế độ tắt animation hiển thị ngay và dừng chuyển động shader.

`Start free trial` là **demo hiệu ứng nhấn**. Source gốc không gắn `onPress` cho CTA; chưa có Google Play Billing, giao dịch, tạo tài khoản hoặc trial thực.

## Kiểm tra

`./scripts/build.sh --offline` đã PASS cả `assembleDebug` và `lintDebug` với JDK 17. QA trực tiếp bằng Argent trên `emulator-5554`, AVD `OTPInputPerfAPI36`, Android API 36 (1080×2400, density420):

- PASS: mở Purchase, spring/fabric chạy, Yearly `$25` hồng → Monthly `$18` xanh → Yearly; badge/highlight đổi theo lựa chọn; bấm luân phiên nhanh không sai giá.
- PASS: CTA chỉ tạo hiệu ứng nhấn; đóng bằng backdrop/vuốt handle/Android Back; mở lại giữ Monthly; đưa app về Home rồi resume giữ lựa chọn và chạy tiếp shader. Logcat theo PID app không có lỗi `AndroidRuntime` trong lần kiểm tra.
- PASS font scale **1.6**: badge Yearly không cắt/wrap, giá hiển thị và nút CTA tới được bằng cuộn. Đã khôi phục font scale gốc **1.0**. Ảnh minh chứng: [chữ lớn](../artifacts/native-qa/android-large-text-top.png), [CTA sau cuộn](../artifacts/native-qa/android-large-text.png).

Ảnh: [home](../artifacts/native-qa/android-home.png), [Yearly](../artifacts/native-qa/android-yearly.png), [Monthly](../artifacts/native-qa/android-monthly.png). Nút sau lần chỉnh shadow cuối: [Purchase](../artifacts/native-qa/button-shadow/android-home.png), [CTA](../artifacts/native-qa/button-shadow/android-sheet.png). [Video demo Android](../artifacts/native-qa/android-demo.mp4) lưu cục bộ và không đưa vào Git. Shader thay đổi liên tục nên đối chiếu cấu trúc/màu/hiệu ứng trực tiếp với video, không dùng pixel diff để khẳng định hai ảnh ở thời điểm khác nhau giống tuyệt đối.

Các khác biệt nền tảng còn lại: Android dùng Roboto thay cho font iOS, status/navigation bar hệ thống khác và spring/digit animation do Android render. Không thể khẳng định pixel-identical cho shader đang chạy theo thời gian trên hai nền tảng.
