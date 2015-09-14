package org.apache.deltaspike.jsf.api.security;

import javax.faces.component.UIViewRoot;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;

/**
 * Allows the application to programmatically run security checks for a view.
 */
public interface ViewAccessHandler
{

    /**
     * Determines if access is permitted to the view with the given ID.
     * 
     * @param viewId
     *            The ID of the view to check
     * @return True if access is permitted, false if not
     */
    boolean canAccessView(String viewId);

    /**
     * Determines if access is permitted to the view represented by the given
     * {@link UIViewRoot}.
     * 
     * @param viewRoot
     *            The view to check
     * @return True if access is permitted, false if not
     */
    boolean canAccessView(UIViewRoot viewRoot);

    /**
     * Determines if access is permitted to the view with the given descriptor.
     * 
     * @param viewDescriptor
     *            The descriptor of the view to check
     * @return True if access is permitted, false if not
     */
    boolean canAccessView(ViewConfigDescriptor viewDescriptor);
    
    /**
     * Determines if access is permitted to the view represented by the given {@link ViewConfig}.
     * 
     * @param viewConfig
     *            The ViewConfig to check
     * @return True if access is permitted, false if not
     */
    boolean canAccessView(Class<? extends ViewConfig> viewConfig);

    /**
     * Determines if access is permitted to the view with the given ID. An
     * ErrorViewAwareAccessDeniedException will be thrown if access is not
     * permitted.
     * 
     * @param viewId
     *            The ID of the view to check
     */
    void checkAccessToView(String viewId);

    /**
     * Determines if access is permitted to the view represented by the given
     * {@link UIViewRoot}. An ErrorViewAwareAccessDeniedException will be thrown
     * if access is not permitted.
     * 
     * @param viewRoot
     *            The UIViewRoot to check
     */
    void checkAccessToView(UIViewRoot viewRoot);

    /**
     * Determines if access is permitted to the view with the given descriptor.
     * An ErrorViewAwareAccessDeniedException will be thrown if access is not
     * permitted.
     * 
     * @param viewDescriptor
     *            The descriptor of the view to check
     */
    void checkAccessToView(ViewConfigDescriptor viewDescriptor);
    
    /**
     * Determines if access is permitted to the view represented by the given
     * {@link ViewConfig}. An ErrorViewAwareAccessDeniedException will be thrown
     * if access is not permitted.
     * 
     * @param viewConfig
     *            The ViewConfig to check
     */
    void checkAccessToView(Class<? extends ViewConfig> viewConfig);

}
