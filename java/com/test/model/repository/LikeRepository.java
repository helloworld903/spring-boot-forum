package com.test.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.model.entity.Comment;
import com.test.model.entity.Like;
import com.test.model.entity.User;

@Repository("likes")
public interface  LikeRepository  extends JpaRepository<Like,Long> {
	Like findByOwner(User owner);
	Like findByOwnerAndComment(User owner,Comment comment);
}
