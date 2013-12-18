/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dashboard.test;

import java.io.File;
import java.util.Collection;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ShrinkWrapHelper {

    public static JavaArchive createJavaArchive() {
        File rootDir = MavenProjectHelper.getRootDir();
        if (rootDir == null) throw new NullPointerException("Root directory not found");
        return createJavaArchive(rootDir);
    }

    public static JavaArchive createJavaArchive(File root) {
        Collection<String> javaPackages = MavenProjectHelper.getJavaPackages(root);
        JavaArchive arch = ShrinkWrap.create(JavaArchive.class);
        for (String javaPackage : javaPackages) arch.addPackage(javaPackage);
        return arch;
    }
}
