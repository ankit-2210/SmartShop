package com.ecommerce.repository;

import com.ecommerce.model.Users.Address;
import com.ecommerce.model.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    public User getUserByEmail(String email);

    public List<User> findByRole(String role);

    public User getUserByResetToken(String token);

}
