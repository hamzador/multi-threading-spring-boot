package org.dr.multithreadspringboot.repository;

import org.dr.multithreadspringboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
