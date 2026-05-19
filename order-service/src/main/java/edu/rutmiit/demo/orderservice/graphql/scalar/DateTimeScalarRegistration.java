package edu.rutmiit.demo.orderservice.graphql.scalar;

import graphql.scalars.ExtendedScalars;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

/**
 * Регистрируем скаляры DateTime и Date в runtime wiring.
 * ExtendedScalars — коллекция готовых скаляров от graphql-java.
 */
@Component
public class DateTimeScalarRegistration implements RuntimeWiringConfigurer {

    @Override
    public void configure(RuntimeWiring.Builder builder) {
        builder
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Date);
    }
}