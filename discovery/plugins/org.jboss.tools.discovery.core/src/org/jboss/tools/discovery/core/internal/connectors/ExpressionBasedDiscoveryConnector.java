/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.discovery.core.internal.connectors;

import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.core.model.ValidationException;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.foundation.core.expressions.ExpressionResolutionException;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver.SystemPropertiesVariableResolver;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;
import org.jboss.tools.foundation.core.properties.IPropertiesProvider;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;

/**
 * A subclass of a connector that resolves ${sysPropertyName:defaultValue}
 * expressions in siteUrl property
 * @author snjeza
 */
public class ExpressionBasedDiscoveryConnector extends DiscoveryConnector {

  @Override
  public void validate() throws ValidationException {
    try {
      IPropertiesProvider propertiesProvider = PropertiesHelper.getPropertiesProvider();
      IVariableResolver variableResolver = new RemotePropertiesResolver(propertiesProvider);
      // Resolve expressions in siteUrl before executing siteUrl validation
      siteUrl = new ExpressionResolver(variableResolver).resolve(siteUrl);
      // validate siteUrl with resolved expression
      super.validate();
    } catch (ExpressionResolutionException e) {
      // Translate runtime exception into validation exception to keep
      // original processing algorithm for cDiscoveryConnectors
      throw new ValidationException(NLS.bind("Resolving expression in URL ''{0}'' failed with error: \"{1}\"", siteUrl,e.getMessage()));
    }
  }

  /**
   * Tries to resolve a variable against system properties (see {@link SystemPropertiesVariableResolver}),
   * Falls back on resolving variables using this installation's {@link IPropertiesProvider}, so basically from remote properties.
   *
   * @since 1.6.0
   */
  //TODO decide to move that resolver back to foundation core or not
  static class RemotePropertiesResolver implements IVariableResolver {

    private IPropertiesProvider propertiesProvider;

    public RemotePropertiesResolver(IPropertiesProvider propertiesProvider) {
      this.propertiesProvider = propertiesProvider;
    }

    @Override
    public String resolve(String variable, String defaultValue) {
      IVariableResolver delegate = new ExpressionResolver.SystemPropertiesVariableResolver();
      // ignoring default value first, as we want to fall back on the
      // propertiesProvider later.
      String result = delegate.resolve(variable, null);
      if (result == null) {
        result = propertiesProvider.getValue(variable, defaultValue);
      }
      return result;
    }
  }

}
