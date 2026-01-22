package uk.co.frankz.hmcts.dts.spring.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.spring.Application;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
class ActionCoverageTest {

    private static List<AnnotationMetadata> ANNOTATION_CONTROLLERS;

    @BeforeAll
    static void scanSpring() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        Set<BeanDefinition> controllerClasses = scanner.findCandidateComponents(
            "uk.co.frankz.hmcts.dts.spring.controller");

        System.out.println("Controllers>>>>>>>>>>>>>>>>>>>>>>>");
        controllerClasses.forEach(bean -> System.out.println(bean.getBeanClassName()));
        System.out.println("<<<<<<<<<<<<<<<<<<<<");

        ANNOTATION_CONTROLLERS =
            controllerClasses.stream()
                .filter(bean -> bean instanceof AnnotatedBeanDefinition)
                .map(bean -> (AnnotatedBeanDefinition) bean)
                .map(AnnotatedBeanDefinition::getMetadata)
                .toList();

    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldHaveSpringMappingAnnotation(Action actionPath) {
        // Given
        String pathGiven = actionPath.getPath();

        // when
        List<String> pathsSpringMapping = readSpringControllers();

        // then
        assertNotNull(pathsSpringMapping);
        assertFalse(pathsSpringMapping.isEmpty());
        assertTrue(pathsSpringMapping.contains(pathGiven), pathsSpringMapping.toString());
    }

    private List<String> readSpringControllers() {

        List<String> valuesMappingAttributes = ANNOTATION_CONTROLLERS
            .stream()
            .flatMap(this::getMappingValues)
            .toList();

        return valuesMappingAttributes;
    }

    Stream<String> getMappingValues(AnnotationMetadata beanMetadata) {

        Set<String> values = new HashSet<>();
        values.addAll(getAnnotatedAtttributeValueFieldValue(beanMetadata, GetMapping.class));
        values.addAll(getAnnotatedAtttributeValueFieldValue(beanMetadata, PostMapping.class));
        values.addAll(getAnnotatedAtttributeValueFieldValue(beanMetadata, PutMapping.class));
        values.addAll(getAnnotatedAtttributeValueFieldValue(beanMetadata, DeleteMapping.class));

        return values.stream().filter(Objects::nonNull);
    }

    Collection<String> getAnnotatedAtttributeValueFieldValue(AnnotationMetadata beanMetaData, Class<?> annotation) {

        Set<MethodMetadata> methods = beanMetaData.getAnnotatedMethods(annotation.getName());

        if (CollectionUtils.isNotEmpty(methods)) {

            List<Map<String, Object>> allAttributes = methods
                .stream()
                .map(m -> m.getAnnotationAttributes(annotation.getName()))
                .filter(Objects::nonNull)
                .filter(m -> !m.isEmpty())
                .toList();

            Stream<Object> fieldValueValues = allAttributes
                .stream()
                .map(m -> m.get("value"))
                .filter(Objects::nonNull);

            return fieldValueValues
                .filter(obj -> obj.getClass().isArray())
                .filter(obj -> obj.getClass().getComponentType().equals(String.class))
                .map(obj -> (String[]) obj)
                .flatMap(Arrays::stream)
                .toList();

        } else {
            return Collections.emptySet();
        }

    }

}
