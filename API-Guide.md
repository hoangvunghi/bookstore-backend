# API Documentation - Bookstore Application

## Mục lục
1. [Authentication API](#authentication-api)
2. [User API](#user-api)
3. [Product API](#product-api)
4. [Category API](#category-api)
5. [Cart API](#cart-api)
6. [Order API](#order-api)
7. [Review API](#review-api)
8. [Password Reset API](#password-reset-api)

## Authentication API
Base URL: `/api/auth`

### Đăng nhập
- **Endpoint**: `POST /login`
- **Mô tả**: Đăng nhập vào hệ thống
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response**: JWT Token

### Đăng ký
- **Endpoint**: `POST /register`
- **Mô tả**: Đăng ký tài khoản mới
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
    "email": "string",
    "fullName": "string"
  }
  ```
- **Response**: Thông báo đăng ký thành công

## User API
Base URL: `/api/users`

### Xem thông tin cá nhân
- **Endpoint**: `GET /profile`
- **Mô tả**: Lấy thông tin cá nhân của người dùng đang đăng nhập
- **Authentication**: Required
- **Response**: Thông tin profile người dùng

## Product API
Base URL: `/api/products`

### Danh sách sản phẩm
- **Endpoint**: `GET /`
- **Mô tả**: Lấy danh sách tất cả sản phẩm có phân trang
- **Parameters**:
  - `page`: Số trang (mặc định = 0)
  - `size`: Số sản phẩm mỗi trang (mặc định = 10)

### Chi tiết sản phẩm
- **Endpoint**: `GET /{id}`
- **Mô tả**: Lấy thông tin chi tiết của một sản phẩm

### Tạo sản phẩm mới
- **Endpoint**: `POST /`
- **Mô tả**: Tạo sản phẩm mới (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Request Body**: Thông tin sản phẩm

### Cập nhật sản phẩm
- **Endpoint**: `PUT /{id}`
- **Mô tả**: Cập nhật thông tin sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Request Body**: Thông tin cập nhật

### Xóa sản phẩm
- **Endpoint**: `DELETE /{id}`
- **Mô tả**: Xóa sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)

### Tìm kiếm sản phẩm
- **Endpoint**: `GET /search/name`
- **Mô tả**: Tìm kiếm theo tên
- **Parameters**: `name`

- **Endpoint**: `GET /search/author`
- **Mô tả**: Tìm kiếm theo tác giả
- **Parameters**: `author`

- **Endpoint**: `GET /search/publisher`
- **Mô tả**: Tìm kiếm theo nhà xuất bản
- **Parameters**: `publisher`

- **Endpoint**: `GET /search/isbn`
- **Mô tả**: Tìm kiếm theo ISBN
- **Parameters**: `isbn`

- **Endpoint**: `GET /search/price-range`
- **Mô tả**: Tìm kiếm theo khoảng giá
- **Parameters**: 
  - `minPrice`
  - `maxPrice`

- **Endpoint**: `GET /search/year`
- **Mô tả**: Tìm kiếm theo năm xuất bản
- **Parameters**: `year`

- **Endpoint**: `GET /search/category/{categoryId}`
- **Mô tả**: Tìm kiếm theo danh mục

### Tìm kiếm nâng cao
- **Endpoint**: `GET /search/advanced`
- **Mô tả**: Tìm kiếm với nhiều tiêu chí
- **Parameters**:
  - `name` (optional)
  - `author` (optional)
  - `publisher` (optional)
  - `minPrice` (optional)
  - `maxPrice` (optional)
  - `year` (optional)
  - `categoryId` (optional)

### Sản phẩm đặc biệt
- **Endpoint**: `GET /discounted`
- **Mô tả**: Lấy danh sách sản phẩm đang giảm giá
- **Parameters**: `minDiscount` (mặc định = 0)

- **Endpoint**: `GET /new`
- **Mô tả**: Lấy danh sách sản phẩm mới
- **Parameters**: `year`

- **Endpoint**: `GET /in-stock`
- **Mô tả**: Lấy danh sách sản phẩm còn hàng
- **Parameters**: `minStock` (mặc định = 0)

### Quản lý ảnh sản phẩm
- **Endpoint**: `GET /{productId}/images`
- **Mô tả**: Lấy danh sách ảnh của sản phẩm

- **Endpoint**: `POST /{productId}/images`
- **Mô tả**: Thêm ảnh cho sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Parameters**: `imageURL`

- **Endpoint**: `PUT /images/{imageId}`
- **Mô tả**: Cập nhật ảnh sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Parameters**: `imageURL`

- **Endpoint**: `DELETE /images/{imageId}`
- **Mô tả**: Xóa ảnh sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)

- **Endpoint**: `DELETE /{productId}/images`
- **Mô tả**: Xóa tất cả ảnh của sản phẩm (chỉ ADMIN)
- **Authentication**: Required (ADMIN)

## Category API
Base URL: `/api/categories`

### Danh sách danh mục
- **Endpoint**: `GET /`
- **Mô tả**: Lấy danh sách danh mục có phân trang

- **Endpoint**: `GET /all`
- **Mô tả**: Lấy tất cả danh mục không phân trang

### Chi tiết danh mục
- **Endpoint**: `GET /{id}`
- **Mô tả**: Lấy thông tin chi tiết của danh mục

### Quản lý danh mục (ADMIN)
- **Endpoint**: `POST /`
- **Mô tả**: Tạo danh mục mới
- **Authentication**: Required (ADMIN)

- **Endpoint**: `PUT /{id}`
- **Mô tả**: Cập nhật danh mục
- **Authentication**: Required (ADMIN)

- **Endpoint**: `DELETE /{id}`
- **Mô tả**: Xóa danh mục
- **Authentication**: Required (ADMIN)

## Cart API
Base URL: `/api/cart`

### Xem giỏ hàng
- **Endpoint**: `GET /`
- **Mô tả**: Lấy thông tin giỏ hàng của người dùng hiện tại
- **Authentication**: Required

### Quản lý giỏ hàng
- **Endpoint**: `POST /items/{productId}`
- **Mô tả**: Thêm sản phẩm vào giỏ hàng
- **Authentication**: Required
- **Parameters**: `quantity` (mặc định = 1)

- **Endpoint**: `PUT /items/{productId}`
- **Mô tả**: Cập nhật số lượng sản phẩm trong giỏ hàng
- **Authentication**: Required
- **Parameters**: `quantity`

- **Endpoint**: `DELETE /items/{productId}`
- **Mô tả**: Xóa sản phẩm khỏi giỏ hàng
- **Authentication**: Required

- **Endpoint**: `DELETE /`
- **Mô tả**: Xóa toàn bộ giỏ hàng
- **Authentication**: Required

## Order API
Base URL: `/api/orders`

### Xem đơn hàng
- **Endpoint**: `GET /my-orders`
- **Mô tả**: Lấy danh sách đơn hàng của người dùng hiện tại
- **Authentication**: Required

- **Endpoint**: `GET /{orderId}`
- **Mô tả**: Lấy chi tiết đơn hàng
- **Authentication**: Required

### Quản lý đơn hàng
- **Endpoint**: `POST /`
- **Mô tả**: Tạo đơn hàng mới
- **Authentication**: Required
- **Request Body**: Danh sách sản phẩm

- **Endpoint**: `PUT /{orderId}/status`
- **Mô tả**: Cập nhật trạng thái đơn hàng (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Parameters**: `status`

- **Endpoint**: `PUT /{orderId}/cancel`
- **Mô tả**: Hủy đơn hàng
- **Authentication**: Required

- **Endpoint**: `GET /by-status`
- **Mô tả**: Lấy danh sách đơn hàng theo trạng thái (chỉ ADMIN)
- **Authentication**: Required (ADMIN)
- **Parameters**: `status`

## Review API
Base URL: `/api/reviews`

### Đánh giá sản phẩm
- **Endpoint**: `POST /orders/{orderId}/products/{productId}`
- **Mô tả**: Tạo đánh giá cho sản phẩm (chỉ cho phép khi đơn hàng đã hoàn thành và chỉ đánh giá 1 lần)
- **Authentication**: Required
- **Parameters**:
  - `rating`: Số sao đánh giá (1-5)
  - `comment`: Nội dung đánh giá

## Password Reset API
Base URL: `/api/password`

### Quên mật khẩu
- **Endpoint**: `POST /forgot`
- **Mô tả**: Yêu cầu đặt lại mật khẩu
- **Request Body**:
  ```json
  {
    "email": "string"
  }
  ```

### Đặt lại mật khẩu
- **Endpoint**: `POST /reset`
- **Mô tả**: Đặt lại mật khẩu với token
- **Request Body**:
  ```json
  {
    "token": "string",
    "newPassword": "string"
  }
  ``` 