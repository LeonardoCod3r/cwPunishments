package me.centralworks.modules.reports;

import com.google.gson.Gson;
import me.centralworks.Main;
import me.centralworks.modules.reports.cmds.CmdReport;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;

import java.util.List;

public class ReportPlugin {

    protected static ReportPlugin instance;
    protected static Configuration configuration;
    protected static Configuration usages;
    protected static Configuration messages;
    protected static List<String> reasons;
    protected static Gson gson;

    public ReportPlugin() {
        instance = this;
        configuration = Main.getConfiguration("config.yml", "reports", "/reports/");
        messages = Main.getConfiguration("messages.yml", "reports", "/reports/");
        usages = Main.getConfiguration("usages.yml", "reports", "/reports/");
        gson = new Gson();
        reasons = getConfiguration().getStringList("Reasons");
        registerCommand(new CmdReport());
    }

    protected void registerCommand(Command command) {
        Main.getInstance().getProxy().getPluginManager().registerCommand(Main.getInstance(), command);
    }

    protected void registerListener(Listener listener) {
        Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), listener);
    }

    public static List<String> getReasons() {
        return reasons;
    }

    public static ReportPlugin getInstance() {
        return instance;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration getUsages() {
        return usages;
    }

    public static Configuration getMessages() {
        return messages;
    }

    public static Gson getGson() {
        return gson;
    }
}