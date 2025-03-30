# API Quản Lý Sản Phẩm

## Tổng quan
API này cung cấp các endpoint để quản lý sản phẩm trong hệ thống bookstore, bao gồm các chức năng CRUD cơ bản và các chức năng tìm kiếm nâng cao.

## Các Endpoint

### 1. Lấy danh sách sản phẩm
- **URL**: `/api/products`
- **Method**: GET
- **Mô tả**: Lấy danh sách sản phẩm với phân trang
- **Tham số**:
  - `page`: Số trang (mặc định: 0)
  - `size`: Số lượng sản phẩm mỗi trang (mặc định: 10)
- **Phân quyền**: Không yêu cầu

### 2. Lấy chi tiết sản phẩm
- **URL**: `/api/products/{id}`
- **Method**: GET
- **Mô tả**: Lấy thông tin chi tiết của một sản phẩm theo ID
- **Tham số**:
  - `id`: ID của sản phẩm
- **Phân quyền**: Không yêu cầu

### 3. Tạo sản phẩm mới
- **URL**: `/api/products`
- **Method**: POST
- **Mô tả**: Tạo một sản phẩm mới
- **Body**:
  ```json
  {
    "name": "Tên sản phẩm",
    "price": 100000,
    "stockQuantity": 10,
    "discount": 0,
    "publicationYear": 2024,
    "author": "Tác giả",
    "publisher": "Nhà xuất bản",
    "isbn": "ISBN",
    "description": "Mô tả sản phẩm",
    "imageUrls": ["url1", "url2"]
  }
  ```
- **Phân quyền**: Yêu cầu quyền ADMIN

### 4. Cập nhật sản phẩm
- **URL**: `/api/products/{id}`
- **Method**: PUT
- **Mô tả**: Cập nhật thông tin của một sản phẩm
- **Tham số**:
  - `id`: ID của sản phẩm
- **Body**: Tương tự như tạo sản phẩm
- **Phân quyền**: Yêu cầu quyền ADMIN

### 5. Xóa sản phẩm
- **URL**: `/api/products/{id}`
- **Method**: DELETE
- **Mô tả**: Xóa một sản phẩm
- **Tham số**:
  - `id`: ID của sản phẩm
- **Phân quyền**: Yêu cầu quyền ADMIN

### 6. Tìm kiếm sản phẩm

#### 6.1. Tìm kiếm theo tên
- **URL**: `/api/products/search/name`
- **Method**: GET
- **Tham số**:
  - `name`: Tên sản phẩm cần tìm
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.2. Tìm kiếm theo tác giả
- **URL**: `/api/products/search/author`
- **Method**: GET
- **Tham số**:
  - `author`: Tên tác giả
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.3. Tìm kiếm theo nhà xuất bản
- **URL**: `/api/products/search/publisher`
- **Method**: GET
- **Tham số**:
  - `publisher`: Tên nhà xuất bản
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.4. Tìm kiếm theo ISBN
- **URL**: `/api/products/search/isbn`
- **Method**: GET
- **Tham số**:
  - `isbn`: Mã ISBN

#### 6.5. Tìm kiếm theo khoảng giá
- **URL**: `/api/products/search/price-range`
- **Method**: GET
- **Tham số**:
  - `minPrice`: Giá tối thiểu
  - `maxPrice`: Giá tối đa
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.6. Tìm kiếm theo năm xuất bản
- **URL**: `/api/products/search/year`
- **Method**: GET
- **Tham số**:
  - `year`: Năm xuất bản
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.7. Tìm kiếm theo danh mục
- **URL**: `/api/products/search/category/{categoryId}`
- **Method**: GET
- **Tham số**:
  - `categoryId`: ID danh mục
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 6.8. Tìm kiếm nâng cao
- **URL**: `/api/products/search/advanced`
- **Method**: GET
- **Tham số**:
  - `name`: Tên sản phẩm
  - `author`: Tác giả
  - `publisher`: Nhà xuất bản
  - `minPrice`: Giá tối thiểu
  - `maxPrice`: Giá tối đa
  - `minRealPrice`: Giá thực tối thiểu (sau giảm giá)
  - `maxRealPrice`: Giá thực tối đa (sau giảm giá)
  - `year`: Năm xuất bản
  - `categoryId`: ID danh mục
  - `minStock`: Số lượng tồn kho tối thiểu
  - `minSold`: Số lượng đã bán tối thiểu
  - `minRating`: Đánh giá trung bình tối thiểu
  - `page`: Số trang
  - `size`: Số lượng mỗi trang
  - `sort`: Sắp xếp theo trường (ví dụ: price,desc hoặc name,asc)

