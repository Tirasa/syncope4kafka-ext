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
package org.apache.syncope.client.console.wizards.any;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.tabs.Accordion;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.common.lib.EntityTOUtils;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.AttributableTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.GroupableRelatableTO;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.PlainSchemaTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;

public class PlainAttrs extends AbstractAttrs<PlainSchemaTO> {

    private static final long serialVersionUID = 552437609667518888L;

    public <T extends AnyTO> PlainAttrs(
            final AnyWrapper<T> modelObject,
            final Form<?> form,
            final AjaxWizard.Mode mode,
            final List<String> anyTypeClasses,
            final List<String> whichPlainAttrs) throws IllegalArgumentException {

        super(modelObject, mode, anyTypeClasses, whichPlainAttrs);

        if (modelObject.getInnerObject() instanceof UserTO) {
            fileKey = UserTO.class.cast(modelObject.getInnerObject()).getUsername();
        } else if (modelObject.getInnerObject() instanceof GroupTO) {
            fileKey = GroupTO.class.cast(modelObject.getInnerObject()).getName();
        } else if (modelObject.getInnerObject() instanceof AnyObjectTO) {
            fileKey = AnyObjectTO.class.cast(modelObject.getInnerObject()).getName();
        }

        if (modelObject instanceof UserWrapper) {
            previousObject = UserWrapper.class.cast(modelObject).getPreviousUserTO();
        } else {
            previousObject = null;
        }

        setTitleModel(new ResourceModel("attributes.plain"));

        add(new Accordion("plainSchemas", Collections.<ITab>singletonList(new AbstractTab(
                new ResourceModel("attributes.accordion", "Plain Attributes")) {

            private static final long serialVersionUID = 1037272333056449378L;

            @Override
            public WebMarkupContainer getPanel(final String panelId) {
                return new PlainSchemasOwn(panelId, schemas, attrTOs);
            }
        }), Model.of(0)).setOutputMarkupId(true));

        add(new ListView<MembershipTO>("membershipsPlainSchemas", memberships) {

            private static final long serialVersionUID = 6741044372185745296L;

            @Override
            protected void populateItem(final ListItem<MembershipTO> item) {
                final MembershipTO membershipTO = item.getModelObject();
                item.add(new Accordion("membershipPlainSchemas", Collections.<ITab>singletonList(new AbstractTab(
                        new StringResourceModel(
                                "attributes.membership.accordion",
                                PlainAttrs.this,
                                Model.of(membershipTO))) {

                    private static final long serialVersionUID = 1037272333056449378L;

                    @Override
                    public WebMarkupContainer getPanel(final String panelId) {
                        return new PlainSchemasMemberships(
                                panelId,
                                membershipSchemas.get(membershipTO.getGroupKey()),
                                new LoadableDetachableModel<AttributableTO>() { // SYNCOPE-1439

                            private static final long serialVersionUID = 526768546610546553L;

                            @Override
                            protected AttributableTO load() {
                                return membershipTO;
                            }

                        });
                    }
                }), Model.of(-1)).setOutputMarkupId(true));
            }
        });
    }

    @Override
    protected SchemaType getSchemaType() {
        return SchemaType.PLAIN;
    }

    @Override
    protected boolean reoderSchemas() {
        return super.reoderSchemas() && mode != AjaxWizard.Mode.TEMPLATE;
    }

    @Override
    protected List<AttrTO> getAttrsFromTO() {
        return anyTO.getPlainAttrs().stream().sorted(attrComparator).collect(Collectors.toList());
    }

    @Override
    protected List<AttrTO> getAttrsFromTO(final MembershipTO membershipTO) {
        return membershipTO.getPlainAttrs().stream().sorted(attrComparator).collect(Collectors.toList());
    }

    @Override
    protected void setAttrs() {
        Map<String, AttrTO> attrMap = EntityTOUtils.buildAttrMap(anyTO.getPlainAttrs());

        List<AttrTO> attrs = schemas.values().stream().map(schema -> {
            AttrTO attrTO = new AttrTO();
            attrTO.setSchema(schema.getKey());
            if (attrMap.get(schema.getKey()) == null || attrMap.get(schema.getKey()).getValues().isEmpty()) {
                attrTO.getValues().add("");
            } else {
                attrTO = attrMap.get(schema.getKey());
            }
            return attrTO;
        }).collect(Collectors.toList());

        anyTO.getPlainAttrs().clear();
        anyTO.getPlainAttrs().addAll(attrs);
    }

    @Override
    protected void setAttrs(final MembershipTO membershipTO) {
        Map<String, AttrTO> attrMap = GroupableRelatableTO.class.cast(anyTO).getMembership(membershipTO.getGroupKey()).
                isPresent()
                        ? EntityTOUtils.buildAttrMap(GroupableRelatableTO.class.cast(anyTO).
                                getMembership(membershipTO.getGroupKey()).get().getPlainAttrs())
                        : new HashMap<>();

        List<AttrTO> attrs = membershipSchemas.get(membershipTO.getGroupKey()).values().stream().map(schema -> {
            AttrTO attrTO = new AttrTO();
            attrTO.setSchema(schema.getKey());
            if (attrMap.get(schema.getKey()) == null || attrMap.get(schema.getKey()).getValues().isEmpty()) {
                attrTO.getValues().add(StringUtils.EMPTY);
            } else {
                attrTO.getValues().addAll(attrMap.get(schema.getKey()).getValues());
            }
            return attrTO;
        }).collect(Collectors.toList());

        membershipTO.getPlainAttrs().clear();
        membershipTO.getPlainAttrs().addAll(attrs);
    }

    protected class PlainSchemasOwn extends PlainSchemas<List<AttrTO>> {

        private static final long serialVersionUID = -4730563859116024676L;

        public PlainSchemasOwn(
                final String id,
                final Map<String, PlainSchemaTO> schemas,
                final IModel<List<AttrTO>> attrTOs) {

            super(id, schemas, attrTOs);

            add(new ListView<AttrTO>("schemas", attrTOs) {

                private static final long serialVersionUID = 9101744072914090143L;

                @Override
                protected void populateItem(final ListItem<AttrTO> item) {
                    PlainSchemaTO schema = schemas.get(item.getModelObject().getSchema());
                    setPanel(schemas, item, schema == null ? false : schema.isReadonly());
                }
            });
        }
    }

    protected class PlainSchemasMemberships extends PlainSchemas<AttributableTO> {

        private static final long serialVersionUID = 456754923340249215L;

        public PlainSchemasMemberships(
                final String id,
                final Map<String, PlainSchemaTO> schemas,
                final IModel<AttributableTO> attributableTO) {

            super(id, schemas, attributableTO);

            add(new ListView<AttrTO>("schemas", new ListModel<AttrTO>(attributableTO.getObject().
                    getPlainAttrs().stream().sorted(attrComparator).collect(Collectors.toList()))) {

                private static final long serialVersionUID = 5306618783986001008L;

                @Override
                protected void populateItem(final ListItem<AttrTO> item) {
                    PlainSchemaTO schema = schemas.get(item.getModelObject().getSchema());
                    setPanel(schemas, item, schema == null ? false : schema.isReadonly());
                }
            });
        }
    }
}
