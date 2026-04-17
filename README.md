<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: none;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<h1 align="center"><b>PHÁT TRIỂN ỨNG DỤNG TRÊN THIẾT BỊ DI ĐỘNG - NT118.Q21</b></h1>

## GIỚI THIỆU MÔN
-    **Tên môn học:** Phát triển ứng dụng trên thiết bị di động
-    **Mã môn học:** NT118
-    **Mã lớp:** NT118.Q21
-    **Năm học:** Học Kỳ 2 (2025 - 2026)
-    **Giảng viên:** Phan Xuân Thiện
-    **Email Giảng viên:** thienpx@uit.edu.vn

## THÔNG TIN NHÓM 
| STT | MSSV     | Họ và Tên         | GitHub                           | Email                  |
| :-- | :------- | :---------------- | :------------------------------- | :--------------------- |
| 1   | 22521169 | Nguyễn Võ Đăng Phương    |       | 22521169@gm.uit.edu.vn |
| 2   | 23520230 | Nguyễn Lê Bảo Đăng   | https://github.com/nguyenlebaodang      | 23520230@gm.uit.edu.vn |
| 3   | 23520924 | Hồ Nhật Minh    | https://github.com/min-hit-1701      | 23520924@gm.uit.edu.vn |
| 4   | 23521770 | Đỗ Ngọc Tường Vân    |https://github.com/23521770       | 23521770@gm.uit.edu.vn |
| 5   | 23521812 |Nguyễn Trường Vũ    | https://github.com/vu-leonguyen      | 23521812@gm.uit.edu.vn |

## GIỚI THIỆU ĐỒ ÁN

**Finance Tracker** là ứng dụng quản lý tài chính dành cho cá nhân và doanh nghiệp, được phát triển dựa trên thiết kế UI/UX từ Figma.

Ứng dụng hỗ trợ:
- Theo dõi thu nhập và chi tiêu
- Quản lý ví và danh mục
- Lập kế hoạch ngân sách
- Thống kê và báo cáo tài chính

## CƠ SỞ THIẾT KẾ GIAO DIỆN

Giao diện của ứng dụng được thiết kế dựa trên bộ UI Kit từ Figma tại liên kết sau:

https://www.figma.com/design/DlLLfH3PNaHJ9Q7lrvNjZX/Coinpay-Fintech-Finance-Mobile-App-UI-kit

Trong quá trình phát triển, nhóm tuân thủ các nguyên tắc:
- Đảm bảo tính nhất quán về màu sắc thông qua tệp colors.xml
- Tuân thủ bố cục (layout), khoảng cách (spacing) và kích thước theo thiết kế
- Không tự ý thay đổi cấu trúc giao diện đã được định nghĩa

---

## KIẾN TRÚC HỆ THỐNG

Ứng dụng được xây dựng theo mô hình:

- **Single Activity** kết hợp với **Multiple Fragment**
- Điều hướng chính sử dụng Bottom Navigation

Luồng hoạt động tổng quát của hệ thống:

Splash Screen → Đăng nhập/Đăng ký → Chọn chế độ → Màn hình chính → Các Fragment chức năng

Kiến trúc này giúp:
- Tách biệt rõ ràng các chức năng
- Dễ dàng mở rộng và bảo trì hệ thống
- Hỗ trợ phát triển song song giữa các thành viên

## CẤU TRÚC ĐỒ ÁN

Đồ án được tổ chức theo cấu trúc phân lớp như sau:

- **activity:** Chứa các Activity chính của hệ thống
- **fragment.personal:** Chứa các Fragment cho chế độ cá nhân
- **fragment.business:** Chứa các Fragment cho chế độ doanh nghiệp
- **adapter:** Xử lý hiển thị danh sách
- **model:** Định nghĩa cấu trúc dữ liệu

Cấu trúc này đảm bảo tính mô-đun hóa và dễ quản lý mã nguồn.


