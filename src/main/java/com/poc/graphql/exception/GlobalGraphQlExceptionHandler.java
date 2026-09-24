package com.poc.graphql.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalGraphQlExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalGraphQlExceptionHandler.class);

    @GraphQlExceptionHandler
    public GraphQLError handleNotFound(ResourceNotFoundException ex, DataFetchingEnvironment env) {
        return error(env, ErrorType.NOT_FOUND, ex.getMessage());
    }

    @GraphQlExceptionHandler
    public GraphQLError handleDuplicate(DuplicateResourceException ex, DataFetchingEnvironment env) {
        return error(env, ErrorType.BAD_REQUEST, ex.getMessage());
    }

    @GraphQlExceptionHandler
    public GraphQLError handleValidation(ConstraintViolationException ex, DataFetchingEnvironment env) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(", "));
        return error(env, ErrorType.BAD_REQUEST, message);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleOther(Exception ex, DataFetchingEnvironment env) {
        log.error("Unexpected error in GraphQL request", ex);
        return error(env, ErrorType.INTERNAL_ERROR, "Something went wrong");
    }

    private GraphQLError error(DataFetchingEnvironment env, ErrorType type, String message) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(message)
                .build();
    }
}
