/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.performance.regression.corefeature

import org.gradle.performance.AbstractCrossVersionPerformanceTest
import org.gradle.performance.WithExternalRepository
import org.gradle.performance.fixture.BuildExperimentInvocationInfo
import org.gradle.performance.fixture.BuildExperimentListener
import org.gradle.performance.fixture.BuildExperimentSpec
import org.gradle.performance.fixture.GradleInvocationSpec
import org.gradle.performance.measure.MeasuredOperation

class ExcludeRuleMergingPerformanceTest extends AbstractCrossVersionPerformanceTest implements WithExternalRepository {

    private final static TEST_PROJECT_NAME = 'excludeRuleMergingBuild'

    def setup() {
        runner.minimumVersion = '4.0'
    }

    def "merge exclude rules"() {
        runner.testProject = TEST_PROJECT_NAME
        startServer()

        given:
        runner.tasksToRun = ['resolveDependencies']
        runner.gradleOpts = ["-Xms256m", "-Xmx256m"]
        runner.targetVersions = ["4.6-20180125002142+0000"]
        runner.args = ['-PuseHttp', "-PhttpPort=${serverPort}"]
        runner.addBuildExperimentListener(new BuildExperimentListener() {
            @Override
            void beforeExperiment(BuildExperimentSpec experimentSpec, File projectDir) {
                GradleInvocationSpec invocation = experimentSpec.invocation as GradleInvocationSpec
                if (invocation.gradleDistribution.version.version != '4.6-20180125002142+0000') {
                    invocation.args << '-Dorg.gradle.advancedpomsupport=true'
                }
            }

            @Override
            void beforeInvocation(BuildExperimentInvocationInfo invocationInfo) {

            }

            @Override
            void afterInvocation(BuildExperimentInvocationInfo invocationInfo, MeasuredOperation operation, BuildExperimentListener.MeasurementCallback measurementCallback) {

            }
        })

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        cleanup:
        stopServer()
    }

    def "merge exclude rules (parallel)"() {
        runner.testProject = TEST_PROJECT_NAME
        startServer()

        given:
        runner.tasksToRun = ['resolveDependencies']
        runner.gradleOpts = ["-Xms512m", "-Xmx512m"]
        runner.args = ['-PuseHttp', "-PhttpPort=${serverPort}", "--parallel"]
        runner.targetVersions = ["4.6-20180125002142+0000"]
        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        cleanup:
        stopServer()
    }
}
