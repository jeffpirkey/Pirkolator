/*******************************************************************************
 * Copyright 2013 Reality Warp Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.rws.utility.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class AbstractSpringTest {

    public final Logger LOG = LoggerFactory.getLogger(getClass());

    @Before
    public void setupTest() {

        LOG.info("Setting up Spring unit test");
    }

    public void sleep(long time) {

        LOG.info("Sleeping for " + time + "ms");
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // Do nothing
        }
        LOG.info("Done sleeping");
    }
}
