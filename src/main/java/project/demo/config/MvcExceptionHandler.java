package project.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Bộ xử lý ngoại lệ toàn cục để đảm bảo mọi lỗi đều được xử lý đúng cách
 * và tránh lỗi ERR_INCOMPLETE_CHUNKED_ENCODING
 */
@ControllerAdvice(basePackages = "project.demo.controller.mvc")
public class MvcExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MvcExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(HttpServletRequest request, Exception ex) {
        logger.error("Exception occurred while handling request: {}", request.getRequestURI(), ex);
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Đã xảy ra lỗi không mong muốn");
        mav.addObject("message", "Hệ thống đang xử lý yêu cầu của bạn không thành công. Vui lòng thử lại sau.");
        mav.addObject("url", request.getRequestURL());
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.setViewName("error/generic-error");
        
        return mav;
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        logger.error("IllegalArgumentException occurred while handling request: {}", request.getRequestURI(), ex);
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Dữ liệu không hợp lệ");
        mav.addObject("message", ex.getMessage());
        mav.addObject("url", request.getRequestURL());
        mav.addObject("status", HttpStatus.BAD_REQUEST.value());
        mav.setViewName("error/generic-error");
        
        return mav;
    }
}