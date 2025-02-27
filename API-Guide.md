# Hướng dẫn sử dụng API Bookstore

## Mục lục
- [1. API Sản phẩm (Product)](#1-api-sản-phẩm-product)
- [2. API Danh mục (Category)](#2-api-danh-mục-category)
- [3. API Phân loại sản phẩm (Product Category)](#3-api-phân-loại-sản-phẩm-product-category)

## 1. API Sản phẩm (Product)

### 1.1. API Cơ bản

#### 1.1.1. Lấy danh sách sản phẩm
- **URL**: `/api/products`
- **Phương thức**: GET
- **Tham số**:
  - `page`: Số trang (mặc định = 0)
  - `size`: Số sản phẩm mỗi trang (mặc định = 10)
  - `sort`: Sắp xếp (VD: `name,desc` hoặc `price,asc`)
- **Ví dụ**: `/api/products?page=0&size=10&sort=name,desc`

#### 1.1.2. Lấy thông tin sản phẩm theo ID
- **URL**: `/api/products/{id}`
- **Phương thức**: GET
- **Ví dụ**: `/api/products/1`

#### 1.1.3. Tạo sản phẩm mới
- **URL**: `/api/products`
- **Phương thức**: POST
- **Body mẫu**:
```json
{
    "name": "Clean Code",
    "description": "Sách về lập trình sạch",
    "price": 450000,
    "stockQuantity": 50,
    "discount": 10,
    "author": "Robert C. Martin",
    "publisher": "Prentice Hall",
    "publicationYear": 2008,
    "pageCount": 464,
    "ISBN": "9780132350884",
    "categoryIds": [1, 2],
    "imageUrls": ["url1", "url2"]
}
```

#### 1.1.4. Cập nhật sản phẩm
- **URL**: `/api/products/{id}`
- **Phương thức**: PUT
- **Body**: Tương tự như tạo mới

#### 1.1.5. Xóa sản phẩm
- **URL**: `/api/products/{id}`
- **Phương thức**: DELETE

### 1.2. API Tìm kiếm

#### 1.2.1. Tìm theo tên sách
- **URL**: `/api/products/search/name`
- **Phương thức**: GET
- **Tham số**: 
  - `name`: Tên sách cần tìm
  - `page`, `size`: Phân trang
- **Ví dụ**: `/api/products/search/name?name=Clean Code&page=0&size=10`

#### 1.2.2. Tìm theo tác giả
- **URL**: `/api/products/search/author`
- **Phương thức**: GET
- **Tham số**: 
  - `author`: Tên tác giả
- **Ví dụ**: `/api/products/search/author?author=Robert Martin`

#### 1.2.3. Tìm theo nhà xuất bản
- **URL**: `/api/products/search/publisher`
- **Phương thức**: GET
- **Tham số**: 
  - `publisher`: Tên nhà xuất bản
- **Ví dụ**: `/api/products/search/publisher?publisher=Prentice Hall`

#### 1.2.4. Tìm theo ISBN
- **URL**: `/api/products/search/isbn`
- **Phương thức**: GET
- **Tham số**: 
  - `isbn`: Mã ISBN
- **Ví dụ**: `/api/products/search/isbn?isbn=9780132350884`

#### 1.2.5. Tìm theo khoảng giá
- **URL**: `/api/products/search/price-range`
- **Phương thức**: GET
- **Tham số**: 
  - `minPrice`: Giá tối thiểu
  - `maxPrice`: Giá tối đa
- **Ví dụ**: `/api/products/search/price-range?minPrice=100000&maxPrice=500000`

#### 1.2.6. Tìm theo năm xuất bản
- **URL**: `/api/products/search/year`
- **Phương thức**: GET
- **Tham số**: 
  - `year`: Năm xuất bản
- **Ví dụ**: `/api/products/search/year?year=2023`

#### 1.2.7. Tìm theo danh mục
- **URL**: `/api/products/search/category/{categoryId}`
- **Phương thức**: GET
- **Ví dụ**: `/api/products/search/category/1`

### 1.3. Tìm kiếm nâng cao

#### 1.3.1. Tìm kiếm kết hợp nhiều tiêu chí
- **URL**: `/api/products/search/advanced`
- **Phương thức**: GET
- **Tham số**: (tất cả đều không bắt buộc)
  - `name`: Tên sách
  - `author`: Tác giả
  - `publisher`: Nhà xuất bản
  - `minPrice`: Giá tối thiểu
  - `maxPrice`: Giá tối đa
  - `year`: Năm xuất bản
  - `categoryId`: ID danh mục
- **Ví dụ**: `/api/products/search/advanced?name=Java&minPrice=100000&maxPrice=500000&year=2023`

### 1.4. API Đặc biệt

#### 1.4.1. Lấy sản phẩm đang giảm giá
- **URL**: `/api/products/discounted`
- **Phương thức**: GET
- **Tham số**: 
  - `minDiscount`: Mức giảm giá tối thiểu (mặc định = 0)
- **Ví dụ**: `/api/products/discounted?minDiscount=10`

#### 1.4.2. Lấy sản phẩm mới
- **URL**: `/api/products/new`
- **Phương thức**: GET
- **Tham số**: 
  - `year`: Năm xuất bản
- **Ví dụ**: `/api/products/new?year=2023`

#### 1.4.3. Lấy sản phẩm còn hàng
- **URL**: `/api/products/in-stock`
- **Phương thức**: GET
- **Tham số**: 
  - `minStock`: Số lượng tồn kho tối thiểu (mặc định = 0)
- **Ví dụ**: `/api/products/in-stock?minStock=10`

## 2. API Danh mục (Category)

### 2.1. Lấy danh sách danh mục
- **URL**: `/api/categories`
- **Phương thức**: GET
- **Tham số phân trang**:
  - `page`: Số trang (mặc định = 0)
  - `size`: Số danh mục mỗi trang (mặc định = 10)
- **Ví dụ**: `/api/categories?page=0&size=10`

### 2.2. Lấy tất cả danh mục không phân trang
- **URL**: `/api/categories/all`
- **Phương thức**: GET

### 2.3. Lấy danh mục theo ID
- **URL**: `/api/categories/{id}`
- **Phương thức**: GET

### 2.4. Tạo danh mục mới
- **URL**: `/api/categories`
- **Phương thức**: POST
- **Body mẫu**:
```json
{
    "name": "Sách lập trình",
    "parentCategoryId": null
}
```

### 2.5. Cập nhật danh mục
- **URL**: `/api/categories/{id}`
- **Phương thức**: PUT
- **Body**: Tương tự như tạo mới

### 2.6. Xóa danh mục
- **URL**: `/api/categories/{id}`
- **Phương thức**: DELETE

## 3. API Phân loại sản phẩm (Product Category)

### 3.1. Lấy danh sách phân loại sản phẩm
- **URL**: `/api/product-categories`
- **Phương thức**: GET
- **Tham số phân trang**:
  - `page`: Số trang (mặc định = 0)
  - `size`: Số phân loại mỗi trang (mặc định = 10)
- **Ví dụ**: `/api/product-categories?page=0&size=10`

### 3.2. Lấy tất cả phân loại không phân trang
- **URL**: `/api/product-categories/all`
- **Phương thức**: GET

### 3.3. Lấy phân loại theo ID
- **URL**: `/api/product-categories/{id}`
- **Phương thức**: GET

### 3.4. Tạo phân loại mới
- **URL**: `/api/product-categories`
- **Phương thức**: POST
- **Body mẫu**:
```json
{
    "productId": 1,
    "categoryId": 1
}
```

### 3.5. Cập nhật phân loại
- **URL**: `/api/product-categories/{id}`
- **Phương thức**: PUT
- **Body**: Tương tự như tạo mới

### 3.6. Xóa phân loại
- **URL**: `/api/product-categories/{id}`
- **Phương thức**: DELETE

## Cấu trúc dữ liệu

### Response cho một sản phẩm
```json
{
    "productId": 1,
    "name": "Clean Code",
    "description": "Sách về lập trình sạch",
    "price": 450000,
    "stockQuantity": 50,
    "discount": 10,
    "realPrice": 405000,
    "author": "Robert C. Martin",
    "publisher": "Prentice Hall",
    "publicationYear": 2008,
    "pageCount": 464,
    "ISBN": "9780132350884",
    "categoryIds": [1, 2],
    "imageUrls": ["url1", "url2"],
    "averageRating": 4.5,
    "reviewCount": 100
}
```

### Response cho danh sách có phân trang
```json
{
    "content": [
        // Mảng các đối tượng
    ],
    "totalElements": 100,    // Tổng số phần tử
    "totalPages": 10,        // Tổng số trang
    "number": 0,             // Trang hiện tại
    "size": 10,             // Số phần tử mỗi trang
    "first": true,          // Có phải trang đầu không
    "last": false           // Có phải trang cuối không
}
```

## Lưu ý chung
1. Tất cả các API trả về danh sách đều hỗ trợ phân trang
2. Mặc định mỗi trang có 10 phần tử
3. Có thể sắp xếp theo bất kỳ trường nào bằng tham số `sort`
4. Tìm kiếm không phân biệt chữ hoa/thường
5. Các API đều trả về mã lỗi 404 nếu không tìm thấy dữ liệu
6. Đối với sản phẩm, giá trị trả về luôn bao gồm đánh giá trung bình và số lượng đánh giá 