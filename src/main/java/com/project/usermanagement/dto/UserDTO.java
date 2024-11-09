package com.project.usermanagement.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public class UserDTO {
        @Schema(description= "Auto generated")
        private UUID userid;
        @Schema(description = "Username must be atleast 3 characters")
        private String username;
        @Schema(description = "Email should be of proper format")
        private String email;

        private String address;

        private short age;

        public UserDTO(UUID userid, String username,  String address,String email, short age) {
            this.userid = userid;
            this.username = username;
            this.email = email;
            this.address = address;
            this.age = age;
        }

        public UserDTO() {
        }

        public UUID getUserid() {
            return userid;
        }

        public void setUserid(UUID userid) {
            this.userid = userid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public short getAge() {
            return age;
        }

        public void setAge(short age) {
            this.age = age;
        }
    }
