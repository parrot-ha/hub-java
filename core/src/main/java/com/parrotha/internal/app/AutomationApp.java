/**
 * Copyright (c) 2021-2023 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.internal.app;

import com.parrotha.internal.system.OAuthToken;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutomationApp {
    private String id;
    private String file;
    private String name;
    private String namespace;
    private String author;
    private String description;
    private String category;
    private Boolean installOnOpen;
    private String documentationLink;
    private String iconUrl;
    private String iconX2Url;
    private String iconX3Url;
    private String parent;

    private String oAuthClientId;
    private String oAuthClientSecret;
    private String oAuthRedirectURI;
    private String oAuthDisplayName;
    private String oAuthDisplayLink;

    private List<OAuthToken> oAuthTokens;

    private String extensionId;
    private Type type;
    public enum Type {
        USER,
        SYSTEM,
        EXTENSION,
        EXTENSION_SOURCE;
    }

    /**
     * Check if the 2 AutomationApps are equal but ignore the id field
     */
    public boolean equalsIgnoreId(AutomationApp aa) {
        return equalsIgnoreId(aa, false);
    }

    /**
     * Check if the 2 AutomationApps are equal but ignore the id field
     */
    public boolean equalsIgnoreId(AutomationApp aa, boolean includeOAuthClientIdSecret) {
        if (aa == this) {
            return true;
        }
        if (!StringUtils.equals(file, aa.getFile())) {
            return false;
        }
        if (!StringUtils.equals(name, aa.getName())) {
            return false;
        }
        if (!StringUtils.equals(namespace, aa.getNamespace())) {
            return false;
        }
        if (!StringUtils.equals(author, aa.getAuthor())) {
            return false;
        }
        if (!StringUtils.equals(description, aa.getDescription())) {
            return false;
        }
        if (!StringUtils.equals(category, aa.getCategory())) {
            return false;
        }
        if (BooleanUtils.toBooleanDefaultIfNull(installOnOpen, false) !=
                BooleanUtils.toBooleanDefaultIfNull(aa.installOnOpen, false)) {
            return false;
        }
        if (!StringUtils.equals(documentationLink, aa.getDocumentationLink())) {
            return false;
        }
        if (!StringUtils.equals(iconUrl, aa.getIconUrl())) {
            return false;
        }
        if (!StringUtils.equals(iconX2Url, aa.getIconX2Url())) {
            return false;
        }
        if (!StringUtils.equals(iconX3Url, aa.getIconX3Url())) {
            return false;
        }
        if (!StringUtils.equals(parent, aa.getParent())) {
            return false;
        }
        if (!StringUtils.equals(oAuthDisplayName, aa.getoAuthDisplayName())) {
            return false;
        }
        if (!StringUtils.equals(oAuthDisplayLink, aa.getoAuthDisplayLink())) {
            return false;
        }
        if (type != null) {
            if (!type.equals(aa.getType())) {
                return false;
            }
        } else if (aa.getType() != null) {
            // aa.type is not null but type is null, they are not equal
            return false;
        }
        if (includeOAuthClientIdSecret) {
            if (!StringUtils.equals(oAuthClientId, aa.getoAuthClientId())) {
                return false;
            }
            if (!StringUtils.equals(oAuthClientSecret, aa.getoAuthClientSecret())) {
                return false;
            }
        }
        return true;
    }

    public AutomationApp() {
    }

    public AutomationApp(String id, String file, Map definition) {
        this.id = id;
        this.file = file;
        this.name = getStringValue(definition, "name");
        this.namespace = getStringValue(definition, "namespace");
        this.author = getStringValue(definition, "author");
        this.description = getStringValue(definition, "description");
        this.category = getStringValue(definition, "category");
        this.installOnOpen = (Boolean) definition.get("installOnOpen");
        this.documentationLink = getStringValue(definition, "documentationLink");
        this.iconUrl = getStringValue(definition, "iconUrl");
        this.iconX2Url = getStringValue(definition, "iconX2Url");
        this.iconX3Url = getStringValue(definition, "iconX3Url");
        this.parent = getStringValue(definition, "parent");
        if (definition.get("oauth") instanceof Map) {
            Map oAuthMap = (Map) definition.get("oauth");
            this.oAuthDisplayName = getStringValue(oAuthMap, "displayName");
            this.oAuthDisplayLink = getStringValue(oAuthMap, "displayLink");
            this.oAuthRedirectURI = getStringValue(oAuthMap, "redirectURI");
            this.oAuthClientId = getStringValue(oAuthMap, "clientId");
            this.oAuthClientSecret = getStringValue(oAuthMap, "clientSecret");
        }

        this.extensionId = getStringValue(definition, "extensionId");
        Object typeObj = definition.get("type");
        if (typeObj != null) {
            if (typeObj instanceof String) {
                this.type = Type.valueOf((String) typeObj);
            } else if (typeObj instanceof Type) {
                this.type = (Type) typeObj;
            }
        }
    }

    private String getStringValue(Map definition, String value) {
        if (definition.get(value) != null) {
            return definition.get(value).toString();
        }
        return null;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getInstallOnOpen() {
        return installOnOpen;
    }

    public void setInstallOnOpen(Boolean installOnOpen) {
        this.installOnOpen = installOnOpen;
    }

    public String getDocumentationLink() {
        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {
        this.documentationLink = documentationLink;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconX2Url() {
        return iconX2Url;
    }

    public void setIconX2Url(String iconX2Url) {
        this.iconX2Url = iconX2Url;
    }

    public String getIconX3Url() {
        return iconX3Url;
    }

    public void setIconX3Url(String iconX3Url) {
        this.iconX3Url = iconX3Url;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getoAuthClientId() {
        return oAuthClientId;
    }

    public void setoAuthClientId(String oAuthClientId) {
        this.oAuthClientId = oAuthClientId;
    }

    public String getoAuthClientSecret() {
        return oAuthClientSecret;
    }

    public void setoAuthClientSecret(String oAuthClientSecret) {
        this.oAuthClientSecret = oAuthClientSecret;
    }

    public String getoAuthRedirectURI() {
        return oAuthRedirectURI;
    }

    public void setoAuthRedirectURI(String oAuthRedirectURI) {
        this.oAuthRedirectURI = oAuthRedirectURI;
    }

    public String getoAuthDisplayName() {
        return oAuthDisplayName;
    }

    public void setoAuthDisplayName(String oAuthDisplayName) {
        this.oAuthDisplayName = oAuthDisplayName;
    }

    public String getoAuthDisplayLink() {
        return oAuthDisplayLink;
    }

    public void setoAuthDisplayLink(String oAuthDisplayLink) {
        this.oAuthDisplayLink = oAuthDisplayLink;
    }

    public List<OAuthToken> getoAuthTokens() {
        return oAuthTokens;
    }

    public void setoAuthTokens(List<OAuthToken> oAuthTokens) {
        this.oAuthTokens = oAuthTokens;
    }

    public boolean addoAuthToken(OAuthToken oAuthToken) {
        if (oAuthTokens == null) {
            oAuthTokens = new ArrayList<>();
        }
        return oAuthTokens.add(oAuthToken);
    }

    public String getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Transient
    public boolean isOAuthEnabled() {
        return StringUtils.isNotBlank(oAuthClientId) && StringUtils.isNotBlank(oAuthClientSecret);
    }

    @Override
    public String toString() {
        return "AutomationApp{" +
                "id='" + id + '\'' +
                ", file='" + file + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", installOnOpen=" + installOnOpen +
                ", documentationLink='" + documentationLink + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", iconX2Url='" + iconX2Url + '\'' +
                ", iconX3Url='" + iconX3Url + '\'' +
                ", type='" + type + '\'' +
                ", oAuthClientId='" + oAuthClientId + '\'' +
                ", oAuthClientSecret='" + oAuthClientSecret + '\'' +
                ", oAuthRedirectURI='" + oAuthRedirectURI + '\'' +
                ", oAuthDisplayName='" + oAuthDisplayName + '\'' +
                ", oAuthDisplayLink='" + oAuthDisplayLink + '\'' +
                ", oAuthTokens=" + oAuthTokens +
                '}';
    }
}
