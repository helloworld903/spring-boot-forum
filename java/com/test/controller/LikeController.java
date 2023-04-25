package com.test.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.test.model.entity.Comment;
import com.test.model.entity.Like;
import com.test.model.entity.Message;
import com.test.model.entity.User;
import com.test.model.repository.CommentRepository;
import com.test.model.repository.LikeRepository;
import com.test.model.repository.MessageRepository;
import com.test.model.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LikeController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	LikeRepository likeRepository;
	@Autowired
	MessageRepository messageRepository;
	
	@ResponseBody
	@PostMapping("/api/like/")
	public JSONObject likChange(HttpServletRequest request,HttpServletResponse response,Model model) {
		response.setContentType("text/html;charset=UTF-8");
		JSONObject res = new JSONObject();
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null)return null;
		
		String email="";
		String encPass="";
		for(Cookie cookie:cookies) {
			if("email".equals(cookie.getName())) {
				email=cookie.getValue();
			}
			if("encPass".equals(cookie.getName())) {
				encPass=cookie.getValue();
			}
		}
	
		User user=userRepository.findByEmail(email);
		if(user==null || !encPass.equals(String.valueOf(user.getPassword().hashCode())))return null;
		request.getSession().setAttribute("user", user);
				

		Long id=Long.valueOf(request.getParameter("id"));
		Comment comment=commentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("找不到id为" + id + "的评论"));
		if(comment==null)return null;
		
		Like tmp=likeRepository.findByOwnerAndComment(user,comment);
		if(tmp==null) {
			Like like=new Like();
			like.setDate(new Date());
			like.setOwner(user);
			like.setComment(comment);
			
			comment.setLikeCount(comment.getLikeCount()+1);
			commentRepository.save(comment);
			likeRepository.save(like);
		}else {
			comment.setLikeCount(comment.getLikeCount()-1);
			commentRepository.save(comment);
			likeRepository.delete(tmp);
			
			String ok=request.getParameter("ok");
			if(ok.equals("false"))return null;
		}
		
		if(tmp==null) {
			Message message=new Message();
			message.setSender(user.getName());
			message.setReceiver(comment.getAuthor());
			message.setTag("comment");
			message.setComment(comment);
			message.setPaper(comment.getPaper());
			message.setContent("点赞了你的评论");
			message.setPublishDate(new Date());
			
			User author=comment.getAuthor();
			if(author.getMessagesCount()==null)
				author.setMessagesCount(1);
			else
				author.setMessagesCount(author.getMessagesCount()+1);
			messageRepository.save(message);
			userRepository.save(author);
		}
		request.getSession().setAttribute("user",user);
		res.put("status", 0);
		return res;
	}
}
