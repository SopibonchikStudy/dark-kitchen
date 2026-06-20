package edu.rutmiit.demo.orderservice.graphql.scalar;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DgsComponent
public class LocalDateTimeScalarRegistration {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @DgsRuntimeWiring
    public RuntimeWiring.Builder addScalars(RuntimeWiring.Builder builder) {
        return builder.scalar(
                GraphQLScalarType.newScalar()
                        .name("LocalDateTime")
                        .description("LocalDateTime scalar")
                        .coercing(new Coercing<LocalDateTime, String>() {

                            @Override
                            public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                                if (dataFetcherResult instanceof LocalDateTime) {
                                    return ((LocalDateTime) dataFetcherResult).format(FORMATTER);
                                }
                                throw new CoercingSerializeException(
                                        "Unable to serialize " + dataFetcherResult + " as LocalDateTime"
                                );
                            }

                            @Override
                            public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                                if (input instanceof String) {
                                    try {
                                        return LocalDateTime.parse((String) input, FORMATTER);
                                    } catch (Exception e) {
                                        throw new CoercingParseValueException(
                                                "Unable to parse value " + input + " as LocalDateTime", e
                                        );
                                    }
                                }
                                throw new CoercingParseValueException(
                                        "Unable to parse value " + input + " as LocalDateTime"
                                );
                            }

                            @Override
                            public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                                if (input instanceof String) {
                                    try {
                                        return LocalDateTime.parse((String) input, FORMATTER);
                                    } catch (Exception e) {
                                        throw new CoercingParseLiteralException(
                                                "Unable to parse literal " + input + " as LocalDateTime", e
                                        );
                                    }
                                }
                                throw new CoercingParseLiteralException(
                                        "Unable to parse literal " + input + " as LocalDateTime"
                                );
                            }
                        })
                        .build()
        );
    }
}