package com.test.controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FileController {
	@ResponseBody
	@RequestMapping("/api/upload/")
	public JSONObject fileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
		Enumeration<String>resu=request.getParameterNames();
		while(resu.hasMoreElements()) {
			String key=resu.nextElement();
			System.out.println(key+";"+request.getParameter(key));
		}
		//上传路径保存设置
	    //获得SpringBoot当前项目的路径：System.getProperty("user.dir")
	    String path = System.getProperty("user.dir")+"/upload/";
	 
	    //按照月份进行分类：
	    Calendar instance = Calendar.getInstance();
	    String month = (instance.get(Calendar.MONTH) + 1)+"月";
	    path = path+month+"/img";
	 
	    File realPath = new File(path);
	    if (!realPath.exists()){
	        realPath.mkdirs();
	    }
	 
	    //上传文件地址
	    //System.out.println("上传文件保存地址："+realPath);
	 
	    //解决文件名字问题：使用uuid
	    String filename = UUID.randomUUID().toString().replaceAll("-", "")+".jpg";
	    File newfile = new File(realPath, filename);
	    //通过CommonsMultipartFile的方法直接写文件（注意这个时候）
	    file.transferTo(newfile);
	 
	    //进行回调
	    JSONObject res = new JSONObject();
	    res.put("status", 0);
        res.put("msg", "upload success!");
        res.put("url","/upload/"+month+"/img/"+ filename);
        System.out.println(res.toString());
	    return res;
	}
	
	@ResponseBody
	@RequestMapping("/user/upload/")
	public JSONObject userUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
		//上传路径保存设置
	    //获得SpringBoot当前项目的路径：System.getProperty("user.dir")
	    String path = System.getProperty("user.dir")+"/upload/";
	    
	    path = path+"/user";
	 
	    File realPath = new File(path);
	    if (!realPath.exists()){
	        realPath.mkdirs();
	    }
	 
	    //上传文件地址
	    //System.out.println("上传文件保存地址："+realPath);
	 
	    //解决文件名字问题：使用uuid
	    String filename = UUID.randomUUID().toString().replaceAll("-", "")+".jpg";
	    File newfile = new File(realPath, filename);
	    //通过CommonsMultipartFile的方法直接写文件（注意这个时候）
	    file.transferTo(newfile);
	 
	    //进行回调
	    JSONObject res = new JSONObject();
	    res.put("status", 0);
        res.put("msg", "upload success!");
        res.put("url","/upload/"+"user/"+ filename);
        System.out.println(res.toString());
	    return res;
	}
	
	@RequestMapping("/test")
	public void test(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		Enumeration<String>res=request.getParameterNames();
		while(res.hasMoreElements()) {
			String key=res.nextElement();
			System.out.println(key+";"+request.getParameter(key));
		}
		System.out.println("this is a test");
	}
}
