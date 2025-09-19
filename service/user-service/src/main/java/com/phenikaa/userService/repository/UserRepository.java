package com.phenikaa.userService.repository;

import com.phenikaa.userService.entity.AccountStatus;
import com.phenikaa.userService.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByUserNameOrEmail(String userName, String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // Pagination and search methods
    Page<User> findByStatus(AccountStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR u.status = :status)")
    Page<User> findBySearchAndStatus(@Param("search") String search,
            @Param("status") AccountStatus status,
            Pageable pageable);
}
