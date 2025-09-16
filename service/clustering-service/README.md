# User Clustering Service

Dịch vụ phân cụm người dùng cho hệ thống booking tour du lịch Wayzy.

## 🎯 Mục đích

Áp dụng thuật toán machine learning để phân cụm người dùng dựa trên:

- **Demographic features**: Tuổi, vùng miền, giới tính
- **Behavioral features**: Loại tour ưa thích, mùa du lịch, kích thước nhóm
- **Engagement features**: Tần suất đặt tour, phản hồi với khuyến mãi

## 🚀 Tính năng chính

### 1. Thuật toán phân cụm

- **K-Means**: Thuật toán chính cho phân cụm
- **DBSCAN**: Dự phòng cho dữ liệu có nhiều noise
- **Hierarchical**: Phân cụm phân cấp

### 2. Tích hợp dữ liệu

- Đồng bộ dữ liệu từ User Service
- Lấy lịch sử booking từ Booking Service
- Thông tin tour từ Tour Service

### 3. Gợi ý cá nhân hóa

- Gợi ý tour dựa trên cluster của người dùng
- Phân tích hành vi tương tự
- Cập nhật real-time khi có booking mới

### 4. Dashboard quản trị

- Xem kết quả clustering
- Thống kê chi tiết
- Quản lý cluster

## 🛠️ Cài đặt và chạy

### 1. Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Spring Boot 3.0+

### 2. Cấu hình database

```sql
CREATE DATABASE wayzy_clustering;
```

### 3. Cấu hình application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wayzy_clustering
    username: your_username
    password: your_password

clustering:
  algorithm:
    type: KMEANS
    k-means:
      clusters: 5
      max-iterations: 100
```

### 4. Chạy service

```bash
mvn spring-boot:run
```

## 📊 API Endpoints

### Clustering Operations

- `POST /api/clustering/perform` - Chạy clustering
- `GET /api/clustering/stats` - Thống kê clustering
- `GET /api/clustering/clusters` - Lấy danh sách cluster
- `GET /api/clustering/clusters/{id}` - Chi tiết cluster

### User Recommendations

- `GET /api/clustering/recommendations/{userId}` - Gợi ý cho user
- `POST /api/clustering/profiles` - Tạo/cập nhật profile
- `PUT /api/clustering/profiles/{userId}/cluster` - Cập nhật cluster

## 🎨 Frontend Integration

### 1. Admin Dashboard

```jsx
import ClusteringDashboard from "./pages/admin-ui/ClusteringDashboard";

// Trong AdminRoute
<Route path="/admin/clustering" element={<ClusteringDashboard />} />;
```

### 2. Personalized Recommendations

```jsx
import PersonalizedRecommendations from "./components/PersonalizedRecommendations";

// Trong trang tour hoặc dashboard user
<PersonalizedRecommendations userId={currentUser.id} />;
```

## 🔄 Workflow

### 1. Khởi tạo

1. Service khởi động và kết nối database
2. Đồng bộ dữ liệu từ các service khác
3. Chạy clustering lần đầu

### 2. Cập nhật định kỳ

- **Mỗi 6 giờ**: Đồng bộ dữ liệu user
- **Mỗi ngày 2h sáng**: Chạy clustering toàn bộ

### 3. Cập nhật real-time

- Khi có booking mới → Cập nhật profile user
- Khi có user mới → Gán vào cluster phù hợp

## 📈 Monitoring và Analytics

### 1. Metrics quan trọng

- Số lượng cluster
- Số lượng user trong mỗi cluster
- Độ chính xác clustering
- Tỷ lệ chuyển đổi từ gợi ý

### 2. Logs

- Clustering process logs
- User data sync logs
- Recommendation generation logs

## 🔧 Tùy chỉnh

### 1. Thêm thuộc tính clustering

```java
// Trong UserProfile entity
@Column(name = "new_feature")
private String newFeature;

// Trong KMeansClustering
private double[] extractFeatureVector(UserProfile profile) {
    // Thêm logic xử lý newFeature
}
```

### 2. Thêm thuật toán clustering

```java
@Service
public class NewClusteringAlgorithm implements ClusteringAlgorithm {
    // Implement interface methods
}
```

### 3. Tùy chỉnh gợi ý

```java
// Trong ClusteringServiceImpl
@Override
public List<Integer> getRecommendations(Integer userId) {
    // Custom recommendation logic
}
```

## 🚨 Troubleshooting

### 1. Lỗi kết nối database

- Kiểm tra cấu hình MySQL
- Đảm bảo database đã được tạo
- Kiểm tra quyền truy cập

### 2. Lỗi đồng bộ dữ liệu

- Kiểm tra các service khác có chạy không
- Kiểm tra URL endpoints
- Xem logs để debug

### 3. Clustering không chính xác

- Tăng số lượng cluster
- Điều chỉnh thuật toán
- Kiểm tra chất lượng dữ liệu đầu vào

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Weka Machine Learning](https://www.cs.waikato.ac.nz/ml/weka/)
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/)
- [K-Means Clustering](https://en.wikipedia.org/wiki/K-means_clustering)

## 🤝 Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.


