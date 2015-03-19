package org.apache.river.spring.boot;

import com.sun.jini.example.hello.Hello;
import java.io.File;
import java.io.IOException;
import java.net.SocketPermission;
import java.security.Permission;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.security.BasicProxyPreparer;
import net.jini.security.ProxyPreparer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

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
@SpringBootApplication
public class SpringBootClient {

  @Value("${hello-service.groups}")
  String[] groups;

  @Bean
  LookupDiscovery lookupDiscovery() throws IOException, ConfigurationException {
    return new LookupDiscovery(groups, config());
  }

  @Bean
  ServiceDiscoveryManager serviceDiscoveryManager() throws IOException, ConfigurationException {
    return new ServiceDiscoveryManager(lookupDiscovery(), null, config());
  }

  @Bean(name = {"net.jini.discovery.LookupDiscovery.registrarPreparer",
                "net.jini.lookup.ServiceDiscoveryManager.registrarPreparer"})
  ProxyPreparer registrarPreparer() {
    return new BasicProxyPreparer(false, new Permission[]{new SocketPermission("*:1024-", "connect")});
  }

  @Bean
  Configuration config() throws ConfigurationException {
    return ConfigurationProvider.getInstance(new String[] {});
  }

  public static void main(final String[] args) throws Throwable {

    System.out.println("CWD: " + new File(".").getAbsolutePath());

    //System.setProperty("java.security.manager", "");
    System.setProperty("java.security.policy", "src/main/resources/config/client.policy");
    System.setProperty("java.security.properties", "src/main/resources/config/dynamic-policy.security.properties");
    //System.setProperty("java.rmi.server.codebase", "http://localhost:8080/jsk-dl.jar");
    System.setSecurityManager(new SecurityManager());

    ApplicationContext ctx = SpringApplication.run(SpringBootClient.class, args);

    ServiceDiscoveryManager serviceDiscoveryManager = ctx.getBean("serviceDiscoveryManager", ServiceDiscoveryManager.class);
    ServiceItem serviceItem = serviceDiscoveryManager.lookup(new ServiceTemplate(null, new Class[]{Hello.class}, null), null, Long.MAX_VALUE);
    Hello server = (Hello) serviceItem.service;
    System.out.println("Server says: " + server.sayHello());
  }
}
