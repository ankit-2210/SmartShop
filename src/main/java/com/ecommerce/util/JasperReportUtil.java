package com.ecommerce.util;

import com.ecommerce.model.Orders.Order;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class JasperReportUtil {

    public String generateOrderInvoice(Order order) {
        try {
            // Load JRXML from classpath
            InputStream reportStream =
                    this.getClass().getResourceAsStream("/reports/order_status_report.jrxml");

            if (reportStream == null) {
                throw new RuntimeException("JRXML file not found in resources/reports/");
            }

            // Compile JRXML to JasperReport
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Parameters
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", order.getOrderId());
            params.put("orderDate", order.getOrderDate());
            params.put("firstName", order.getOrderAddress().getFirstName());
            params.put("lastName", order.getOrderAddress().getLastName());
            params.put("address", order.getOrderAddress().getAddress());
            params.put("city", order.getOrderAddress().getCity());
            params.put("state", order.getOrderAddress().getState());
            params.put("pincode", order.getOrderAddress().getPincode());
            params.put("paymentId", order.getPaymentId());
            params.put("paymentStatus", order.getPaymentStatus());
            params.put("totalAmount", order.getTotalAmount());

            JRBeanCollectionDataSource itemSource = new JRBeanCollectionDataSource(order.getItems());

            params.put("itemDataSource", itemSource);

            // Output folder
            File folder = new File("invoices");
            if (!folder.exists())
                folder.mkdirs();

            String output = "invoices/order_" + order.getOrderId() + ".pdf";

            // Fill report
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

            // Export PDF
            JasperExportManager.exportReportToPdfFile(print, output);
            return output;

        }
        catch (Exception e) {
            throw new RuntimeException("Invoice generation failed: " + e.getMessage(), e);
        }
    }
}
