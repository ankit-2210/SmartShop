package com.ecommerce.repository;

import com.ecommerce.model.Users.Profile.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    public User getUserByEmail(String email);

    public List<User> findByRole(String role);

    public User getUserByResetToken(String token);

}
