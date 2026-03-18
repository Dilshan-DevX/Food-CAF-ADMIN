package com.codex.adminfoodcaf.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private String productId;
    private String categoryId;
    private String foodRating;
    private String foodTitle;
    private double productPrice;
    private List<String> productImage;
    private String foodTime;
    private String foodDetail;
    private String ingrideint;
    private boolean availability;
    private List<Attribute> attribute;



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor

//    public static class Attribute {
//
//        @com.google.firebase.firestore.PropertyName("porsion")
//        private String name;
//        private String type;
//        private List<String> values;
//
//        @com.google.firebase.firestore.PropertyName("Price")
//        private List<String> prices;
//    }


    public static class Attribute {
        private String porsion;
        private String type;
        private List<String> values;
        private List<String> Price;



        @com.google.firebase.firestore.PropertyName("porsion")
        public String getPorsion() { return porsion; }

        @com.google.firebase.firestore.PropertyName("porsion")
        public void setPorsion(String porsion) { this.porsion = porsion; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getValues() { return values; }
        public void setValues(List<String> values) { this.values = values; }

        @com.google.firebase.firestore.PropertyName("Price")
        public List<String> getPrice() { return Price; }

        @com.google.firebase.firestore.PropertyName("Price")
        public void setPrice(List<String> Price) { this.Price = Price; }
    }

}
