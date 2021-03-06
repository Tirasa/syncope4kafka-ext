/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.flowable.support;

import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.api.UserQuery;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.flowable.idm.engine.impl.IdmIdentityServiceImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SyncopeIdmIdentityService extends IdmIdentityServiceImpl implements ApplicationContextAware {

    private ConfigurableApplicationContext ctx;

    public SyncopeIdmIdentityService(final IdmEngineConfiguration idmEngineConfiguration) {
        super(idmEngineConfiguration);
    }

    @Override
    public void setApplicationContext(final ApplicationContext ctx) throws BeansException {
        this.ctx = (ConfigurableApplicationContext) ctx;
    }

    @Override
    public UserQuery createUserQuery() {
        return (UserQuery) ctx.getBeanFactory().
                createBean(SyncopeUserQueryImpl.class, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, false);
    }

    @Override
    public GroupQuery createGroupQuery() {
        return (GroupQuery) ctx.getBeanFactory().
                createBean(SyncopeGroupQueryImpl.class, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, false);
    }
}
