
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(SecurityManager.class)
@ConditionalOnMissingBean(name = {"securityManager", "java.security.manager"})
@ConditionalOnExpression("#{systemProperties['" + SecurityManagerConfiguration.SECURITY_MANAGER_KEY + "'] == null}")
public class SecurityManagerConfiguration {

  public static final String SECURITY_MANAGER_KEY = "java.security.manager";
  
  @Lazy
  @Bean(name = {"securityManager", SECURITY_MANAGER_KEY})
  public SecurityManager securityManager() {
    SecurityManager securityManager = new SecurityManager();
    
    // Go ahead and set the property in case anyone inspects it...
    System.setProperty(SECURITY_MANAGER_KEY, securityManager.getClass().getName());
    
    return securityManager;
  }
}
