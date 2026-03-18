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
public class CartItem {

    private String productId;
    private String productName;
    private double productPrice;
    private double unitPrice;
    private int qty;
    private List<Attribute> attributes;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Attribute {

        private List<String> values;
        private List<String> Price;
    }


}
