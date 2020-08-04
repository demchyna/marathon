package com.softserve.edu.repository;


import com.softserve.edu.model.Progress;
import com.softserve.edu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "select * from users where email =?1", nativeQuery = true)
    User getUserByEmail(String email);
}
