package com.codex.adminfoodcaf.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    private String orderId;
    private String userId;
    private String orderDate;
    private String status;
    private List<OrderItem> orderItems;
    private Address DeliveryAddress;
    private String paymentMethod;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItem {
        private String productId;
        private String productName;
        private double unitPrice;
        private int qty;
        private double totalPrice;
        private List<Attribute> attributes;



        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class Attribute {

            private List<String> values;
            private List<String> price;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Address {
        private String address;
        private String name;
        private String email;
        private String contactNum;

    }
}
