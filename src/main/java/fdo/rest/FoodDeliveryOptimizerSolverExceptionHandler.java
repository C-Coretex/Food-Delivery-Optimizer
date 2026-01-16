package fdo.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice

public class FoodDeliveryOptimizerSolverExceptionHandler {
    @ExceptionHandler({FoodDeliveryOptimizerSolverException.class})
    public ResponseEntity<ErrorInfo> handleFoodDeliveryOptimizerSolverException(FoodDeliveryOptimizerSolverException exception) {
        return new ResponseEntity<>(new ErrorInfo(exception.getJobId(), exception.getMessage()), exception.getStatus());
    }
}
