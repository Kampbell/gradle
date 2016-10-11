/*
 * Copyright 2013 the original author or authors.
 *
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
package org.gradle.language.mc

import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.RequiresInstalledToolChain
import org.gradle.nativeplatform.fixtures.ToolChainRequirement
import org.gradle.nativeplatform.fixtures.app.CppHelloWorldApp
import org.gradle.nativeplatform.fixtures.app.HelloWorldApp
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition

class WindowsMessagesUnsupportedIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {

    HelloWorldApp helloWorldApp = new CppHelloWorldApp()

    @Requires(TestPrecondition.NOT_WINDOWS)
    def "message files are ignored on unsupported platforms"() {
        given:
        buildFile << """
plugins {
    id 'cpp'
    id 'windows-messages'
}

model {
    components {
        main(NativeExecutableSpec)
    }
}
         """

        and:
        helloWorldApp.writeSources(file("src/main"))
        file("src/main/mc/broken.mc") << """
        #include <stdio.h>

        NOT A VALID MESSAGE
"""

        when:
        run "mainExecutable"

        then:
        !executedTasks.contains(":compileMainExecutableMainMc")
    }

    @Requires(TestPrecondition.WINDOWS)
    @RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
    def "reasonable error message when attempting to compile message files with unsupported tool chain"() {
        given:
        buildFile << """
plugins {
    id 'cpp'
    id 'windows-messages'
}

model {
    components {
        main(NativeExecutableSpec)
    }
}
         """

        and:
        helloWorldApp.writeSources(file("src/main"))
        file("src/main/mc/broken.mc") << """
        #include <stdio.h>

        NOT A VALID MESSAGE
"""

        when:
        fails "mainExecutable"

        then:
        failure.assertHasDescription("Execution failed for task ':compileMainExecutableMainMc'.")
        failure.assertHasCause("Windows message compiler is not available")
    }
}

