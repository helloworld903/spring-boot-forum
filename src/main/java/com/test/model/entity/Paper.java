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
@Table(name="papers")
public class Paper {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(nullable=false)
	private String title;
	@Column(name="description")
	private String description;
	@Column(name="tag")
	private String tag;
	@Column(name="images")
	private String images;
	@Column(name="video")
	private String video;
	@Column(name="type")
	private Integer type;
	@Column(name="status")
	private Integer status;
	@Column(name="permission")
	private Integer permission;
	@Column(name="content",length=100000)
	private String content;
	@Column(name="viewCount")
	private Integer viewCount;
	@Column(name="commentCount")
	private Integer commentCount;
	@Column(name="likeCount")
	private Integer likeCount;
	@Column(name="publishDate")
    private Date publishDate;
	@Column(name="modifyDate")
	private Date modifyDate;
	@Column(name="lastComment")
	private Date lastComment;
	
	@ManyToOne
	@JoinColumn(name="author_id")
	private User author;
	@OneToMany(mappedBy="paper",fetch=FetchType.EAGER)
	private List<Comment>comments;
	@OneToMany(mappedBy="paper",fetch=FetchType.EAGER)
	private List<Message>messages;
}


