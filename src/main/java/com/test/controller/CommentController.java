package com.test.controller;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import com.test.model.entity.Comment;
import com.test.model.entity.Message;
import com.test.model.entity.Paper;
import com.test.model.entity.User;
import com.test.model.repository.CommentRepository;
import com.test.model.repository.MessageRepository;
import com.test.model.repository.PaperRepository;
import com.test.model.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CommentController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	PaperRepository paperRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	MessageRepository messageRepository;
	
	@PostMapping("/addComment")
	public String addComment(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return "html/user/login";
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
		
		if(request.getSession().getAttribute("user")==null) {
			User realUser=userRepository.findByEmail(email);
			if(realUser==null || !encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
				response.getWriter().print("<script>alert('请先登录');</script>");
				return "html/user/login";
			}
			request.getSession().setAttribute("user", realUser);
		}
		User user=(User) request.getSession().getAttribute("user");
		model.addAttribute("user",user);
		
		String refer=request.getHeader("Referer");
		Pattern pattern=Pattern.compile("\\/paper\\/(\\d+)");
		Matcher matcher = pattern.matcher(refer);
		Long paperId=(long) -1;
		if (matcher.find()) {
		    paperId = Long.valueOf(matcher.group(1));
		    System.out.println(paperId);
		}
		
		if(paperId==-1) {
			response.getWriter().print("<script>alert('评论文章不存在');</script>");
			return "error";
		}
		
		Paper paper=paperRepository.findById(paperId).orElseThrow(() -> new EntityNotFoundException("找不到论文"));
		if(paper==null) {
			response.getWriter().print("<script>alert('评论文章不存在');</script>");
			return "error";
		}
		String content=request.getParameter("content");//do some @somebody
		Comment comment=new Comment();
		comment.setPaper(paper);
		comment.setContent(content==null?"":content);
		comment.setLikeCount(0);
		comment.setCommentCount(0);
		comment.setModifyDate(new Date());
		comment.setAuthor(user);
		
		try {
			//发送消息
			Message message=new Message();
			message.setSender(user.getName());
			message.setReceiver(paper.getAuthor());
			message.setPaper(paper);
			message.setContent("评论了你的说说");
			message.setPublishDate(new Date());
			if(user.getCommentCount()==null)
				user.setCommentCount(1);
			else
				user.setCommentCount(user.getCommentCount()+1);
			
			messageRepository.save(message);
			commentRepository.save(comment);
			paper.setCommentCount(paper.getCommentCount()+1);
			paperRepository.save(paper);
			userRepository.save(user);
			
			User author=paper.getAuthor();
			if(author.getMessagesCount()==null)
				author.setMessagesCount(1);
			else
				author.setMessagesCount(author.getMessagesCount()+1);
			userRepository.save(author);
			request.getSession().setAttribute("user", user);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		paper=paperRepository.findById(paperId).orElseThrow(() -> new EntityNotFoundException("找不到论文"));
		model.addAttribute("paper",paper);
		return "redirect:"+"/paper/"+paperId.toString();
	}
}
