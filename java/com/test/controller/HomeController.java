package com.test.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.test.model.entity.Paper;
import com.test.model.entity.User;
import com.test.model.repository.PaperRepository;
import com.test.model.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	PaperRepository paperRepository;
	
	@GetMapping(value={"","/","/home"})
	public String home(HttpServletRequest request,HttpServletResponse response,Model model) {
		response.setContentType("text/html;charset=UTF-8");
		
		List<Paper>papers;
		String sort=request.getParameter("sort");
		String type=request.getParameter("type");
		
		if(type==null) {
			if(sort!=null) {
				papers=paperRepository.findBatchPaperByCommentCount(0, 12);
				model.addAttribute("sort", "count");
			}else {
				papers=paperRepository.findBatchPaper(0,12);
			}
		}else {
			if(sort!=null) {
				papers=paperRepository.findBatchPaperByCommentCountAndType(type,0, 12);
				model.addAttribute("sort", "count");
				model.addAttribute("type", type);
			}else {
				papers=paperRepository.findBatchPaperAndType(type,0,12);
				model.addAttribute("type", type);
			}
		}
		List<Paper>newPapers=paperRepository.findBatchPaperByCommentCountWhthinWeek();
		List<User>users=userRepository.findBatchUser(0, 12);
		model.addAttribute("users",users);
		model.addAttribute("papers",papers);
		model.addAttribute("newPapers", newPapers);
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null)return "html/index";
		
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
				return "html/index";
			}
			if(encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
				request.getSession().setAttribute("user", realUser);
				model.addAttribute("user", realUser);
				return "html/index";
			}
		}
		User user=(User) request.getSession().getAttribute("user");
		if(user==null || !email.equals(user.getEmail()) || !encPass.equals(String.valueOf(user.getPassword().hashCode()))){
			return "html/index";
		}
		model.addAttribute("user", user);
		return "html/index";
	}
	
	@GetMapping("/userHome")
	public String userHome(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
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
		
		User realUser=userRepository.findByEmail(email);
		if(realUser==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return "html/user/login";
		}
		if(encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
			request.getSession().setAttribute("user", realUser);
			model.addAttribute("user", realUser);
			return "html/user/home";
		}
		
		model.addAttribute("user", realUser);
		return "html/user/home";
	}
	
	@GetMapping("/jump")
	public String jumpTo(@RequestParam String username,HttpServletRequest request,HttpServletResponse response,Model model) throws IOException{
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
		User visitor=userRepository.findByEmail(email);
		if(visitor!=null&&encPass.equals(String.valueOf(visitor.getPassword().hashCode()))) {
			model.addAttribute("visitor",visitor);
		}
		if(visitor.getName().equals(username)) return "redirect:/userHome";
		
		User user=userRepository.findByName(username);
		model.addAttribute("user", user);
		return "html/user/visitor";
		
	}
}
