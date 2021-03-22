package org.javaunit.autoparams;

import static java.util.Arrays.stream;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

final class AutoArgumentsProvider implements ArgumentsProvider,
    AnnotationConsumer<AutoSource> {

    private static final Stream<Arguments> EMPTY = stream(new Arguments[0]);

    private static final ObjectGenerator PRIMITIVE_VALUE_GENERATOR = new CompositeObjectGenerator(
        new BooleanGenerator(), new ByteGenerator(), new IntegerGenerator(), new LongGenerator(),
        new FloatGenerator(), new DoubleGenerator(), new CharacterGenerator());

    private static final ObjectGenerator SIMPLE_VALUE_OBJECT_GENERATOR =
        new CompositeObjectGenerator(new BigIntegerGenerator(), new BigDecimalGenerator(),
            new StringGenerator(), new UuidGenerator(), new EnumGenerator(),
            new DateAndTimeGenerator());

    private static final ObjectGenerator COLLECTION_GENERATOR = new CompositeObjectGenerator(
        new ArrayGenerator(), new CollectionGenerator(),
        new MapGenerator(), new SetGenerator());

    private static final ObjectGenerator STREAM_GENERATOR = new CompositeObjectGenerator(
        new IntStreamGenerator(), new LongStreamGenerator(), new DoubleStreamGenerator(),
        new StreamGenerator());

    public static final CompositeObjectGenerator DEFAULT_OBJECT_GENERATOR =
        new CompositeObjectGenerator(PRIMITIVE_VALUE_GENERATOR, SIMPLE_VALUE_OBJECT_GENERATOR,
            COLLECTION_GENERATOR, STREAM_GENERATOR, new BuilderGenerator(),
            new ComplexObjectGenerator());

    private final ObjectGenerationContext context;
    private int repeat;

    public AutoArgumentsProvider() {
        this(new ObjectGenerationContext(DEFAULT_OBJECT_GENERATOR));
    }

    private AutoArgumentsProvider(ObjectGenerationContext context) {
        this.context = context;
        this.repeat = 1;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return context.getTestMethod().map(this::generate).orElse(EMPTY);
    }

    private Stream<Arguments> generate(Method method) {
        Arguments[] streamSource = new Arguments[repeat];
        for (int i = 0; i < streamSource.length; i++) {
            streamSource[i] = createArguments(method);
        }

        return stream(streamSource);
    }

    private Arguments createArguments(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = stream(parameters).map(this::createArgument).toArray();
        return Arguments.of(arguments);
    }

    private Object createArgument(Parameter parameter) {
        ObjectQuery query = ObjectQuery.create(parameter);
        return context.generate(query);
    }

    @Override
    public void accept(AutoSource annotation) {
        repeat = annotation.repeat();
    }

}
