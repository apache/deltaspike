package org.apache.deltaspike.test.core.api.partialbean.uc008;

import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;

@TestPartialBeanBinding
@ApplicationScoped
public interface PartialBean extends SuperInterface<Object>, SuperInterface2<Object>
{

}
