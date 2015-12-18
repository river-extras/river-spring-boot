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
package org.apache.river.springframework.boot.autoconfigure.security;

import java.security.Policy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@EnableConfigurationProperties({RiverSecurityProperties.class})
@Import({SecurityPolicyConfiguration.class, SecurityManagerConfiguration.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RiverSecurityAutoConfiguration implements InitializingBean {

  private RiverSecurityProperties properties;

  @Autowired(required = false)
  private Policy securityPolicy;

  @Autowired(required = false)
  private SecurityManager securityManager;

  @Override
  public void afterPropertiesSet() throws Exception {
    if (securityPolicy != null) {
      Policy.setPolicy(securityPolicy);
    }

    if (securityManager != null) {
      System.setSecurityManager(securityManager);
    }
  }
}
