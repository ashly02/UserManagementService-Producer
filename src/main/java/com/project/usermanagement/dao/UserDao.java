package com.project.usermanagement.dao;

import com.project.usermanagement.model.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDao extends CassandraRepository<User, UUID> {
}
