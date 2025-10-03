package com.example.demo.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);


    boolean existsByEmailAndIdNot(String email , Long id);

    boolean existsByUserName(String userName);

    boolean existsByUserNameAndIdNot(String userName , Long id);

    Optional<User> findByFirstNameAndLastNameIgnoreCase(String firstName , String lastName);

    Optional <User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName , String lastName);


    @Query("SELECT u FROM User u WHERE u.role =:role")
    Page<User> findByRole(@Param ( "role" ) Role role ,
                          Pageable pageable);


}
