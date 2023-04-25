package com.test.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.test.model.entity.User;
import com.test.model.repository.PaperRepository;
import com.test.model.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserController {
	@Autowired
	UserRepository userRepository;
	@Autowired
	PaperRepository paperRepository;
	@Autowired
	JavaMailSender mailSender;
	@Value("${spring.mail.username}")
	private String emailUserName;
	//email check api-key
	@Value("${verApi-key")
	private String emailCheckKey;
	
	@GetMapping(value={"/login"})
	public String login(HttpServletRequest request,HttpServletResponse response,Model model) {
		response.setContentType("text/html;charset=UTF-8");
		return "html/user/login";
	}
	
	@PostMapping("/userLogin")
	public String doLoginPost(@RequestParam String email, @RequestParam String password,
			HttpServletRequest request,HttpServletResponse response,
			Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		User user=userRepository.findByEmailAndPassword(email, password);
		if(user==null) {
			response.getWriter().printf("<script>alert('用户%s不存在或密码错误');</script>",email);
			return "redirect:/login";
		}else {
			request.getSession().setAttribute("user", user);
			
			Cookie emailCookie=new Cookie("email",user.getEmail());
			emailCookie.setMaxAge(60*60*24);
			response.addCookie(emailCookie);
			Cookie passCookie=new Cookie("encPass",String.valueOf(user.getPassword().hashCode()));
			passCookie.setMaxAge(60*60*24);
			response.addCookie(passCookie);
		}
		response.getWriter().print("<script>alert('登录成功');</script>");
		return "redirect:/home";
	}
	
	@GetMapping("/userLogout")
	public String userLogout(HttpServletRequest request,HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		Cookie emailCookie=new Cookie("email","");
		emailCookie.setMaxAge(0);
		response.addCookie(emailCookie);
		Cookie passCookie=new Cookie("encPass","");
		passCookie.setMaxAge(0);
		response.addCookie(passCookie);
		
		response.getWriter().printf("<script>alert('退出成功');</script>");
		return "redirect:/home";
	}
	
	@GetMapping("register")
	public String register() {
		return "html/user/reg";
	}
	
	@PostMapping("/userRegister")
	public String doRegisterPost(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		response.setContentType("text/html;charset=UTF-8");
		
		String email=request.getParameter("email");
		String password=request.getParameter("password");
		String repass=request.getParameter("repass");
		System.out.println(email+";"+password+";"+repass);
		
		//密码输入不一致
		if(!password.equals(repass)) {
			response.getWriter().printf("<script>alert('两次密码输入不一致');</script>");
			return "html/user/reg";
		}
		
		//用户已经注册
		User tmp=userRepository.findByEmail(email);
		if(tmp!=null) {
			response.getWriter().printf("<script>alert('用户%s已注册');</script>",email);
			return "/html/user/reg";
		}
		
		//验证邮箱
		String url = "https://api.apilayer.com/email_verification/"+email;
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new java.security.SecureRandom());
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", emailCheckKey);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        Gson gson = new Gson();
        
        @SuppressWarnings("unchecked")
		Map<String, Object> jsonMap = gson.fromJson(sb.toString(), Map.class);
        System.out.println(sb.toString());
        boolean invalid=(boolean) jsonMap.get("is_deliverable");
        if(invalid==false) {
        	response.getWriter().print("<script>alert('非法邮箱');</script>");
        	return "html/user/reg";
        }
        
		//保存用户信息
		User user=new User();
		user.setEmail(email);
		user.setPassword(password);
		user.setIsActivated(0);
		user.setCommentCount(0);
		user.setMessagesCount(0);
		user.setName(request.getParameter("name"));
		request.getSession().setAttribute("user", user);
		
		try {
			userRepository.save(user);
		}catch(Exception e) {
			e.printStackTrace();
			response.getWriter().printf("<script>alert('注册失败');</script>");
			return "html/user/reg";
		}
		Cookie emailCookie=new Cookie("email",user.getEmail());
		emailCookie.setMaxAge(60*60*24);
		response.addCookie(emailCookie);
		Cookie passCookie=new Cookie("encPass",String.valueOf(user.getPassword().hashCode()));
		passCookie.setMaxAge(60*60*24);
		response.addCookie(passCookie);
		
		response.getWriter().printf("<script>alert('注册成功');</script>");
		model.addAttribute("user", user);
		return "html/index";
	}
	
	@GetMapping("/userCenter")
	public String userCenter(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
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
			if(realUser==null) {
				response.getWriter().printf("<script>alert('用户%s不存在，请先登录');</script>",email);
				return "html/user/login";
			}
			if(encPass.equals(String.valueOf(realUser.getPassword().hashCode()))) {
				request.getSession().setAttribute("user", realUser);
			}
		}
		
		User user=(User) request.getSession().getAttribute("user");
		if(user==null || !email.equals(user.getEmail()) || !encPass.equals(String.valueOf(user.getPassword().hashCode()))){
			response.getWriter().print("<script>alert('请先登录');</script>");
			return "html/user/login";
		}
		
		model.addAttribute("user", user);
		if(user.getIsActivated()==null) {
			user.setIsActivated(0);
			userRepository.save(user);
			return "html/user/activate";
		}
		if(user.getIsActivated()==1)
			return "html/user/index";
		else 
			return "html/user/activate";
	}
	
	@GetMapping("/userSet")
	public String userSet(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return  "html/user/login";
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
		return "html/user/set";
	}
	
	@PostMapping("/userUpdate")
	public String userUpdate(HttpServletRequest request,HttpServletResponse response,Model model) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		Enumeration<String>res=request.getParameterNames();
		while(res.hasMoreElements()) {
			String key=res.nextElement();
			System.out.println(key+";"+request.getParameter(key));
		}
		
		Cookie[]cookies=request.getCookies();
		if(cookies==null) {
			response.getWriter().print("<script>alert('请先登录');</script>");
			return  "html/user/login";
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
		
		String type=request.getParameter("type");
		if(type.equals("0")) {
			user.setName(request.getParameter("username"));
			user.setSex(request.getParameter("sex").equals("0")?0:1);
			user.setAddress(request.getParameter("city"));
			user.setSign(request.getParameter("sign"));
		}else if(type.equals("1")) {
			user.setImageSrc(request.getParameter("avatar"));
		}else if(type.equals("2")){
			//修改密码
		}
		
		try {
			userRepository.save(user);
			model.addAttribute("user", user);
			return "redirect:/userSet";
		}catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	@ResponseBody
	@RequestMapping("/user_test")
	public String  test() {
		String res="";
		List<User> users=userRepository.findAll();
		for(int i=0;i<users.size();i++) {
			res=res+"<br>"+users.get(i).getName()+" : "+users.get(i).getPassword()+"</br>";
		}
		return res;
	}
}
