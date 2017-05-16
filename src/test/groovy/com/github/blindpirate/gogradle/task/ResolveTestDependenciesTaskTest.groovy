/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependencyForTest
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class ResolveTestDependenciesTaskTest extends TaskTest {
    ResolveTestDependenciesTask task

    File resource

    @Mock
    DependencyTreeNode buildDependencyTree

    @Before
    void setUp() {
        task = buildTask(ResolveTestDependenciesTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'test dependencies should remove all that has existed in build dependencies'() {
        // given
        VendorResolvedDependency vendorA = new VendorResolvedDependencyForTest('a',
                'versionA',
                1L,
                gogradleRootProject,
                'vendor/a')
        VendorResolvedDependency vendorB = new VendorResolvedDependencyForTest('b',
                'versionB',
                2L,
                gogradleRootProject,
                'vendor/b')
        VendorResolvedDependency vendorC = new VendorResolvedDependencyForTest('c',
                'versionC',
                3L,
                gogradleRootProject,
                'vendor/a/vendor/c')

        vendorA.dependencies.add(vendorC)
        GolangDependencySet buildDependencies = asGolangDependencySet(vendorA, vendorB, vendorC)

        GolangDependencySet testDependencies = asGolangDependencySet(mockDependency('c'), mockDependency('d'))


        when(getGolangTaskContainer().get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildDependencyTree)
        when(buildDependencyTree.flatten()).thenReturn(buildDependencies)
        when(strategy.produce(gogradleRootProject, resource, visitor, 'test')).thenReturn(testDependencies)
        // when
        task.resolve()
        // then
        ArgumentCaptor captor = ArgumentCaptor.forClass(ResolvedDependency)
        verify(gogradleRootProject).setDependencies(captor.capture())
        assert captor.value.size() == 1
        assert captor.value.first().name == 'd'
    }
}