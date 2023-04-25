package com.test.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.test.model.entity.Message;
import com.test.model.entity.User;
import com.test.model.repository.MessageRepository;
import com.test.model.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MessageController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	MessageRepository messageRepository;
	
	@GetMapping("/userMessage")
	public String userMessage(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return  "redirect:/login";
		}
		
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
		if(user==null || !encPass.equals(String.valueOf(user.getPassword().hashCode()))) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return "redirect:/login";
		}
		request.getSession().setAttribute("user", user);
		model.addAttribute("user", user);
		return  "html/user/message";
	}
	
	@ResponseBody
	@PostMapping("/message/remove/")
	public JSONObject removeMessage(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		JSONObject res = new JSONObject();
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			return null;
		}
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
		
		User user=(User)userRepository.findByEmail(email);
		if(user==null || !encPass.equals(String.valueOf(user.getPassword().hashCode())))return null;
		
		if(request.getParameter("all")!=null) {
			try {
				List<Message>messages=user.getRecvMessages();
				for(Message m:messages) {
						messageRepository.delete(m);
				}
				
				User newUser=userRepository.findByEmail(email);
				newUser.setMessagesCount(0);
				newUser.setRecvMessages(null);
				userRepository.save(newUser);
				request.getSession().setAttribute("user", newUser);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}else{
			Long messageId=Long.valueOf(request.getParameter("id"));
			Message message=messageRepository.findById(messageId).orElseThrow(() -> new EntityNotFoundException("找不到id为" + messageId + "的消息"));
			if(message==null) {
				return null;
			}
			if(!message.getReceiver().getEmail().equals(user.getEmail()))return null;
			
			user.setMessagesCount(user.getMessagesCount()-1);
			messageRepository.delete(message);
			User newUser=userRepository.findByEmail(email);
			request.getSession().setAttribute("user", newUser);
		}
		
		 //进行回调
	    res.put("status", 0);
	    return res;
	}
}
