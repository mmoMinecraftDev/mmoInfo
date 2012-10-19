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

import mmo.Core.InfoAPI.MMOInfoEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MMOInfoEventAPI extends MMOInfoEvent implements Cancellable {
	SpoutPlayer player;
	String token;
	String[] args;
	Plugin plugin = null;
	Widget widget = null;
	String icon = null;
	boolean cancelled = false;

	public MMOInfoEventAPI(SpoutPlayer player, String token, String[] args) {
		this.player = player;
		this.token = token;
		this.args = args;
	}

	public SpoutPlayer getPlayer() {
		return this.player;
	}

	public boolean isToken(String token) {
		return token.equalsIgnoreCase(this.token);
	}

	public String[] getArgs() {
		return (String[])this.args.clone();
	}

	public void setWidget(Plugin p, Widget widget) {
		this.widget = widget;
		this.plugin = p;
	}

	public Widget getWidget() {
		return this.widget;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return this.icon;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}