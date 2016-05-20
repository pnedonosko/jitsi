
@Application(defaultController = JitsiMeetApplication.class)
@Portlet(name = "JitsiMeetPortlet")

@Bindings({ @Binding(value = org.exoplatform.jitsi.JitsiMeetService.class),
    @Binding(value = org.exoplatform.services.organization.OrganizationService.class),
    @Binding(value = org.exoplatform.social.core.space.spi.SpaceService.class) })

@Stylesheets({ @Stylesheet(id = "jitsi-meet-app.css", value = "skin/jitsi-meet-app.css", location = AssetLocation.SERVER) })

@Scripts({ @Script(value = "js/jitsi-meet-app.js", depends = { "jitsi-meet-app.css" }, location = AssetLocation.SERVER) })

@Assets("*")

package org.exoplatform.jitsi.portlet.videocall;

import juzu.Application;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
