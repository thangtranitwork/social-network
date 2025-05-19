# ỨNG DỤNG MÔ HÌNH ĐỒ THỊ TRONG XÂY DỰNG WEBSITE MẠNG XÃ HỘI

![Mạng xã hội](https://via.placeholder.com/800x400?text=M%E1%BA%A1ng+X%C3%A3+H%E1%BB%99i+Vi%E1%BB%87t+Nam)

## Giới thiệu

Đây là ứng dụng mạng xã hội chạy trên nền tảng web, với mục đích tạo ra một không gian trực tuyến giúp người dùng kết nối, chia sẻ và tương tác với nhau dễ dàng. Hệ thống hỗ trợ các chức năng như đăng bài viết, bình luận, kết bạn, nhắn tin và thông báo theo thời gian thực. Mục tiêu của ứng dụng là xây dựng một nền tảng thân thiện, bảo mật và mở rộng dễ dàng để phục vụ nhu cầu kết nối cộng đồng, đặc biệt hướng đến người dùng Việt Nam.

## Công nghệ sử dụng

### Backend
- **Spring Boot**: Framework Java để xây dựng ứng dụng web nhanh chóng, dễ cấu hình và dễ triển khai
- **Spring Security**: Module bảo mật trong hệ sinh thái Spring, xử lý xác thực và phân quyền người dùng
- **Neo4j**: Cơ sở dữ liệu đồ thị phù hợp cho việc lưu trữ và truy vấn dữ liệu theo quan hệ giữa các đối tượng
- **Spring Data Neo4j**: Hỗ trợ tích hợp Neo4j vào ứng dụng Spring Boot
- **WebSocket**: Giao thức truyền thông hai chiều, liên tục giữa client và server cho các tính năng thời gian thực

### Công cụ phát triển
- **IntelliJ IDEA**: IDE mạnh mẽ cho phát triển Java và Spring Boot
- **Docker**: Nền tảng ảo hóa giúp đóng gói và triển khai ứng dụng dễ dàng
- **Git**: Hệ thống quản lý mã nguồn phân tán

## Chức năng chính

- **Xác thực**: Đăng ký, đăng nhập, quản lý tài khoản
- **Quản lý bài viết**: Đăng, chỉnh sửa, xóa bài viết
- **Bình luận**: Tương tác với bài viết qua bình luận
- **Trang cá nhân**: Xem và quản lý thông tin cá nhân
- **Nhắn tin**: Giao tiếp trực tiếp giữa người dùng theo thời gian thực
- **Thông báo**: Hệ thống thông báo theo thời gian thực
- **Tìm kiếm**: Tìm người dùng, bài viết và nội dung
- **Báo cáo**: Báo cáo nội dung không phù hợp
- **Quản lý người dùng**: Chức năng dành cho quản trị viên
- **Quản lý mối quan hệ**: Kết bạn, hủy kết bạn, chặn người dùng

## Những thách thức của dự án

- **Hiệu năng và khả năng mở rộng**: Đảm bảo hệ thống phục vụ lượng lớn người dùng đồng thời
- **Bảo mật và quyền riêng tư**: Bảo vệ dữ liệu người dùng và chống các cuộc tấn công phổ biến
- **Quản lý nội dung và kiểm duyệt**: Triển khai cơ chế báo cáo và xử lý nội dung không phù hợp
- **Tính năng thời gian thực**: Đảm bảo nhắn tin, thông báo hoạt động theo thời gian thực
- **Trải nghiệm người dùng**: Giao diện trực quan, thân thiện và tương thích đa thiết bị
- **Quản lý quan hệ người dùng**: Hỗ trợ các mối quan hệ phức tạp giữa người dùng
- **Quản trị hệ thống và phân quyền**: Phân định rõ vai trò và quyền hạn của quản trị viên

## Kết quả mong đợi

- Hệ thống mạng xã hội hoạt động ổn định với đầy đủ chức năng
- Giao diện người dùng thân thiện và responsive
- Các tính năng thời gian thực hoạt động mượt mà
- Hệ thống quản trị đầy đủ và linh hoạt
- Đảm bảo tính bảo mật và quyền riêng tư
- Khả năng mở rộng và bảo trì dễ dàng

## Cài đặt và chạy dự án

### Yêu cầu hệ thống
- Java 17+
- Docker và Docker Compose
- Neo4j (có thể sử dụng qua Docker)

### Cài đặt

1. Clone repository:
```bash
git clone https://github.com/your-username/social-network.git
cd social-network
```

2. Cài đặt dependencies cho backend:
```bash
cd backend
./mvnw install
```

3. Chạy docker compose:
```bash
docker-compose up -d
```

### Chạy ứng dụng
1. Cấu hình file env cho backend
2. Chạy backend:
```bash
cd backend
./mvnw spring-boot:run
```

## Cấu trúc dự án

```
social-network-api/
├── backend/                 # Mã nguồn backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/        # Mã nguồn Java
│   │   │   └── resources/   # Cấu hình và tài nguyên
│   │   └── test/            # Mã nguồn kiểm thử
│   └── pom.xml              # Cấu hình Maven
├── docker-compose.yml       # Cấu hình Docker Compose
└── README.md                # Tài liệu dự án
```

## Liên hệ

- Email: [thangtran.it.work@gmail.com](mailto:thangtran.it.work@gmail.com)