### 7. Quản lý ảnh sản phẩm

#### 7.1. Lấy danh sách ảnh
- **URL**: `/api/products/{productId}/images`
- **Method**: GET
- **Tham số**:
  - `productId`: ID sản phẩm

#### 7.2. Thêm ảnh sản phẩm
- **URL**: `/api/products/{productId}/images`
- **Method**: POST
- **Tham số**:
  - `productId`: ID sản phẩm
  - `imageURL`: URL của ảnh
- **Phân quyền**: Yêu cầu quyền ADMIN

#### 7.3. Cập nhật ảnh sản phẩm
- **URL**: `/api/products/images/{imageId}`
- **Method**: PUT
- **Tham số**:
  - `imageId`: ID ảnh
  - `imageURL`: URL mới của ảnh
- **Phân quyền**: Yêu cầu quyền ADMIN

#### 7.4. Xóa ảnh sản phẩm
- **URL**: `/api/products/images/{imageId}`
- **Method**: DELETE
- **Tham số**:
  - `imageId`: ID ảnh
- **Phân quyền**: Yêu cầu quyền ADMIN

#### 7.5. Xóa tất cả ảnh sản phẩm
- **URL**: `/api/products/{productId}/images`
- **Method**: DELETE
- **Tham số**:
  - `productId`: ID sản phẩm
- **Phân quyền**: Yêu cầu quyền ADMIN

### 8. Các API đặc biệt

#### 8.1. Lấy sản phẩm giảm giá
- **URL**: `/api/products/discounted`
- **Method**: GET
- **Tham số**:
  - `minDiscount`: Giảm giá tối thiểu (%)
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 8.2. Lấy sản phẩm mới
- **URL**: `/api/products/new`
- **Method**: GET
- **Tham số**:
  - `year`: Năm xuất bản
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 8.3. Lấy sản phẩm còn hàng
- **URL**: `/api/products/in-stock`
- **Method**: GET
- **Tham số**:
  - `minStock`: Số lượng tồn kho tối thiểu
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 8.4. Lấy sản phẩm bán chạy
- **URL**: `/api/products/best-selling`
- **Method**: GET
- **Tham số**:
  - `minSold`: Số lượng đã bán tối thiểu
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 8.5. Lấy sản phẩm bán chạy nhất
- **URL**: `/api/products/top-selling`
- **Method**: GET
- **Tham số**:
  - `page`: Số trang
  - `size`: Số lượng mỗi trang

#### 8.6. Lấy sản phẩm tồn kho thấp
- **URL**: `/api/products/low-inventory`
- **Method**: GET
- **Tham số**:
  - `threshold`: Ngưỡng số lượng tồn kho (mặc định: 5)
- **Phân quyền**: Yêu cầu quyền ADMIN

## Lưu ý
1. Tất cả các API đều trả về dữ liệu dưới dạng JSON với cấu trúc:
   ```json
   {
     "success": true/false,
     "message": "Thông báo",
     "data": {dữ liệu}
   }
   ```

2. Các API yêu cầu quyền ADMIN cần gửi token xác thực trong header:
   ```
   Authorization: Bearer <token>
   ```

3. Các tham số phân trang (page, size) có thể được sử dụng ở hầu hết các API trả về danh sách.

4. API tìm kiếm nâng cao cho phép kết hợp nhiều điều kiện tìm kiếm và sắp xếp kết quả. 

# API Tìm Kiếm Nâng Cao - Bookstore

## Tổng quan
API tìm kiếm nâng cao cho phép người dùng tìm kiếm sản phẩm với nhiều tiêu chí khác nhau một cách linh hoạt. API này hỗ trợ tìm kiếm theo nhiều tham số và trả về kết quả có phân trang.

## Endpoint
```
GET /api/products/search/advanced
```

## Tham số tìm kiếm

### 1. Tham số cơ bản
- `name` (tùy chọn): Tên sản phẩm
  - Kiểu dữ liệu: String
  - Mô tả: Tìm kiếm theo tên sản phẩm, không phân biệt chữ hoa/thường
  - Ví dụ: "Harry Potter"

- `author` (tùy chọn): Tác giả
  - Kiểu dữ liệu: String
  - Mô tả: Tìm kiếm theo tên tác giả, không phân biệt chữ hoa/thường
  - Ví dụ: "J.K. Rowling"

