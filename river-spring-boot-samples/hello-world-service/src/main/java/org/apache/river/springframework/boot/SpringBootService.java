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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.ExportException;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.LookupDiscovery;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.lookup.entry.Name;
import org.apache.river.examples.hello.api.Greeter;
import org.apache.river.examples.hello.impl.GreeterImpl;
import org.apache.river.lookup.ServiceIDHelper;
import org.apache.river.springframework.config.SpringBasedConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootService {

  @Value("${hello-service.groups}")
  String[] groups;

  @Value("${hello-service.name}")
  String name;

  @Bean
  GreeterImpl serviceImpl() {
    return new GreeterImpl();
  }

  @Bean
  Exporter serviceExporter() throws UnknownHostException {
    return new BasicJeriExporter(TcpServerEndpoint.getInstance(String.format("%s.local.", InetAddress.getLocalHost().getHostName()), 0), new BasicILFactory());
  }
  
  @Bean
  Greeter serviceProxy() throws ExportException, UnknownHostException {
    return (Greeter) serviceExporter().export(serviceImpl());
  }
  
  @Bean
  ServiceID serviceId() {
    return ServiceIDHelper.newServiceID();
  }

  @Bean
  LookupDiscovery lookupDiscovery() throws IOException, ConfigurationException {
    return new LookupDiscovery(groups, config());
  }

  @Bean
  JoinManager joinManager() throws IOException, ConfigurationException {
    return new JoinManager(serviceProxy(), new Entry[]{new Name(name)}, serviceId(), lookupDiscovery(), null, config());
  }

  @Bean
  Configuration config() throws ConfigurationException {
    return new SpringBasedConfiguration();
  }

  public static void main(final String[] args) throws Throwable {

    SpringApplication.run(SpringBootService.class, args);
  }
}
