package org.apache.deltaspike.test.jsf.impl.config.view.navigation.destination.uc005;

import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.View;

public class ViewConfigPreProcessorWithoutValidation extends View.ViewConfigPreProcessor
{
    @Override
    protected void validateViewMetaData(View view, ViewConfigNode viewConfigNode)
    {
        //do nothing to check intentionally ignored basePath usages at folder-nodes
    }
}
