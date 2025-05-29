package project.demo.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for the application.
 * Provides consistent error responses for various exception types.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles BaseException and its subclasses, except AddressException.
     * AddressException is handled by controllers for proper web form responses.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        // Let controllers handle AddressException for web form responses
        if (ex instanceof AddressException) {
            throw ex;
        }
        
        HttpStatus status = resolveStatus(ex);
        ErrorResponse errorResponse = buildErrorResponse(ex.getErrorCode(), ex.getMessage(), status, request.getRequestURI());
        
        logger.error("BaseException: {}", ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles NoSuchElementException.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = buildErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage(), status, request.getRequestURI());
        
        logger.error("NoSuchElementException: {}", ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles validation exceptions.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        if (ex instanceof MethodArgumentNotValidException) {
            ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof BindException) {
            ((BindException) ex).getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = buildErrorResponse("VALIDATION_ERROR", "Validation failed", status, request.getRequestURI());
        errorResponse.setErrors(errors);
        
        logger.error("Validation Exception: {}", errors, ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles MissingServletRequestParameterException.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = buildErrorResponse(
            "MISSING_PARAMETER",
            String.format("Required parameter '%s' is missing", ex.getParameterName()),
            status,
            request.getRequestURI()
        );
        
        logger.error("MissingServletRequestParameterException: {}", ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles MethodArgumentTypeMismatchException.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = buildErrorResponse(
            "INVALID_PARAMETER_TYPE",
            String.format("Parameter '%s' should be of type %s", ex.getName(), ex.getRequiredType().getSimpleName()),
            status,
            request.getRequestURI()
        );
        
        logger.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handles 404 Not Found exceptions.
     * Returns a custom 404 page for browser requests or JSON for API requests.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ModelAndView with the custom 404 page for browsers, or ResponseEntity for APIs
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("404 Not Found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        // Check if this is an API request (accepts JSON) or a browser request (accepts HTML)
        String acceptHeader = request.getHeader("Accept");
        boolean isApiRequest = acceptHeader != null && 
                              (acceptHeader.contains("application/json") || 
                               acceptHeader.contains("application/xml")) &&
                              !acceptHeader.contains("text/html");
        
        if (isApiRequest) {
            // Return JSON response for API requests
            HttpStatus status = HttpStatus.NOT_FOUND;
            ErrorResponse errorResponse = buildErrorResponse(
                "RESOURCE_NOT_FOUND", 
                "The requested endpoint does not exist", 
                status, 
                request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, status);
        } else {
            // Return HTML page for browser requests
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error/404");
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            
            // Thêm thông tin bổ sung cho template nếu cần
            modelAndView.addObject("requestUrl", ex.getRequestURL().toString());
            modelAndView.addObject("method", ex.getHttpMethod());
            modelAndView.addObject("timestamp", LocalDateTime.now());
            
            return modelAndView;
        }
    }
    
    /**
     * Handles NoResourceFoundException (static resource not found).
     * Returns a custom 404 page for browser requests or JSON for API requests.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ModelAndView with the custom 404 page for browsers, or ResponseEntity for APIs
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        logger.warn("No resource found: {}", ex.getResourcePath());
        
        // Check if this is an API request or browser request
        String acceptHeader = request.getHeader("Accept");
        String requestUri = request.getRequestURI();
        
        // Force HTML response for all requests not starting with /api/
        boolean isApiRequest = requestUri != null && requestUri.startsWith("/api/");
        
        if (isApiRequest) {
            // Return JSON response for API requests
            HttpStatus status = HttpStatus.NOT_FOUND;
            ErrorResponse errorResponse = buildErrorResponse(
                "RESOURCE_NOT_FOUND", 
                "The requested resource does not exist", 
                status, 
                request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, status);
        } else {
            // Return HTML page for browser requests
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error/404");
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            
            // Thêm thông tin bổ sung cho template
            modelAndView.addObject("requestUrl", request.getRequestURI());
            modelAndView.addObject("method", request.getMethod());
            modelAndView.addObject("timestamp", LocalDateTime.now());
            
            return modelAndView;
        }
    }
    
    /**
     * Handles all other exceptions.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return a ResponseEntity with an error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = buildErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            status,
            request.getRequestURI()
        );
        
        logger.error("Unexpected Exception: {}", ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Resolves the HTTP status for a BaseException.
     * 
     * @param ex the exception
     * @return the resolved HTTP status
     */
    private HttpStatus resolveStatus(BaseException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof BadRequestException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        } else if (ex instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof BusinessLogicException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (ex instanceof ServiceUnavailableException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Builds an error response.
     * 
     * @param errorCode the error code
     * @param message the error message
     * @param status the HTTP status
     * @param path the request path
     * @return the error response
     */
    private ErrorResponse buildErrorResponse(String errorCode, String message, HttpStatus status, String path) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);
        return errorResponse;
    }
}
