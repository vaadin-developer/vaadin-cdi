/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rapidpm.vaadin.addon.cdi;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to create {@link CdiVaadinServletService}.
 * <p>
 * An instance of this servlet is automatically deployed by
 * {@link CdiServletDeployer} if no VaadinServlet is deployed based on web.xml or
 * Servlet 3.0 annotations. A subclass of this servlet and of
 * {@link CdiVaadinServletService} can be used and explicitly deployed
 * to customize it, in which case
 * {@link #createServletService(DeploymentConfiguration)} must call
 * service.init() .
 */

public class CdiVaadinServlet extends VaadinServlet {
  @Inject private BeanManager beanManager;

  private static final ThreadLocal<String> SERVLET_NAME = new ThreadLocal<>();

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    try {
      SERVLET_NAME.set(servletConfig.getServletName());
      super.init(servletConfig);
    } finally {
      SERVLET_NAME.set(null);
    }
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      SERVLET_NAME.set(getServletName());
      super.service(request, response);
    } finally {
      SERVLET_NAME.set(null);
    }
  }

  /**
   * Name of the Vaadin servlet for the current thread.
   * <p>
   * Until VaadinService appears in CurrentInstance,
   * it have to be used to get the servlet name.
   * <p>
   * This method is meant for internal use only.
   *
   * @return currently processing vaadin servlet name
   * @see VaadinServlet#getCurrent()
   */
  public static String getCurrentServletName() {
    return SERVLET_NAME.get();
  }

  @Override
  protected VaadinServletService createServletService(
      DeploymentConfiguration configuration) throws ServiceException {
    final CdiVaadinServletService service =
        new CdiVaadinServletService(this, configuration, beanManager);
    service.init();
    return service;
  }

}
