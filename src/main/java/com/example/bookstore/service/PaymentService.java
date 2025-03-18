package com.example.bookstore.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Payment;
import com.example.bookstore.model.Product;
import com.example.bookstore.repository.OrderDetailRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentRepository;
import com.example.bookstore.repository.ProductRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final EmailService emailService;

    public PaymentService(OrderRepository orderRepository, 
                         OrderDetailRepository orderDetailRepository,
                         ProductRepository productRepository,
                         PaymentRepository paymentRepository,
                         OrderService orderService,
                         EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.emailService = emailService;
    }

    /**
     * Xử lý thanh toán thành công
     * @param orderId ID của đơn hàng
     * @param amount Số tiền thanh toán (VND)
     * @param transactionId ID giao dịch từ VNPay
     * @param paymentDate Ngày thanh toán
     * @return true nếu xử lý thành công, false nếu có lỗi
     */
    @Transactional
    public boolean processSuccessfulPayment(String orderId, String amount, String transactionId, String paymentDate) {
        try {
            System.out.println("Xử lý thanh toán thành công cho đơn hàng: " + orderId);
            
            // Chuyển đổi orderId từ chuỗi sang số
            Long orderIdLong;
            try {
                orderIdLong = Long.parseLong(orderId);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: ID đơn hàng không hợp lệ: " + orderId);
                return false;
            }
            
            // Tìm đơn hàng
            Optional<Order> orderOpt = orderRepository.findById(orderIdLong);
            if (orderOpt.isEmpty()) {
                System.out.println("Lỗi: Không tìm thấy đơn hàng với ID: " + orderIdLong);
                return false;
            }
            
            Order order = orderOpt.get();
            
            // Kiểm tra nếu đơn hàng đã được thanh toán trước đó
            if ("PAID".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus())) {
                System.out.println("Đơn hàng đã được thanh toán trước đó: " + orderIdLong);
                return true; // Trả về true để không hiển thị lỗi cho người dùng
            }
            
            // Tạo bản ghi thanh toán mới
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaymentMethod("VNPAY");
            
            // Chuyển đổi số tiền từ chuỗi sang số
            try {
                // VNPay trả về số tiền đã nhân 100, cần chia cho 100
                int paymentAmount = Integer.parseInt(amount) / 100;
                payment.setAmount(paymentAmount);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Số tiền không hợp lệ: " + amount);
                return false;
            }
            
            payment.setPaymentDate(new Date()); // Thời gian hiện tại
            payment.setStatus("SUCCESS");
            
            // Lưu thông tin thanh toán
            paymentRepository.save(payment);
            
            // Cập nhật trạng thái đơn hàng
            orderService.updateOrderStatus(orderIdLong, "PAID");
            
            System.out.println("Đã cập nhật trạng thái đơn hàng: " + orderIdLong + " thành PAID");
            
            // Kiểm tra tồn kho sau thanh toán
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderOrderId(orderIdLong);
            for (OrderDetail detail : orderDetails) {
                Product product = detail.getProduct();
                
                // Không cần cập nhật inventoryCount nữa vì nó được tính toán tự động
                // Chỉ cần kiểm tra nếu tồn kho thấp và cảnh báo
                int inventoryCount = product.getInventoryCount();
                if (inventoryCount < 5) {
                    // Gửi thông báo cho admin về tồn kho thấp
                    System.out.println("CẢNH BÁO: Sản phẩm " + product.getName() + " có tồn kho thấp: " + inventoryCount);
                }
            }
            
            // Gửi email xác nhận đơn hàng đã thanh toán cho khách hàng
            try {
                String userEmail = order.getUser().getEmail();
                String subject = "Xác nhận thanh toán đơn hàng #" + orderIdLong;
                String content = "Kính gửi " + order.getUser().getFullName() + ",\n\n"
                        + "Đơn hàng #" + orderIdLong + " của bạn đã được thanh toán thành công.\n"
                        + "Chúng tôi sẽ xử lý và giao hàng sớm nhất có thể.\n\n"
                        + "Cảm ơn bạn đã mua sắm tại cửa hàng sách của chúng tôi.\n\n"
                        + "Trân trọng,\nĐội ngũ cửa hàng sách";
                
                emailService.sendEmail(userEmail, subject, content);
                System.out.println("Đã gửi email xác nhận thanh toán tới: " + userEmail);
            } catch (Exception e) {
                System.out.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
                // Không cần return false vì việc gửi email thất bại không nên ảnh hưởng đến xử lý thanh toán
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý thanh toán thành công: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xử lý thanh toán thất bại
     * @param orderId ID của đơn hàng
     * @return true nếu xử lý thành công, false nếu có lỗi
     */
    @Transactional
    public boolean processFailedPayment(String orderId) {
        try {
            System.out.println("Xử lý thanh toán thất bại cho đơn hàng: " + orderId);
            
            // Chuyển đổi orderId từ chuỗi sang số
            Long orderIdLong;
            try {
                orderIdLong = Long.parseLong(orderId);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: ID đơn hàng không hợp lệ: " + orderId);
                return false;
            }
            
            // Tìm đơn hàng
            Optional<Order> orderOpt = orderRepository.findById(orderIdLong);
            if (orderOpt.isEmpty()) {
                System.out.println("Lỗi: Không tìm thấy đơn hàng với ID: " + orderIdLong);
                return false;
            }
            
            Order order = orderOpt.get();
            
            // Cập nhật trạng thái đơn hàng thành PAYMENT_FAILED
            orderService.updateOrderStatus(orderIdLong, "PAYMENT_FAILED");
            
            System.out.println("Đã cập nhật trạng thái đơn hàng: " + orderIdLong + " thành PAYMENT_FAILED");
            
            // Gửi email thông báo thanh toán thất bại cho khách hàng
            try {
                String userEmail = order.getUser().getEmail();
                String subject = "Thanh toán đơn hàng #" + orderIdLong + " thất bại";
                String content = "Kính gửi " + order.getUser().getFullName() + ",\n\n"
                        + "Thanh toán cho đơn hàng #" + orderIdLong + " của bạn không thành công.\n"
                        + "Vui lòng thử lại hoặc chọn phương thức thanh toán khác.\n\n"
                        + "Nếu bạn gặp vấn đề, hãy liên hệ với chúng tôi để được hỗ trợ.\n\n"
                        + "Trân trọng,\nĐội ngũ cửa hàng sách";
                
                emailService.sendEmail(userEmail, subject, content);
                System.out.println("Đã gửi email thông báo thanh toán thất bại tới: " + userEmail);
            } catch (Exception e) {
                System.out.println("Lỗi khi gửi email thông báo: " + e.getMessage());
                // Không cần return false vì việc gửi email thất bại không nên ảnh hưởng đến xử lý thanh toán
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý thanh toán thất bại: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 