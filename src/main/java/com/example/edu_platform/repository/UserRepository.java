package com.example.edu_platform.repository;


import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Stream<User> findAllByRole(Role role); // удобно для выборок преподавателей/студентов
}
