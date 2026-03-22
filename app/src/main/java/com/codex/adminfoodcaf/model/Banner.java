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

        private  String banner_id;
        private  String banner_title;
        private  String banner_body;
        private  String banner_date;
        private  String banner_url;

}
