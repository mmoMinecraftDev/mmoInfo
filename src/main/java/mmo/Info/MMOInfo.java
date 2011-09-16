/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
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

import mmo.Core.events.MMOInfoEvent;
import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mmo.Core.MMO;
import mmo.Core.MMOListener;
import mmo.Core.MMOPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericGradient;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MMOInfo extends MMOPlugin {

	static String config_info = "{name}~mmoMinecraft~{coords}";

	@Override
	public BitSet mmoSupport(BitSet support) {
		support.set(MMO_PLAYER);
		return support;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		MMO.mmoInfo = true;
		pm.registerEvent(Type.CUSTOM_EVENT,
				  new MMOListener() {

					  @Override
					  public void onMMOInfo(MMOInfoEvent event) {
						  if (event.isToken("name")) {
							  Player player = event.getPlayer();
							  event.setWidget(new GenericLabel(MMO.getName(player)).setResize(true).setFixed(true));
						  }
					  }
				  }, Priority.Normal, this);
	}

	@Override
	public void loadConfiguration(Configuration cfg) {
		config_info = cfg.getString("info", config_info);
	}

	@Override
	public void onDisable() {
		MMO.mmoInfo = false;
		super.onDisable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("info")) {
			onSpoutCraftPlayer(SpoutManager.getPlayer((Player) sender));
			return true;
		}
		return false;
	}

	@Override
	public void onSpoutCraftPlayer(SpoutPlayer player) {
		Container left, center, right, current;
		Color back = new Color(0f, 0f, 0f, 0.75f);
		Color bottom = new Color(1f, 1f, 1f, 0.75f);
		Screen screen = player.getMainScreen();
		int height = 10, offset = 1;

		screen.removeWidgets(this); // In case we're re-creating the bar on config change
		screen.attachWidget(plugin, new GenericGradient()
			.setBottomColor(back)
			.setTopColor(back)
			.setMaxWidth(2048)
			.setWidth(2048)
			.setHeight(height + offset)
			.setAnchor(WidgetAnchor.TOP_LEFT)
			.setPriority(RenderPriority.Highest));
		screen.attachWidget(plugin, new GenericGradient()
			.setBottomColor(bottom)
			.setTopColor(bottom)
			.setY(height + offset)
			.setMaxWidth(2048)
			.setWidth(2048)
			.setHeight(1)
			.setAnchor(WidgetAnchor.TOP_LEFT)
			.setPriority(RenderPriority.Highest));
		screen.attachWidget(plugin, new GenericContainer(
			current = left = (Container) new GenericContainer()
				  .setLayout(ContainerType.HORIZONTAL)
				  .setAlign(WidgetAnchor.TOP_LEFT)
				  .setAnchor(WidgetAnchor.TOP_LEFT)
				  .setWidth(427)
				  .setHeight(height)
				  .setY(offset),
			center = (Container) new GenericContainer()
				  .setLayout(ContainerType.HORIZONTAL)
				  .setAlign(WidgetAnchor.TOP_CENTER)
				  .setAnchor(WidgetAnchor.TOP_CENTER)
				  .setWidth(427)
				  .setHeight(height)
				  .setX(-213)
				  .setY(offset),
			right = (Container) new GenericContainer()
				  .setLayout(ContainerType.HORIZONTAL)
				  .setAlign(WidgetAnchor.TOP_RIGHT)
				  .setAnchor(WidgetAnchor.TOP_RIGHT)
				  .setWidth(427)
				  .setHeight(height)
				  .setX(-427)
				  .setY(offset)));

		Matcher match = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(config_info);
		while (match.find()) {
			if (match.group(1) != null) {
				if (current == left) {
					current = center;
				} else if (current == center) {
					current = right;
				} else {
					break;
				}
			} else if (match.group(2) != null) {
				String[] args = MMO.smartSplit(match.group(2));
				MMOInfoEventEvent event = new MMOInfoEventEvent(player, args[0], Arrays.copyOfRange(args, 1, args.length));
				pm.callEvent(event);
				if (!event.isCancelled() && event.widget != null) {
					current.addChild(event.widget.setMargin(0, 3));
				}
			} else {
				String str = match.group().trim();
				if (!str.isEmpty()) {
					current.addChild(new GenericLabel(str).setResize(true).setMargin(0, 3).setFixed(true));
				}
			}
		}
	}

	private class MMOInfoEventEvent extends Event implements MMOInfoEvent {

		Player player;
		String token;
		String[] args;
		Widget widget = null;
		boolean cancelled = false;

		public MMOInfoEventEvent(Player player, String token, String[] args) {
			super("mmoInfoEvent");
			this.player = player;
			this.token = token;
			this.args = args;
		}

		@Override
		public Player getPlayer() {
			return player;
		}

		@Override
		public boolean isToken(String token) {
			return token.equalsIgnoreCase(this.token);
		}

		@Override
		public String[] getArgs() {
			return args.clone();
		}

		@Override
		public void setWidget(Widget widget) {
			this.widget = widget;
		}

		@Override
		public Widget getWidget() {
			return widget;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean cancel) {
			cancelled = cancel;
		}
	}
}
