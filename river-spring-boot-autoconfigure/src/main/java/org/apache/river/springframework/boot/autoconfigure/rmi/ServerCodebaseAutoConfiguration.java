
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.springframework.boot.autoconfigure.rmi;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnResource(resources = {"classpath:lib-dl"})
public class ServerCodebaseAutoConfiguration extends WebMvcConfigurerAdapter implements InitializingBean {

  public static final String RMI_SERVER_CODEBASE_KEY = "java.rmi.server.codebase";

  @Autowired(required = true)
  private ServerProperties serverProperties;

  @Value("classpath:lib-dl/*.jar")
  private Resource[] codebaseResources;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/lib-dl/**").addResourceLocations("classpath:lib-dl/");
  }

  @Bean
  public String serverScheme() {
    String serverScheme = "http";
    if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
      serverScheme += "s";
    }
    return serverScheme;
  }

  @Bean
  public InetAddress serverAddress() throws UnknownHostException {
    InetAddress serverAddress = serverProperties.getAddress();
    if (serverAddress == null) {
      serverAddress = InetAddress.getLocalHost();
    }
    return serverAddress;
  }

  @Bean
  public int serverPort() {
    Integer serverPort = serverProperties.getPort();
    if (serverPort == null || serverPort <= 0) {
      if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
        serverPort = 443;
      } else {
        serverPort = 80;
      }
    }
    return serverPort;
  }

  @Bean
  public String serverContextPath() {
    String serverContextPath = serverProperties.getContextPath();
    if (serverContextPath != null && serverContextPath.endsWith("/")) {
      serverContextPath = serverContextPath.substring(0, serverContextPath.length() - 1);
    }
    return serverContextPath;
  }

  @Bean
  public URI serverBaseUri() throws UnknownHostException {
    StringBuilder serverBaseUriBuilder = new StringBuilder();
    serverBaseUriBuilder.append(serverScheme());
    serverBaseUriBuilder.append("://");
    serverBaseUriBuilder.append(serverAddress().getHostAddress());
    serverBaseUriBuilder.append(":");
    serverBaseUriBuilder.append(serverPort());
    if (serverContextPath() != null) {
      if (!serverContextPath().startsWith("/")) {
        serverBaseUriBuilder.append("/");
      }
      serverBaseUriBuilder.append(serverContextPath());
    }
    return URI.create(serverBaseUriBuilder.toString());
  }

  @Bean
  public URI[] serverCodebaseUris() throws UnknownHostException {
    URI[] serverCodebaseUris = new URI[codebaseResources.length];
    for (int i = 0; i < codebaseResources.length; ++i) {
      serverCodebaseUris[i] = URI.create(serverBaseUri() + "/lib-dl/" + codebaseResources[i].getFilename());
    }
    return serverCodebaseUris;
  }
  
  @Bean
  public String serverCodebase() throws UnknownHostException {
    StringBuilder serverCodebaseBuilder = new StringBuilder(System.getProperty(RMI_SERVER_CODEBASE_KEY, ""));
    for (URI serverCodebaseUri : serverCodebaseUris()) {
      serverCodebaseBuilder.append(" ");
      serverCodebaseBuilder.append(serverCodebaseUri);
    }
    return serverCodebaseBuilder.toString().trim();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    System.setProperty(RMI_SERVER_CODEBASE_KEY, serverCodebase());
  }
}
