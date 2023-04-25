package com.test.model.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="messages")
public class Message {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="title")
	private String title;
	@Column(name="description")
	private String description;
	@Column(name="tag")
	private String tag; // "paper" "comment"
	@Column(name="publishDate")
	private Date publishDate;
	@Column(name="status")
	private Integer status;
	@Column(name="content")
	private String content;
	@Column(name="sender")
	private String sender;
	
	@ManyToOne
	@JoinColumn(name="receiver_id")
	private User receiver;
	@ManyToOne
	@JoinColumn(name="paper_id")
	private Paper paper;
	@ManyToOne
	@JoinColumn(name="comment_id")
	private Comment comment;
}
