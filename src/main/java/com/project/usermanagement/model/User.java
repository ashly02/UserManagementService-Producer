package com.project.usermanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value="users")
public class User {
   @PrimaryKey
   @Id
   private UUID userid;

   private String username;

   private String email;

   private String address;

   private short age;

    public User(UUID userid, String username,  String address,String email, short age) {
        this.userid = userid;
        this.username = username;
        this.email = email;
        this.address = address;
        this.age = age;
    }

    public User() {
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
