package edu.rutmiit.demo.orderservice.graphql.scalar;

import graphql.language.StringValue;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class LocalDateTimeScalar {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return builder -> {
            GraphQLScalarType localDateTimeScalar = GraphQLScalarType.newScalar()
                    .name("LocalDateTime")
                    .description("LocalDateTime type")
                    .coercing(new Coercing<LocalDateTime, String>() {

                        @Override
                        public String serialize(Object dataFetcherResult) {
                            // Java LocalDateTime → String для GraphQL ответа
                            return ((LocalDateTime) dataFetcherResult)
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }

                        @Override
                        public LocalDateTime parseValue(Object input) {
                            // String из variables → Java LocalDateTime
                            return LocalDateTime.parse(
                                    (String) input,
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            );
                        }

                        @Override
                        public LocalDateTime parseLiteral(Object input) {
                            // String из query → Java LocalDateTime
                            return LocalDateTime.parse(
                                    ((StringValue) input).getValue(),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            );
                        }
                    })
                    .build();

            builder.scalar(localDateTimeScalar);
        };
    }
}