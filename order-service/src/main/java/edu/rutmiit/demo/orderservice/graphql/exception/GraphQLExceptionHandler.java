package edu.rutmiit.demo.orderservice.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
            DataFetcherExceptionHandlerParameters handlerParameters) {

        Throwable exception = handlerParameters.getException();
        ResultPath path = handlerParameters.getPath();
        SourceLocation location = handlerParameters.getSourceLocation();

        GraphQLError error;

        if (exception instanceof IllegalArgumentException) {
            error = GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(exception.getMessage())
                    .path(path)
                    .location(location)
                    .build();
        } else {
            error = GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message("Внутренняя ошибка сервера: " + exception.getMessage())
                    .path(path)
                    .location(location)
                    .build();
        }

        return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                        .error(error)
                        .build()
        );
    }
}