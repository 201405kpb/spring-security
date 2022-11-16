package com.kpb.security.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过@ControllerAdvice注解可以将对于控制器的全局配置放在同一个位置。
 * 注解了@RestControllerAdvice的类的方法可以使用@ExceptionHandler、@InitBinder、@ModelAttribute注解到方法上。
 * &#064;RestControllerAdvice:  注解将作用在所有注解了@RequestMapping的控制器的方法上。
 * &#064;ExceptionHandler：用于指定异常处理方法。当与@RestControllerAdvice配合使用时，用于全局处理控制器里的异常。
 * &#064;InitBinder：用来设置WebDataBinder，用于自动绑定前台请求参数到Model中。
 * &#064;ModelAttribute：本来作用是绑定键值对到Model中，当与@ControllerAdvice配合使用时，可以让全局的@RequestMapping都能获得在此处设置的键值对
 */
@RestControllerAdvice
public class GlobalControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *  全局数据预处理：应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     *  用来设置WebDataBinder
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class,new CustomDateEditor(dateFormat,false));
    }

    /**
     * 全局数据绑定: 应用到所有@RequestMapping注解方法
     * 此处将键值对添加到全局，注解了@RequestMapping的方法都可以获得此键值对
     */
    @ModelAttribute
    public void addAttributes(HttpServletRequest request, Model model) {
        model.addAttribute("request", request);
        model.addAttribute("base", request.getContextPath());
        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
    }

    /**
     *  全局异常处理 :应用到所有@RequestMapping注解的方法，在其抛出Exception异常时执行
     *  定义全局异常处理，value属性可以过滤拦截指定异常，此处拦截所有的Exception
     */
    @ExceptionHandler(value = { Exception.class })
    public Object handleException(HttpServletRequest request, Exception exception) {
        logger.error(exception.getMessage(), exception);

        Map<String, Object> result = new HashMap<>();
        result.put("message", exception.getMessage());
        result.put("requestUrl", request.getServletPath().substring(request.getContextPath().length()));

        return result;
    }

}
