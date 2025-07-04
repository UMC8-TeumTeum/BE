package umc.teumteum.server.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {


  //1. 스프링 예외 처리 (@Valid, 파라미터 오류, 타입 오류)

  //파라미터에 대한 제약 조건 위반
  @ExceptionHandler
  public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
    String message = e.getConstraintViolations().stream()
        .map(violation -> violation.getMessage())
        .findFirst()
        .orElse("잘못된 요청입니다.");

    return handleExceptionInternalFalse(
        e,
        ErrorStatus._BAD_REQUEST,
        HttpHeaders.EMPTY,
        ErrorStatus._BAD_REQUEST.getHttpStatus(),
        request,
        message
    );
  }

  //Exception	DTO 유효성 검사 실패
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error -> {
      String field = error.getField();
      String message = Optional.ofNullable(error.getDefaultMessage()).orElse("");
      errors.merge(field, message, (existing, current) -> existing + ", " + current);
    });

    return handleExceptionInternalArgs(ex, headers, ErrorStatus._BAD_REQUEST, request, errors);
  }

  //지원하지 않는 HTTP 메서드 사용
  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        String.format(
            "지원하지 않는 HTTP 메서드 입니다.'%s'는 사용할 수 없으며, 다음 메서드만 허용됩니다: %s",
            ex.getMethod(), Arrays.toString(ex.getSupportedMethods()));

    //    return super.handleHttpRequestMethodNotSupported(ex, headers, status, request);
    return handleExceptionInternalFalse(
        ex, ErrorStatus._BAD_REQUEST, headers, HttpStatus.METHOD_NOT_ALLOWED, request, message);
  }

  //지원하지 않는 Content-Type
  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        String.format(
            "지원하지 않는 미디어 타입입니다. '%s'는 사용할 수 없으며, 다음 타입만 허용됩니다: %s",
            ex.getContentType(), ex.getSupportedMediaTypes());

    //    return super.handleHttpMediaTypeNotSupported(ex, headers, status, request);
    return handleExceptionInternalFalse(
        ex, ErrorStatus._BAD_REQUEST, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, message);
  }

  //@RequestParam 누락 - 필수 파라미터 빠짐
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message = String.format("필수 파라미터가 누락되었습니다. '%s' 파라미터는 필수입니다.", ex.getParameterName());

    return handleExceptionInternalFalse(
        ex, ErrorStatus._BAD_REQUEST, headers, HttpStatus.BAD_REQUEST, request, message);
    //    return super.handleMissingServletRequestParameter(ex, headers, status, request);
  }

  //@RequestPart 누락 - multipart 요청에서 필수 파트 누락
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        String.format("필수 request part가 누락되었습니다. '%s' 부분은 필수입니다.", ex.getRequestPartName());

    return handleExceptionInternalFalse(
        ex, ErrorStatus._BAD_REQUEST, headers, HttpStatus.BAD_REQUEST, request, message);
    //    return super.handleMissingServletRequestPart(ex, headers, status, request);
  }

  //@PathVariable 누락	- URI 경로 변수 빠짐
  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      MissingPathVariableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message = String.format("필수 경로 변수가 누락되었습니다. '%s' 변수는 필수 입니다.", ex.getVariableName());
    //    return super.handleMissingPathVariable(ex, headers, status, request);
    return handleExceptionInternalFalse(
        ex, ErrorStatus._BAD_REQUEST, headers, HttpStatus.BAD_REQUEST, request, message);
  }

  //위에서 처리하지 않은 모든 예외 (서버 에러)
  @ExceptionHandler
  public ResponseEntity<Object> exception(Exception e, WebRequest request) {
    e.printStackTrace();

    return handleExceptionInternalFalse(
        e,
        ErrorStatus._INTERNAL_SERVER_ERROR,
        HttpHeaders.EMPTY,
        ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),
        request,
        e.getMessage());
  }






  //2. 비즈니스 로직에서 발생하는 커스텀 예외 처리
  @ExceptionHandler(value = GeneralException.class)
  public ResponseEntity onThrowException(
      GeneralException generalException, HttpServletRequest request) {
    ErrorReasonDto errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
    return handleExceptionInternal(generalException, errorReasonHttpStatus, null, request);
  }





  //3. 예외 응답 생성을 위한 내부 메서드들 - Dto 반환

  //ErrorReasonDto 기반 예외 응답 생성 (GeneralException 전용)
  private ResponseEntity<Object> handleExceptionInternal(
      Exception e, ErrorReasonDto reason, HttpHeaders headers, HttpServletRequest request) {
    ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);

    WebRequest webRequest = new ServletWebRequest(request);
    return super.handleExceptionInternal(e, body, headers, reason.getHttpStatus(), webRequest);
  }

  //메시지(에러포인트)를 포함하는 커스텀 에러 응답 생성 - 예: 잘못된 입력 파라미터 설명 등
  private ResponseEntity<Object> handleExceptionInternalFalse(
      Exception e,
      ErrorStatus errorCommonStatus,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request,
      String errorPoint) {

    ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorPoint);

    return super.handleExceptionInternal(e, body, headers, status, request);
  }

  //필드별 오류 메시지를 Map으로 전달하는 응답 생성 - 예: @Valid 실패 시 필드 단위 오류
  private ResponseEntity<Object> handleExceptionInternalArgs(
      Exception e,
      HttpHeaders headers,
      ErrorStatus errorCommonStatus,
      WebRequest request,
      Map<String, String> errorArgs) {
    ApiResponse<Object> body =
        ApiResponse.onFailure(
            errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorArgs);
    return super.handleExceptionInternal(
        e, body, headers, errorCommonStatus.getHttpStatus(), request);
  }

  //ConstraintViolation 예외 전용 응답 생성
  private ResponseEntity<Object> handleExceptionInternalConstraint(
      Exception e, ErrorStatus errorCommonStatus, HttpHeaders headers, WebRequest request) {
    ApiResponse<Object> body =
        ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
    return super.handleExceptionInternal(
        e, body, headers, errorCommonStatus.getHttpStatus(), request);
  }

}
