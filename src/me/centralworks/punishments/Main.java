package me.centralworks.punishments;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.centralworks.punishments.cmds.*;
import me.centralworks.punishments.db.dao.AddressIPDAO;
import me.centralworks.punishments.db.dao.PunishmentDAO;
import me.centralworks.punishments.db.dao.WarnDAO;
import me.centralworks.punishments.lib.Date;
import me.centralworks.punishments.listeners.ChatListener;
import me.centralworks.punishments.listeners.MuteQuitListener;
import me.centralworks.punishments.listeners.RegisterAddressListener;
import me.centralworks.punishments.listeners.withAddressIP.MuteIPChatListener;
import me.centralworks.punishments.listeners.withAddressIP.join.MuteIPListener;
import me.centralworks.punishments.listeners.withAddressIP.join.OfflineBanIPListener;
import me.centralworks.punishments.listeners.withAddressIP.join.OnlineBanIPListener;
import me.centralworks.punishments.listeners.withoutAddressIP.MuteChatListener;
import me.centralworks.punishments.listeners.withoutAddressIP.join.MuteListener;
import me.centralworks.punishments.listeners.withoutAddressIP.join.OfflineBanListener;
import me.centralworks.punishments.listeners.withoutAddressIP.join.OnlineBanListener;
import me.centralworks.punishments.punishs.supliers.PunishmentReason;
import me.centralworks.punishments.punishs.supliers.cached.Reasons;
import me.centralworks.punishments.punishs.supliers.enums.PunishmentType;
import me.centralworks.punishments.warns.WarnPunishment;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main extends Plugin {

    protected static Main instance;
    protected static Configuration configuration;
    protected static Configuration messages;
    protected static Configuration usages;
    protected static boolean onlineMode;
    protected static Gson gson;
    protected static List<WarnPunishment> wps;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration getMessages() {
        return messages;
    }

    public static Configuration getUsages() {
        return usages;
    }

    public static boolean isOnlineMode() {
        return onlineMode;
    }

    public static Main getInstance() {
        return instance;
    }

    public static List<WarnPunishment> getWps() {
        return wps;
    }

    public static Gson getGson() {
        return gson;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected Configuration getConfiguration(String fileName) {
        try {
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdir();
            }
            final File file = new File(this.getDataFolder(), fileName);
            if (!file.exists()) {
                Files.copy(this.getResourceAsStream(fileName), file.toPath());
            }
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.getDataFolder(), fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void reasons() {
        final List<PunishmentReason> list = Lists.newArrayList();
        getConfiguration().getSection("Reasons").getKeys().forEach(s -> {
            final String path = "Reasons." + s + ".";
            final PunishmentReason pr = new PunishmentReason();
            pr.setReason(getConfiguration().getString(path + "reason"));
            pr.setPermission(getConfiguration().getString(path + "permission"));
            pr.setWithIP(getConfiguration().getBoolean(path + "ip"));
            pr.setPunishmentType(PunishmentType.valueOf(getConfiguration().getString(path + "type")));
            if (pr.getPunishmentType().isTemp()) {
                pr.setDuration(Date.getInstance().convertPunishmentDuration(Lists.newArrayList(getConfiguration().getString(path + "duration").split(","))));
            } else pr.setPermanent(true);
            list.add(pr);
        });
        Reasons.getInstance().setReasons(list);
    }

    protected void wps() {
        final List<WarnPunishment> wp = Lists.newArrayList();
        for (String warn : getConfiguration().getSection("Warns").getKeys()) {
            final WarnPunishment wpo = new WarnPunishment();
            wpo.setId(warn);
            wpo.setAmount(getConfiguration().getInt("Warns." + warn + ".warns"));
            wpo.setCommand(getConfiguration().getString("Warns." + warn + ".command"));
            wp.add(wpo);
        }
        wps = wp;
    }

    protected void registerCommand(Command command) {
        getProxy().getPluginManager().registerCommand(instance, command);
    }

    protected void registerListener(Listener listener) {
        getProxy().getPluginManager().registerListener(instance, listener);
    }

    @Override
    public void onEnable() {
        instance = this;
        onlineMode = getProxy().getConfig().isOnlineMode();
        configuration = getConfiguration("config.yml");
        messages = getConfiguration("messages.yml");
        usages = getConfiguration("usages.yml");
        gson = new Gson();
        reasons();
        wps();
        registerCommand(new CmdBan());
        registerCommand(new CmdTempBan());
        registerCommand(new CmdMute());
        registerCommand(new CmdTempMute());
        registerCommand(new CmdTempBanIP());
        registerCommand(new CmdBanIP());
        registerCommand(new CmdMuteIP());
        registerCommand(new CmdTempMuteIP());
        registerCommand(new CmdUnban());
        registerCommand(new CmdUnmute());
        registerCommand(new CmdUnpunish());
        registerCommand(new CmdPunish());
        registerCommand(new CmdPunishView());
        registerCommand(new CmdHistory());
        registerCommand(new CmdKick());
        registerCommand(new CmdTempWarn());
        registerCommand(new CmdWarn());
        registerCommand(new CmdUnwarn());
        registerCommand(new CmdWarns());
        registerListener(new ChatListener());
        registerListener(new MuteListener());
        registerListener(new MuteIPListener());
        registerListener(new MuteChatListener());
        registerListener(new MuteIPChatListener());
        registerListener(new RegisterAddressListener());
        registerListener(new MuteQuitListener());
        if (Main.isOnlineMode()) {
            registerListener(new OnlineBanListener());
            registerListener(new OnlineBanIPListener());
        } else {
            registerListener(new OfflineBanListener());
            registerListener(new OfflineBanIPListener());
        }
        PunishmentDAO.getInstance().createTable();
        final AddressIPDAO adr = AddressIPDAO.getInstance();
        adr.createTable();
        adr.loadAll();
        WarnDAO.getInstance().createTable();
    }

    @Override
    public void onDisable() {
        AddressIPDAO.getInstance().saveAll();
    }
}
