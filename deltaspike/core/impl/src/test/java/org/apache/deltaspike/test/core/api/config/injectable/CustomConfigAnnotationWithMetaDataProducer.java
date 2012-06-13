package org.apache.deltaspike.test.core.api.config.injectable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.config.BaseConfigPropertyProducer;

/**
 * Sample producer for {@link CustomConfigAnnotationWithMetaData}
 */
@ApplicationScoped
public class CustomConfigAnnotationWithMetaDataProducer extends BaseConfigPropertyProducer
{

    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaData
    public Integer produceIntegerCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0;
        }

        Integer result = Integer.parseInt(configuredValue);

        CustomConfigAnnotationWithMetaData metaData =
                injectionPoint.getAnnotated().getAnnotation(CustomConfigAnnotationWithMetaData.class);
        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1;
        }

        return result;
    }

    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaData
    public Long produceLongCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0L;
        }

        Long result = Long.parseLong(configuredValue);

        CustomConfigAnnotationWithMetaData metaData =
                injectionPoint.getAnnotated().getAnnotation(CustomConfigAnnotationWithMetaData.class);
        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1L;
        }

        return result;
    }
}
