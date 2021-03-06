/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.integration;

import java.util.Collection;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.tests.tb3.PrivateTB3Configuration;

/**
 * Base class for integration tests. Integration tests use the
 * {@literal deployment.url} parameter to determine the base deployment url
 * (http://hostname:123)
 * 
 * @author Vaadin Ltd
 */
@RunWith(IntegrationTestRunner.class)
public abstract class AbstractIntegrationTest extends
        PrivateTB3Configuration {
    @Override
    protected String getBaseURL() {
        String deploymentUrl = System.getProperty("deployment.url");
        if (deploymentUrl == null || deploymentUrl.equals("")) {
            throw new RuntimeException(
                    "Deployment url must be given as deployment.url");
        }

        return deploymentUrl;
    }

    @Parameters
    public static Collection<DesiredCapabilities> getBrowsersForTest() {
        return Collections.singleton(BrowserUtil.firefox(17));
    }

}
