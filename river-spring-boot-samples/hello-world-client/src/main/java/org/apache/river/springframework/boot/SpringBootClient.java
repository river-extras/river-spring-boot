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
package org.apache.river.springframework.boot;

import org.apache.river.examples.hello.api.Greeter;
import java.io.IOException;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.lookup.ServiceDiscoveryManager;
import org.apache.river.springframework.config.SpringBasedConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

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

  @Bean
  Configuration config() throws ConfigurationException {
    return new SpringBasedConfiguration();
  }

  public static void main(final String[] args) throws Throwable {

    ApplicationContext ctx = SpringApplication.run(SpringBootClient.class, args);

    ServiceDiscoveryManager serviceDiscoveryManager = ctx.getBean("serviceDiscoveryManager", ServiceDiscoveryManager.class);
    ServiceItem serviceItem = serviceDiscoveryManager.lookup(new ServiceTemplate(null, new Class[]{Greeter.class}, null), null, Long.MAX_VALUE);
    Greeter server = (Greeter) serviceItem.service;
    System.out.println("Server says: " + server.sayHello("foo"));
  }
}
