package com.test.model.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="name",nullable=false)
	private String name;
	@Column(name="password",nullable=false)
	private String password;
	@Column(name="imageSrc")
	private String imageSrc;
	@Column(name="sign")
	private String sign;
	@Column(name="age")
	private int age;
	@Column(name="sex")
	private int sex;
	@Column(name="email")
	private String email;
	@Column(name="isActivated")
	private Integer isActivated;
	@Column(name="phone")
	private String phone;
	@Column(name="address")
	private String address;
	@Column(name="registerDate")
	private Date registerDate;
	@Column(name="lastModified")
	private Date lastModified;
	@Column(name="commentCount")
	private Integer commentCount;
	@Column(name="messagesCount")
	private Integer messagesCount;
	
	
	@OneToMany(mappedBy="author",fetch=FetchType.EAGER)
    private List<Paper> papers;
	@OneToMany(mappedBy="author",fetch=FetchType.EAGER)
	private List<Comment>comments;
	@OneToMany(mappedBy="receiver",fetch=FetchType.EAGER)
	private List<Message>recvMessages;
	@OneToMany(mappedBy="owner",fetch=FetchType.EAGER)
	private List<Like>likes;
}
