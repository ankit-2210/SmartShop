package com.ecommerce.util;

import com.ecommerce.model.Orders.Order;
import com.ecommerce.model.Orders.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    public Boolean sendMail(String url, String receiptEmail) throws UnsupportedEncodingException, MessagingException {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom("aki.code22@gmail.com", "Shooping Cart");
            helper.setTo(receiptEmail);

            String content = "<p>Hello, </p>" + "<p> You have requested to reset your password.</p>"
                    + "<p>Click the link below to change your password: </p>" + "<p><a href=\"" + url
                    + "\">Change my password</a></p>";

            helper.setSubject("Password Reset");
            helper.setText(content, true);
            mailSender.send(message);

            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String generateUrl(HttpServletRequest request){
        String siteUrl=request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }


    public Boolean sendMailForOrder(Order order, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("aki.code22@gmail.com", "Shopping Cart");
            helper.setTo(order.getOrderAddress().getEmail());

            String subject;
            String introMessage;

            switch (status) {
                case "Order Confirmation":
                    subject = "Order Confirmation - #" + order.getOrderId();
                    introMessage = "Your payment was successful and your order has been confirmed.";
                    break;
                case "Received":
                    subject = "Order Received - #" + order.getOrderId();
                    introMessage = "We have received your order. It is now being processed.";
                    break;
                case "Out for Delivery":
                    subject = "Your order is out for delivery - #" + order.getOrderId();
                    introMessage = "Your order is on the way and will reach you soon.";
                    break;
                case "Delivered":
                    subject = "Your order has been delivered - #" + order.getOrderId();
                    introMessage = "We are happy to inform you that your order has been successfully delivered.";
                    break;
                case "Cancelled":
                    subject = "Your order has been cancelled - #" + order.getOrderId();
                    introMessage = "Your order was cancelled as per your request or due to an issue.";
                    break;
                default:
                    return true;
            }

            String content =
                    "<div style='font-family: Arial, sans-serif; color:#333;'>" +
                            "<h2 style='color:#2d89ef;'>" + subject + "</h2>" +
                            "<p>Hello <b>" + order.getOrderAddress().getFirstName() + " " +
                            order.getOrderAddress().getLastName() + "</b>,</p>" +
                            "<p>" + introMessage + "</p>" +

                            "<h3 style='color:#444;'>Order Details:</h3>" +
                            "<table style='width:100%; border-collapse: collapse; margin-bottom:20px;'>" +
                            "<thead>" +
                            "<tr style='background-color:#2d89ef; color:#fff;'>" +
                            "<th style='padding:10px; text-align:left;'>Product</th>" +
                            "<th style='padding:10px; text-align:center;'>Quantity</th>" +
                            "<th style='padding:10px; text-align:right;'>Price</th>" +
                            "</tr>" +
                            "</thead><tbody>";

            for (OrderItem item : order.getItems()) {
                content +=
                        "<tr style='border-bottom:1px solid #ddd;'>" +
                                "<td style='padding:10px;'>" + item.getProduct().getName() + "</td>" +
                                "<td style='padding:10px; text-align:center;'>" + item.getQuantity() + "</td>" +
                                "<td style='padding:10px; text-align:right;'>₹" + item.getProduct().getPrice() + "</td>" +
                                "</tr>";
            }

            content +=
                    "</tbody></table>" +
                            "<p><b>Total Amount: </b> ₹" + order.getTotalAmount() + "</p>" +
                            "<p><b>Payment ID:</b> " + (order.getPaymentId() != null ? order.getPaymentId() : "N/A") + "</p>" +
                            "<p><b>Payment Type:</b> " + (order.getPaymentType() != null ? order.getPaymentType() : "N/A") + "</p>" +

                            "<h3 style='color:#444;'>Shipping Address:</h3>" +
                            "<p>" +
                            order.getOrderAddress().getFirstName() + " " + order.getOrderAddress().getLastName() + "<br/>" +
                            order.getOrderAddress().getAddress() + "<br/>" +
                            order.getOrderAddress().getCity() + ", " +
                            order.getOrderAddress().getState() + " - " +
                            order.getOrderAddress().getPincode() + "<br/>" +
                            order.getOrderAddress().getCountry() + "<br/>" +
                            "Phone: " + order.getOrderAddress().getMobileNo() +
                            "</p>" +

                            "<hr style='margin:20px 0;'/>" +
                            "<p style='color:#555;'>Thank you for shopping with us!<br/>" +
                            "<b>The Ecommerce Team</b></p>" +
                            "</div>";

            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }










}
