package com.test.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.test.model.entity.User;

@Repository("users")
public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findById(Long id);
	User findByName(String name);
	User findByNameAndPassword(String name,String password);
	User findByEmailAndPassword(String email,String password);
	User findByEmail(String email);
	
	@Query(value="select * from users order by  commentCount limit ?1,?2",nativeQuery=true)
	public List<User> findBatchUser(Integer offset,Integer count);
}
