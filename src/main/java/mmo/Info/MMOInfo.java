/*
 * This file is part of mmoInfo <http://github.com/mmoMinecraftDev/mmoInfo>,
 * which is part of mmoMinecraft <http://github.com/mmoMinecraftDev>.
 *
 * mmoInfo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Info;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.InfoAPI.MMOInfoEvent;
import mmo.Core.MMO;
import mmo.Core.MMOPlugin;
import mmo.Core.MMOPlugin.Support;
import mmo.Core.util.EnumBitSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.gui.*;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MMOInfo extends MMOPlugin implements Listener {

	static String config_info = "{name}~mmoMinecraft~{compass}{coords}";
	private static final int HEIGHT = 10;
	private static final int OFFSET = 1;
	private static final int WIDTH = 427;

	@Override
	public EnumBitSet mmoSupport(final EnumBitSet support) {
		support.set(Support.MMO_AUTO_EXTRACT);
		return support;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void loadConfiguration(final FileConfiguration cfg) {
		config_info = cfg.getString("info", config_info);
	}

	@EventHandler
	public void onMMOHUD(final MMOHUDEvent event) {
		final Player player = event.getPlayer();
		if ((player.hasPermission("mmo.info.display")) && ((event.getAnchor() == WidgetAnchor.TOP_LEFT) || (event.getAnchor() == WidgetAnchor.TOP_CENTER) || (event.getAnchor() == WidgetAnchor.TOP_RIGHT))) {
			event.setOffsetY(event.getOffsetY() + this.HEIGHT + this.OFFSET + 1);
		}
	}

	@EventHandler
	public void onMMOInfo(final MMOInfoEvent event) {
		if (event.isToken("name")) {
			final SpoutPlayer player = event.getPlayer();
			event.setWidget(this.plugin, new GenericLabel(MMO.getName(player)).setResize(true).setFixed(true));
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		boolean result = false;
		if (command.getName().equalsIgnoreCase("info")) {
			onSpoutCraftPlayer(SpoutManager.getPlayer((Player) sender));
			result = true;
		}
		return result;
	}

	@EventHandler
	public void onSpoutcraftEnable(final SpoutCraftEnableEvent e) {
		onSpoutCraftPlayer(e.getPlayer());
	}

	public void onSpoutCraftPlayer(final SpoutPlayer player) {
		if (!player.hasPermission("mmo.info.display")) {
			return;
		}

		final Color back = new Color(0.0F, 0.0F, 0.0F, 0.75F);
		final Color bottom = new Color(1.0F, 1.0F, 1.0F, 0.75F);
		final Screen screen = player.getMainScreen();

		final Gradient background = (Gradient) new GenericGradient()//
				.setBottomColor(back)//
				.setTopColor(back)//
				.setMaxWidth(2048)//
				.setX(0)//
				.setY(0)//
				.setWidth(2048)//
				.setHeight(MMOInfo.HEIGHT + MMOInfo.OFFSET)//
				.setAnchor(WidgetAnchor.TOP_LEFT)//
				.setPriority(RenderPriority.Highest);
		final Gradient border = (Gradient) new GenericGradient()//
				.setBottomColor(bottom)//
				.setTopColor(bottom)//
				.setX(0)//
				.setY(MMOInfo.HEIGHT + MMOInfo.OFFSET)//
				.setMaxWidth(2048)//
				.setWidth(2048)//
				.setHeight(1)//
				.setAnchor(WidgetAnchor.TOP_LEFT)//
				.setPriority(RenderPriority.Highest);
		final Container left = (Container) new GenericContainer()//
				.setLayout(ContainerType.HORIZONTAL)//
				.setAlign(WidgetAnchor.TOP_LEFT)//
				.setAnchor(WidgetAnchor.TOP_LEFT)//
				.setWidth(MMOInfo.WIDTH)//
				.setHeight(MMOInfo.HEIGHT)//
				.setX(0)//
				.setY(MMOInfo.OFFSET);
		final Container center = (Container) new GenericContainer()//
				.setLayout(ContainerType.HORIZONTAL)//
				.setAlign(WidgetAnchor.TOP_CENTER)//
				.setAnchor(WidgetAnchor.TOP_CENTER)//
				.setWidth(MMOInfo.WIDTH)//
				.setHeight(MMOInfo.HEIGHT)//
				.setX(-(MMOInfo.WIDTH / 2))//
				.setY(MMOInfo.OFFSET);
		final Container right = (Container) new GenericContainer()//
				.setLayout(ContainerType.HORIZONTAL)//
				.setAlign(WidgetAnchor.TOP_RIGHT)//
				.setAnchor(WidgetAnchor.TOP_RIGHT)//
				.setWidth(MMOInfo.WIDTH)//
				.setHeight(MMOInfo.HEIGHT)//
				.setX(-MMOInfo.WIDTH)//
				.setY(MMOInfo.OFFSET);
		screen.removeWidgets(this);
		screen.attachWidget(this.plugin, background);
		screen.attachWidget(this.plugin, border);
		screen.attachWidget(this.plugin, left);
		screen.attachWidget(this.plugin, center);
		screen.attachWidget(this.plugin, right);

		Container current = left;
		final Matcher match = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(config_info);
		while (match.find()) {
			if (match.group(1) != null) {
				if (current.equals(left)) {
					current = center;
				} else if (current.equals(center)) {
					current = right;
				} else {
					break;
				}
			} else if (match.group(2) != null) {
				final String[] args = MMO.smartSplit(match.group(2));
				MMOInfoEventAPI event = new MMOInfoEventAPI(player, args[0], (String[]) Arrays.copyOfRange(args, 1, args.length));
				this.pm.callEvent(event);
				if ((!event.isCancelled()) && (event.getWidget() != null)) {
					final Widget widget = event.getWidget().setMargin(0, 3);
					final Plugin widgetPlugin = event.getPlugin();
					if (event.getIcon() != null) {
						Widget icon = new GenericTexture(event.getIcon()).setMargin(0, 0, 0, 3).setHeight(8).setWidth(8).setFixed(true);
						screen.attachWidget(widgetPlugin, icon);
						current.addChild(icon);
					}
					screen.attachWidget(widgetPlugin, widget);
					current.addChild(widget);
				}
			} else {
				final String str = match.group().trim();
				if (!str.isEmpty()) {
					current.addChild(new GenericLabel(str).setResize(true).setMargin(0, 3).setFixed(true));
				}
			}
		}
	}
}
