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

package com.vaadin.tests.tb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Base class for tests which should be run on all supported browsers. The test
 * is automatically launched for multiple browsers in parallel by the test
 * runner.
 * 
 * Sub classes can, but typically should not, restrict the browsers used by
 * implementing a
 * 
 * <pre>
 * &#064;Parameters
 * public static Collection&lt;DesiredCapabilities&gt; getBrowsersForTest() {
 * }
 * </pre>
 * 
 * @author Vaadin Ltd
 */
public abstract class MultiBrowserTest extends PrivateTB3Configuration {

    static List<DesiredCapabilities> allBrowsers = new ArrayList<DesiredCapabilities>();
    static {
        allBrowsers.add(BrowserUtil.ie(8));
        allBrowsers.add(BrowserUtil.ie(9));
        allBrowsers.add(BrowserUtil.ie(10));
        allBrowsers.add(BrowserUtil.ie(11));
        allBrowsers.add(BrowserUtil.firefox(24));
        // Uncomment once we have the capability to run on Safari 6
        // allBrowsers.add(safari(6));
        allBrowsers.add(BrowserUtil.chrome(29));
        allBrowsers.add(BrowserUtil.opera(12));

    }

    /**
     * @return all supported browsers which are actively tested
     */
    public static List<DesiredCapabilities> getAllBrowsers() {
        return allBrowsers;
    }

    @Override
    public Collection<DesiredCapabilities> getBrowsersToTest() {
        return allBrowsers;
    }

}
