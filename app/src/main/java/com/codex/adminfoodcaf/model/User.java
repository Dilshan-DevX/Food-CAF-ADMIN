package com.codex.adminfoodcaf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String uId;
    private String name;
    private String email;
    private String address;
    private String mobileNum;
    private String profilePicUrl;
    private boolean status;


}
