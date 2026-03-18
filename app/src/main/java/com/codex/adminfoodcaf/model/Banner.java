package com.codex.adminfoodcaf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Banner {

        private  String title;
        private  String offer;
        private  String Date;
        private  String imageUrl;

}
