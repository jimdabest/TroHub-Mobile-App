package me.ihqqq.trohub.repository;

import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying
    @Query("""
            update User u
            set u.password = :password
            where u.username = :username
              and u.role = :role
            """)
    int updatePasswordForRole(@Param("username") String username,
                              @Param("role") Role role,
                              @Param("password") String password);
}