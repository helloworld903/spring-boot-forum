package com.test.controller;


import java.io.IOException;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.test.common.util.MailUtils;
import com.test.model.entity.User;
import com.test.model.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MailController {
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private RedisTemplate<String,String>redisTemplate;
	@Autowired
	private UserRepository userRepository;
	@Value("${spring.mail.username}")
	private String emailUserName;
	
	@ResponseBody
	@PostMapping("/api/activate/")
	public JSONObject activate(HttpServletRequest request,HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		JSONObject res = new JSONObject();
		
		//to do
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			res.put("status", 1);
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
		
		User realUser=userRepository.findByEmail(email);
		if(realUser==null || !encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return null;
		}
		
		String emailCode=MailUtils.randomNumBuilder();
		redisTemplate.opsForValue().set(emailCode,email,2*60,TimeUnit.SECONDS);
		
		
		StringBuffer body=new StringBuffer();
		body.append("客官您来啦,里面请!\n\n").append("    您的激活链接为:  ").append("\n\n http://114.212.116.80:8081/userActivate?code=");
		body.append(emailCode+"\n\n");
        body.append("    客官请注意:需要您在收到邮件后2分钟内使用，否则该链接将会失效。\n\n");
        String message=body.toString();
        
        try {
        	MimeMessage mimeMessage=mailSender.createMimeMessage();
        	MimeMessageHelper mailMessage=new MimeMessageHelper(mimeMessage);
        	mailMessage.setFrom(emailUserName);
        	mailMessage.setTo(email);
        	mailMessage.setSubject("验证邮件");
        	mailMessage.setText(message);
        	
        	mailSender.send(mimeMessage);
        }catch(Exception e) {
			return null;
        }
        
		 //进行回调
	    res.put("status", 0);
	    return res;
	}
	
	//此处应有拦截器
	@RequestMapping("/userActivate")
	public void userActivate(@RequestParam String code,HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		System.out.println(code);
		if(code==null)return;
		
		String email=redisTemplate.opsForValue().get(code);
		User user=userRepository.findByEmail(email);
		if(user==null) {
			response.getWriter().print("<script>alert('不存在需要激活的用户或链接已过期');</script>");
			return;
		}
		user.setIsActivated(1);
		try {
			userRepository.save(user);
			response.getWriter().printf("<script>alert('用户邮箱%s已激活');</script>",email);
			redisTemplate.delete(code);
		}catch(Exception e) {
			e.printStackTrace();
			response.getWriter().print("<script>alert('服务器出错了');</script>");
		}
	}
	
}
