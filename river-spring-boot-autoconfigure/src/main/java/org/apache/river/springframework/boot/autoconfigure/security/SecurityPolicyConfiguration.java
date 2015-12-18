
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.Policy;
import java.security.URIParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnClass(Policy.class)
@ConditionalOnMissingBean(name = {"securityPolicy", "java.security.policy"})
@ConditionalOnExpression("#{systemProperties['" + SecurityPolicyConfiguration.SECURITY_POLICY_KEY + "'] == null}")
public class SecurityPolicyConfiguration {

  public static final String SECURITY_POLICY_KEY = "java.security.policy";

  @Autowired(required = true)
  private ResourceLoader resourceLoader;
  
  @Lazy
  @Bean(name = {"securityPolicy", SECURITY_POLICY_KEY})
  public Policy securityPolicy() throws NoSuchAlgorithmException, IOException {
    Policy securityPolicy = Policy.getInstance("JavaPolicy", new URIParameter(securityPolicyFile().toUri()));
    
    // Go ahead and set the property in case anyone inspects it...
    System.setProperty(SECURITY_POLICY_KEY, securityPolicyFile().toString());

    return securityPolicy;
  }

  @Lazy
  @Bean
  public Path securityPolicyFile() throws IOException {
    Path securityPolicyFile = Files.createTempFile("default", ".policy");
    securityPolicyFile.toFile().deleteOnExit();
    Files.copy(resourceLoader.getResource("classpath:config/default.policy").getInputStream(), securityPolicyFile, StandardCopyOption.REPLACE_EXISTING);
    return securityPolicyFile;
  }
}
