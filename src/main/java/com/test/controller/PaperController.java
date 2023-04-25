package com.test.controller;


import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.test.model.entity.Paper;
import com.test.model.entity.User;
import com.test.model.repository.PaperRepository;
import com.test.model.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Controller
public class PaperController {
	@Autowired
	PaperRepository paperRepository;
	@Autowired
	UserRepository userRepository;
	
	@GetMapping("/writePaper")
	public String writePaper(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
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
			model.addAttribute("user", realUser);
			return "html/jie/add";
		}
		
		User user=(User) request.getSession().getAttribute("user");
		//System.out.println(user.getEmail()+";"+user.getPassword());
		if(user==null || !email.equals(user.getEmail()) || !encPass.equals(String.valueOf(user.getPassword().hashCode()))){
			response.getWriter().print("<script>alert('session有误，请先登录');</script>");
			return "html/user/login";
		}
		
		model.addAttribute("user", user);
		return "html/jie/add";
	}
	
	@PostMapping("/paperRegister")
	public String paperRegister(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException{
		response.setContentType("text/html;charset=UTF-8");
		Enumeration<String>res=request.getParameterNames();
		while(res.hasMoreElements()) {
			String key=res.nextElement();
			System.out.println(key+";"+request.getParameter(key));
		}
		
		
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
		Paper paper=new Paper();
		paper.setTitle(request.getParameter("title"));
		paper.setContent(request.getParameter("content"));
		paper.setTag(request.getParameter("tag"));
		paper.setAuthor(user);
		paper.setCommentCount(0);
		paper.setViewCount(0);
		paper.setLikeCount(0);
		paper.setPublishDate(new Date());
		paper.setModifyDate(new Date());
		paper.setPermission(0);//0 public 1 private
		paper.setStatus(0);// 0 unresolved 1 resolved
		try{
			paperRepository.save(paper);
			response.getWriter().print("<script>alert('保存成功');</script>");
			model.addAttribute("user",user);
			model.addAttribute("paper",paper);
			return "html/jie/detail";
		}catch(Exception e){
			e.printStackTrace();
			response.getWriter().printf("<script>alert('保存出错 %s');</script>",e.toString());
			return "error";
		}
	}

	@GetMapping("/paper/{id}")
	public String getPaper(@PathVariable Long id,HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		//权限管理
		Paper paper=paperRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("找不到id为" + id + "的论文"));
		if(paper==null || paper.getPermission()==1)return "error"; //can not read
		int viewCount=paper.getViewCount();
		paper.setViewCount(viewCount+1);
		try {
			paperRepository.save(paper);
		}catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
		
		List<Paper>newPapers=paperRepository.findBatchPaperByCommentCountWhthinWeek();
		model.addAttribute("newPapers", newPapers);
		
		model.addAttribute("paper",paper);
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			return "html/jie/detail";
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
				Cookie emailCookie=new Cookie("email","");
				emailCookie.setMaxAge(0);
				response.addCookie(emailCookie);
				Cookie passCookie=new Cookie("encPass","");
				passCookie.setMaxAge(0);
				response.addCookie(passCookie);
				request.getSession().removeAttribute("user");
				return "html/jie/detail";
			}
			request.getSession().setAttribute("user", realUser);
		}
		
		User user=(User) request.getSession().getAttribute("user");
		if(user==null || !email.equals(user.getEmail()) || !encPass.equals(String.valueOf(user.getPassword().hashCode()))){
			Cookie emailCookie=new Cookie("email","");
			emailCookie.setMaxAge(0);
			response.addCookie(emailCookie);
			Cookie passCookie=new Cookie("encPass","");
			passCookie.setMaxAge(0);
			response.addCookie(passCookie);
			request.getSession().removeAttribute("user");
			return "html/jie/detail";
		}
	
		model.addAttribute("user",user);
		return "/html/jie/detail";
	}
	
	@GetMapping("/getMorePapers")
	public String getMorePapers(HttpServletRequest request,HttpServletResponse response,Model model) {
		response.setContentType("text/html;charset=UTF-8");
		List<Paper>newPapers=paperRepository.findBatchPaperByCommentCountWhthinWeek();
		model.addAttribute("newPapers", newPapers);
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			return "html/jie/index";
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
			if(realUser==null) {
				return "html/jie/index";
			}
			if(encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
				request.getSession().setAttribute("user", realUser);
				model.addAttribute("user", realUser);
				return "html/jie/index";
			}
		}
		
		User user=(User) request.getSession().getAttribute("user");
		if(user==null || !email.equals(user.getEmail()) || !encPass.equals(String.valueOf(user.getPassword().hashCode()))){
			return "html/jie/index";
		}
		model.addAttribute("user", user);
		return "html/jie/index";
	}
	
}
