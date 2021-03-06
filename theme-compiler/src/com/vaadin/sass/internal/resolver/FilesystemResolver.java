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
package com.vaadin.sass.internal.resolver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.w3c.css.sac.InputSource;

import com.vaadin.sass.internal.ScssStylesheet;

public class FilesystemResolver extends AbstractResolver {

    private String[] customPaths = null;

    public FilesystemResolver(String... customPaths) {
        this.customPaths = customPaths;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.sass.internal.resolver.AbstractResolver#getPotentialPaths(
     * com.vaadin.sass.internal.ScssStylesheet, java.lang.String)
     */
    @Override
    protected List<String> getPotentialParentPaths(
            ScssStylesheet parentStyleSheet, String identifier) {
        List<String> potentialPaths = super.getPotentialParentPaths(
                parentStyleSheet, identifier);
        if (customPaths != null) {
            for (String path : customPaths) {
                potentialPaths.add(extractFullPath(path, identifier));
            }
        }

        return potentialPaths;
    }

    @Override
    public InputSource resolveNormalized(String identifier) {
        String fileName = identifier;
        if (!fileName.endsWith(".css")) {
            fileName += ".scss";
        }

        try {
            InputStream is = new FileInputStream(fileName);
            InputSource source = new InputSource();
            source.setByteStream(is);
            source.setURI(fileName);
            return source;

        } catch (FileNotFoundException e) {
            // not found, try something else
            return null;
        }

    }

}
