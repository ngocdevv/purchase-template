# Purchase — Swift / SwiftUI

Ứng dụng iOS native độc lập, chuyển từ `src/screens/SubScreen.tsx` và các component trong `src/components/ui`. Không cần React Native, Metro, CocoaPods hay package bên ngoài.

Yêu cầu: macOS, Xcode 15 trở lên, iOS 17 trở lên. Shader dùng SwiftUI `colorEffect` và Metal stitchable functions, nên deployment target là iOS 17. Project chỉ hỗ trợ iPhone dọc để giữ bố cục của video.

## Chạy

Mở `PurchaseTemplate.xcodeproj`, chọn scheme **PurchaseTemplate**, chọn iPhone simulator và Run. Để chạy trên iPhone thật, chọn signing team trong Xcode.

Build simulator từ terminal:

```sh
./scripts/build.sh
# Hoặc chỉ định simulator đã có:
./scripts/build.sh 'platform=iOS Simulator,id=YOUR_SIMULATOR_UDID'
```

Bundle simulator ở `build/Build/Products/Debug-iphonesimulator/PurchaseTemplate.app`. Bundle ID: `com.ngocdevv.purchase.swift`.

Ở lần build đầu, Xcode có thể chưa cài Metal Toolchain (đã gặp trên Xcode 27.1 của môi trường này). Nếu báo `cannot execute tool 'metal' due to missing Metal Toolchain`, cài component chính thức rồi build lại:

```sh
xcodebuild -downloadComponent MetalToolchain
```

## Đối chiếu video

Reference: https://x.com/ngocdevv/status/1982074052136337769

- Màn hình sáng, nút Purchase ở giữa, nhấn mở bottom sheet trắng bo góc 42 và backdrop đen opacity 0.5.
- Tiêu đề `One price.One plan`, phần `One plan` màu `#C51A3C`; subtitle, năm quyền lợi và nút `Start free trial` giữ nguyên nội dung.
- Yearly hiển thị `$25` và màu `[0.9, 0.6, 0.7]`; Monthly hiển thị `$18` và màu `[0.1, 0.2, 0.4]`.
- Card cao 240, margin ngang 32, radius 28; shader Metal giữ nguyên toàn bộ phép tính SkSL và chia tọa độ local cho kích thước **màn hình**.
- Spring entrance dùng mass 1, stiffness 10, damping 10, các offset 120/140/160/180; zoom card mass 0.7. Đường cong được ánh xạ sang animation native; tốc độ bản iOS còn khác clip X, xem chi tiết trong báo cáo đối chiếu.
- Highlight segmented control trượt và thay đổi chiều rộng trong 1 giây; chữ số flip 350ms, stagger 40ms. Tap cùng lựa chọn vẫn flip chữ số, giống callback gốc.
- Lựa chọn giữ nguyên khi đóng/mở sheet; đóng bằng backdrop, handle hoặc kéo handle xuống.
- `Start free trial` chỉ có hiệu ứng nhấn như source; chưa tích hợp StoreKit hay thanh toán.

Font và bố cục theo Dynamic Type; trên màn hình thấp hoặc cỡ chữ lớn phần nội dung cuộn, CTA giữ ở dưới. VoiceOver có nhãn, trạng thái Selected, giá và thao tác Escape. Reduce Motion giữ shader tĩnh và bỏ flip/translate/zoom. Timeline shader dừng khi ứng dụng background và được tháo khi sheet đóng.

## Kiểm tra thủ công

Đã build thành công bằng Xcode 27.1 và chạy trực tiếp trên iPhone 18 Pro simulator, iOS 27.2. Đã kiểm tra hai gói, đổi gói nhanh, CTA demo, backdrop/kéo handle, mở lại giữ Monthly và trở lại từ background. Ảnh/video và các khác biệt về tốc độ animation so với X nằm trong [báo cáo đối chiếu](../NATIVE_VALIDATION.md). Chưa chạy trực tiếp các mục chữ lớn/VoiceOver/Reduce Motion bên dưới.

1. Mở app: Purchase nằm giữa. Nhấn Purchase; đợi entrance hoàn tất.
2. Kiểm tra Yearly, Save 25%, card rose `$25`, năm quyền lợi, CTA.
3. Chuyển Monthly: highlight trượt sang ô nhỏ hơn, card navy `$18`; shader tiếp tục cùng clock.
4. Chuyển Yearly và nhấn CTA: CTA co nhẹ, không mở trang thanh toán.
5. Chọn Monthly; kéo handle đóng; mở lại: vẫn Monthly và `$18`.
6. Tap backdrop hoặc handle đóng. Với VoiceOver dùng Escape.
7. Với simulator nhỏ hoặc chữ lớn, cuộn nội dung và kiểm tra CTA vẫn chạm được; bật Reduce Motion kiểm tra shader đứng yên.

Đường dẫn source: `PurchaseScreen.swift` quản lý modal/lựa chọn; `Components/` chứa view native; `Shaders/Fabric.metal` là shader.
