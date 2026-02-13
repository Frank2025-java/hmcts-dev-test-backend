package uk.co.frankz.hmcts.dts.aws.dynamodb;

import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.function.Function;

public class BaseConvertor<T> implements AttributeConverter<T> {

    private final Function<T, String> toString;

    private final Function<String, T> fromString;

    private final Class<T> getType;

    public BaseConvertor(Function<String, T> fromString, Class<T> getType) {
        this(String::valueOf, fromString, getType);
    }

    public BaseConvertor(Function<T, String> toString, Function<String, T> fromString, Class<T> getType) {
        this.toString = toString;
        this.fromString = fromString;
        this.getType = getType;
    }

    @NotNull
    @Override
    public AttributeValue transformFrom(@NotNull T input) {
        return AttributeValue.fromS(toString.apply(input));
    }

    @Override
    public T transformTo(@NotNull AttributeValue input) {
        return fromString.apply(input.s());
    }

    @NotNull
    @Override
    public EnhancedType<T> type() {
        return EnhancedType.of(getType);
    }

    @NotNull
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
