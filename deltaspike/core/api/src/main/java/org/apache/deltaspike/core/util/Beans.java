package org.apache.deltaspike.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.metadata.builder.ImmutableInjectionPoint;

import static org.apache.deltaspike.core.util.Reflections.isSerializable;

/**
 * A set of utility methods for working with beans.
 *
 * @author Pete Muir
 */
public class Beans {

    private Beans() {
    }

    /**
     * Extract the qualifiers from a set of annotations.
     *
     * @param beanManager the beanManager to use to determine if an annotation is
     *                    a qualifier
     * @param annotations the annotations to check
     * @return any qualifiers present in <code>annotations</code>
     */
    public static Set<Annotation> getQualifiers(BeanManager beanManager, Iterable<Annotation>... annotations) {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        for (Iterable<Annotation> annotationSet : annotations) {
            for (Annotation annotation : annotationSet) {
                if (beanManager.isQualifier(annotation.annotationType())) {
                    qualifiers.add(annotation);
                }
            }
        }
        return qualifiers;
    }

    /**
     * Extract the qualifiers from a set of annotations.
     *
     * @param beanManager the beanManager to use to determine if an annotation is
     *                    a qualifier
     * @param annotations the annotations to check
     * @return any qualifiers present in <code>annotations</code>
     */
    @SuppressWarnings("unchecked")
    public static Set<Annotation> getQualifiers(BeanManager beanManager, Iterable<Annotation> annotations) {
        return getQualifiers(beanManager, new Iterable[]{annotations});
    }

    /**
     * Extract the qualifiers from a set of annotations.
     *
     * @param beanManager the beanManager to use to determine if an annotation is
     *                    a qualifier
     * @param annotations the annotations to check
     * @return any qualifiers present in <code>annotations</code>
     */
    public static Set<Annotation> getQualifiers(BeanManager beanManager, Annotation[]... annotations) {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        for (Annotation[] annotationArray : annotations) {
            for (Annotation annotation : annotationArray) {
                if (beanManager.isQualifier(annotation.annotationType())) {
                    qualifiers.add(annotation);
                }
            }
        }
        return qualifiers;
    }

    public static void checkReturnValue(Object instance, Bean<?> bean, InjectionPoint injectionPoint, BeanManager beanManager) {
        if (instance == null && !Dependent.class.equals(bean.getScope())) {
            throw new IllegalStateException("Cannot return null from a non-dependent producer method: " + bean);
        } else if (instance != null) {
            boolean passivating = beanManager.isPassivatingScope(bean.getScope());
            boolean instanceSerializable = isSerializable(instance.getClass());
            if (passivating && !instanceSerializable) {
                throw new IllegalStateException("Producers cannot declare passivating scope and return a non-serializable class: " + bean);
            }
            if (injectionPoint != null && injectionPoint.getBean() != null) {
                if (!instanceSerializable && beanManager.isPassivatingScope(injectionPoint.getBean().getScope())) {
                    if (injectionPoint.getMember() instanceof Field) {
                        if (!injectionPoint.isTransient() && instance != null && !instanceSerializable) {
                            throw new IllegalStateException("Producers cannot produce non-serializable instances for injection into non-transient fields of passivating beans. Producer " + bean + "at injection point " + injectionPoint);
                        }
                    } else if (injectionPoint.getMember() instanceof Method) {
                        Method method = (Method) injectionPoint.getMember();
                        if (method.isAnnotationPresent(Inject.class)) {
                            throw new IllegalStateException("Producers cannot produce non-serializable instances for injection into parameters of initializers of beans declaring passivating scope. Producer " + bean + "at injection point " + injectionPoint);
                        }
                        if (method.isAnnotationPresent(Produces.class)) {
                            throw new IllegalStateException("Producers cannot produce non-serializable instances for injection into parameters of producer methods declaring passivating scope. Producer " + bean + "at injection point " + injectionPoint);
                        }
                    } else if (injectionPoint.getMember() instanceof Constructor<?>) {
                        throw new IllegalStateException("Producers cannot produce non-serializable instances for injection into parameters of constructors of beans declaring passivating scope. Producer " + bean + "at injection point " + injectionPoint);
                    }
                }
            }
        }
    }

    /**
     * Given a method, and the bean on which the method is declared, create a
     * collection of injection points representing the parameters of the method.
     *
     * @param <X>           the type declaring the method
     * @param method        the method
     * @param declaringBean the bean on which the method is declared
     * @param beanManager   the bean manager to use to create the injection points
     * @return the injection points
     */
    public static <X> List<InjectionPoint> createInjectionPoints(AnnotatedMethod<X> method, Bean<?> declaringBean, BeanManager beanManager) {
        List<InjectionPoint> injectionPoints = new ArrayList<InjectionPoint>();
        for (AnnotatedParameter<X> parameter : method.getParameters()) {
            InjectionPoint injectionPoint = new ImmutableInjectionPoint(parameter, beanManager, declaringBean, false, false);
            injectionPoints.add(injectionPoint);
        }
        return injectionPoints;
    }

}