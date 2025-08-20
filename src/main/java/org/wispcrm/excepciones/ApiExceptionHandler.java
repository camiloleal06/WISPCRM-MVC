package org.wispcrm.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class })
    @ResponseBody
    public ErrorMessage notFoundRequest(Exception exception) {

        return new ErrorMessage(exception);
    }

    /*
     * @ResponseStatus(HttpStatus.BAD_REQUEST)
     * 
     * @ExceptionHandler({ BadRequestException.class,
     * org.springframework.dao.DuplicateKeyException.class,
     * org.springframework.web.bind.MethodArgumentNotValidException.class,
     * org.springframework.http.converter.HttpMessageNotReadableException.class,
     * org.springframework.web.HttpRequestMethodNotSupportedException.class })
     * 
     * @ResponseBody public ErrorMessage badRequest(Exception exception) { return
     * new ErrorMessage(exception); }
     * 
     * @ResponseStatus(HttpStatus.CONFLICT)
     * 
     * @ExceptionHandler({ ConflictException.class })
     * 
     * @ResponseBody public ErrorMessage conflict(Exception exception) { return new
     * ErrorMessage(exception); }
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class, org.springframework.dao.DataIntegrityViolationException.class })
    @ResponseBody
    public ErrorMessage exception(Exception exception) {
        return new ErrorMessage(exception);
    }

}