package com.test.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.test.model.entity.Comment;

@Repository("comments")
public interface CommentRepository extends JpaRepository<Comment,Long>{
	
}
