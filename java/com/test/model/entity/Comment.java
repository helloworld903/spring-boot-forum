package com.test.model.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="comments")
public class Comment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="parentId")
	private Long parentId;
	@Column(name="content")
	private String content;
	@Column(name="modifyDate")
	private Date modifyDate;
	@Column(name="likeCount")
	private Integer likeCount;
	@Column(name="commentCount")
	private Integer commentCount;
	
	@ManyToOne
	@JoinColumn(name="paper_id")
	private Paper paper;
	@ManyToOne
	@JoinColumn(name="author_id")
	private User author;
	@OneToMany(mappedBy="comment",fetch=FetchType.EAGER)
	private List<Like>likes;
	@OneToMany(mappedBy="comment",fetch=FetchType.EAGER)
	private List<Message>messages;
}
