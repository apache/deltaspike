package org.apache.deltaspike.test.core.api.config.injectable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.config.BaseConfigPropertyProducer;

/**
 * Sample producer for {@link org.apache.deltaspike.test.core.api.config.injectable.CustomConfigAnnotationWithMetaData}
 */
@ApplicationScoped
public class CustomConfigAnnotationWithConverterProducer extends BaseConfigPropertyProducer
{

    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaDataWithCustomConverter
    public Integer produceIntegerCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0;
        }

        Integer result = Integer.parseInt(configuredValue);

        CustomConfigAnnotationWithMetaDataWithCustomConverter metaData =
                injectionPoint.getAnnotated().
                        getAnnotation(CustomConfigAnnotationWithMetaDataWithCustomConverter.class);
        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1;
        }

        return result;
    }

    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaDataWithCustomConverter
    public Long produceLongCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0L;
        }

        Long result = Long.parseLong(configuredValue);

        CustomConfigAnnotationWithMetaDataWithCustomConverter metaData =
                injectionPoint.getAnnotated().
                        getAnnotation(CustomConfigAnnotationWithMetaDataWithCustomConverter.class);
        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1L;
        }

        return result;
    }
}
