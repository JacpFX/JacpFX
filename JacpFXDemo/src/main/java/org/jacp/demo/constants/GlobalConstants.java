/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ComponentIds.java]
 * AHCP Project http://jacp.googlecode.com
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacp.demo.constants;

public class GlobalConstants {

    public class PerspectiveConstants {
        public static final String DEMO_PERSPECTIVE = "id01";
    }

    public class ComponentConstants {
        public static final String COMPONENT_TREE_VIEW = "id001";
        public static final String COMPONENT_TABLE_VIEW = "id002";
        public static final String COMPONENT_CHART_VIEW = "id003";
        public static final String COMPONENT_DETAIL_VIEW = "id007";
    }

    public class CallbackConstants {
        public static final String CALLBACK_COORDINATOR = "id004";
        public static final String CALLBACK_CREATOR = "id005";
        public static final String CALLBACK_ANALYTICS = "id006";
    }

    public class CSSConstants {
        // CLASSES
        public static final String CLASS_DARK_BORDER = "dark-border";

        // IDs
        public static final String ID_JACP_CUSTOM_TITLE = "jacp-custom-title";
    }

    public static String cascade(String... ids) {
        StringBuilder builder = new StringBuilder();
        for (final String id : ids) {
            if (builder.length() != 0) {
                builder.append(".");
            }
            builder.append(id);
        }
        return builder.toString();
    }

}
