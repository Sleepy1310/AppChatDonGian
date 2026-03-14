# App Chat Đơn Giản - Connect.

Ứng dụng nhắn tin thời gian thực được phát triển bằng Java trên nền tảng Android, sử dụng Firebase (Firestore, Auth, Messaging).

## Các tính năng chính
- Đăng nhập bằng số điện thoại (Firebase Phone Auth).
- Tìm kiếm người dùng và nhóm.
- Nhắn tin 1-1 thời gian thực.
- Thông báo đẩy (FCM) khi có tin nhắn mới.
- Quản lý hồ sơ cá nhân.

## Hướng dẫn đăng nhập (Dành cho kiểm thử)
Để thuận tiện cho việc chấm bài mà không cần chờ tin nhắn OTP thật, tôi đã cấu hình tài khoản kiểm thử sau trên Firebase Console:
- **Số điện thoại:** `+84 123456789`
- **Mã OTP cố định:** `123456`

## Cài đặt và sử dụng
1. Tải file APK từ thư mục `app/build/outputs/apk/debug/app-debug.apk` hoặc từ mục Releases.
2. Cài đặt trên điện thoại Android (Cho phép cài đặt từ nguồn không xác định).
3. Đăng nhập bằng số điện thoại test ở trên.

## Công nghệ sử dụng
- **Android SDK:** Java
- **Database:** Firebase Cloud Firestore
- **Authentication:** Firebase Auth (Phone)
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Architecture:** Android UI Components, Material Design 3
