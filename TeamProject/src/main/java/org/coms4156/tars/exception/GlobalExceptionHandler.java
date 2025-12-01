package org.coms4156.tars.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Centralized exception handling producing standardized ApiError responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles domain {@link NotFoundException} producing a 404 error payload.
   *
   * @param ex      thrown not-found exception
   * @param request originating HTTP servlet request
   * @return ApiError wrapped in 404 response
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
    ApiError err = new ApiError(
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI(),
        null
    );
    return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles domain {@link BadRequestException} producing a 400 error payload.
   *
   * @param ex      thrown bad-request exception
   * @param request originating HTTP servlet request
   * @return ApiError wrapped in 400 response
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(
      BadRequestException ex,
      HttpServletRequest request) {
    ApiError err = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI(),
        null
    );
    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles bean validation failures converting field errors into detail list.
   *
   * @param ex      validation exception
   * @param request originating HTTP servlet request
   * @return ApiError with detail list and 400 status
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    List<String> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .toList();
    ApiError err = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        request.getRequestURI(),
        details
    );
    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles missing required request parameters.
   *
   * @param ex      missing parameter exception
   * @param request originating HTTP servlet request
   * @return ApiError with parameter detail and 400 status
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParam(
      MissingServletRequestParameterException ex,
      HttpServletRequest request) {
    String detail = ex.getParameterName() + " parameter is missing";
    ApiError err = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Required request parameter is missing",
        request.getRequestURI(),
        List.of(detail)
    );
    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles malformed request bodies (e.g., invalid JSON syntax).
   *
   * @param ex      unreadable message exception
   * @param request originating HTTP servlet request
   * @return ApiError describing malformed body with 400 status
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleUnreadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest request) {
    ApiError err = new ApiError(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Malformed JSON request",
        request.getRequestURI(),
        null
    );
    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
  }

  /**
   * Catch-all handler for unanticipated exceptions returning a 500 response.
   *
   * @param ex      generic exception
   * @param request originating HTTP servlet request
   * @return ApiError with original exception message and 500 status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
    ApiError err = new ApiError(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI(),
        null
    );
    return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