- `publisher` (tùy chọn): Nhà xuất bản
  - Kiểu dữ liệu: String
  - Mô tả: Tìm kiếm theo tên nhà xuất bản, không phân biệt chữ hoa/thường
  - Ví dụ: "Bloomsbury"

### 2. Tham số giá
- `minPrice` (tùy chọn): Giá tối thiểu
  - Kiểu dữ liệu: Integer
  - Mô tả: Giá bán tối thiểu của sản phẩm
  - Ví dụ: 100000

- `maxPrice` (tùy chọn): Giá tối đa
  - Kiểu dữ liệu: Integer
  - Mô tả: Giá bán tối đa của sản phẩm
  - Ví dụ: 500000

- `minRealPrice` (tùy chọn): Giá thực tế tối thiểu
  - Kiểu dữ liệu: Integer
  - Mô tả: Giá thực tế (sau giảm giá) tối thiểu của sản phẩm
  - Ví dụ: 80000

- `maxRealPrice` (tùy chọn): Giá thực tế tối đa
  - Kiểu dữ liệu: Integer
  - Mô tả: Giá thực tế (sau giảm giá) tối đa của sản phẩm
  - Ví dụ: 400000

### 3. Tham số khác
- `year` (tùy chọn): Năm xuất bản
  - Kiểu dữ liệu: Integer
  - Mô tả: Năm xuất bản của sản phẩm
  - Ví dụ: 2023

- `categoryId` (tùy chọn): ID danh mục
  - Kiểu dữ liệu: Long
  - Mô tả: ID của danh mục sản phẩm
  - Ví dụ: 1

- `minStock` (tùy chọn): Số lượng tồn kho tối thiểu
  - Kiểu dữ liệu: Integer
  - Mô tả: Số lượng sản phẩm còn trong kho tối thiểu
  - Ví dụ: 5

- `minSold` (tùy chọn): Số lượng đã bán tối thiểu
  - Kiểu dữ liệu: Integer
  - Mô tả: Số lượng sản phẩm đã bán tối thiểu
  - Ví dụ: 100

- `minRating` (tùy chọn): Đánh giá tối thiểu
  - Kiểu dữ liệu: Double
  - Mô tả: Điểm đánh giá trung bình tối thiểu của sản phẩm
  - Ví dụ: 4.5

### 4. Tham số phân trang
- `page` (tùy chọn): Số trang
  - Kiểu dữ liệu: Integer
  - Mặc định: 0
  - Mô tả: Số trang cần lấy

- `size` (tùy chọn): Số lượng mỗi trang
  - Kiểu dữ liệu: Integer
  - Mặc định: 10
  - Mô tả: Số lượng sản phẩm trên mỗi trang

## Ví dụ sử dụng

### Ví dụ 1: Tìm kiếm sách Harry Potter của J.K. Rowling
```
GET /api/products/search/advanced?name=Harry Potter&author=J.K. Rowling
```

### Ví dụ 2: Tìm kiếm sách có giá từ 100.000đ đến 500.000đ
```
GET /api/products/search/advanced?minPrice=100000&maxPrice=500000
```

### Ví dụ 3: Tìm kiếm sách bán chạy trong danh mục Văn học
```
GET /api/products/search/advanced?categoryId=1&minSold=100
```

### Ví dụ 4: Tìm kiếm sách mới xuất bản có đánh giá cao
```
GET /api/products/search/advanced?year=2024&minRating=4.5
```

## Kết quả trả về
API trả về kết quả dưới dạng JSON với cấu trúc:
```json
{
  "content": [
    {
      "productId": 1,
      "name": "Tên sản phẩm",
      "author": "Tác giả",
      "publisher": "Nhà xuất bản",
      "price": 100000,
      "realPrice": 80000,
      "stockQuantity": 10,
      "soldCount": 100,
      "rating": 4.5,
      "publicationYear": 2024,
      "categories": [...],
      "imageUrls": [...]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "empty": false,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0
}
```

## Lưu ý
1. Tất cả các tham số tìm kiếm đều là tùy chọn, có thể kết hợp nhiều tham số để tìm kiếm chính xác hơn.
2. Kết quả tìm kiếm được phân trang để tối ưu hiệu suất.
3. API chỉ trả về các sản phẩm đang hoạt động (isActive = true).
4. Tìm kiếm theo tên, tác giả và nhà xuất bản không phân biệt chữ hoa/thường.
5. Có thể sử dụng kết hợp với các API tìm kiếm khác để có kết quả chính xác hơn. 