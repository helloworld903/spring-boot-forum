package com.test.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.test.model.entity.Message;
import com.test.model.entity.User;

@Repository("messages")
public interface MessageRepository extends JpaRepository<Message,Long>{
	@Override
	@Query(value="select * from messages where id=?1",nativeQuery=true)
	public Optional<Message> findById(Long id);
	@Query(value="select * from messages where receiver_id=?1",nativeQuery=true)
	public List<Message> findByReceiver(Long id);
}
