package com.bhe.bHRank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BHRank extends JavaPlugin implements TabExecutor {

    private File playerDataFile;
    private FileConfiguration playerData;
    private static BHRank instance;
    private int maxTitleLength; // 从配置中读取的最大长度

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();
        reloadConfig();

        // 从配置中读取最大称号长度
        maxTitleLength = getConfig().getInt("max-title-length", 15);
        getLogger().info("最大称号长度设置为: " + maxTitleLength + " 字符");

        // 初始化玩家数据
        loadPlayerData();

        // 注册命令
        getCommand("br").setExecutor(this);
        getCommand("br").setTabCompleter(this);
        getLogger().info("BHRank v4.2.0 已启用");
    }

    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("BHRank 已禁用");
    }

    private void loadPlayerData() {
        playerDataFile = new File(getDataFolder(), "player_data.yml");
        if (!playerDataFile.exists()) {
            saveResource("player_data.yml", false);
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    private void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("保存玩家数据失败: " + e.getMessage());
        }
    }

    // 提供给扩展使用的公共方法
    public String getPlayerTitle(UUID uuid) {
        String title = playerData.getString(uuid.toString());
        return title != null ? title : "";
    }

    public static BHRank getInstance() {
        return instance;
    }

    private void setPlayerTitle(UUID uuid, String title) {
        playerData.set(uuid.toString(), title);
        savePlayerData();
    }

    private void removePlayerTitle(UUID uuid) {
        playerData.set(uuid.toString(), null);
        savePlayerData();
    }

    // 去除颜色代码计算实际可见长度
    private int getVisibleLength(String text) {
        if (text == null || text.isEmpty()) return 0;

        // 去除所有颜色代码
        String stripped = ChatColor.stripColor(text.replace('&', '§'));

        // 计算实际可见字符数
        return stripped.length();
    }

    // 检查称号长度是否有效（包括两边的[]），忽略颜色代码
    private boolean isValidTitleLength(String title) {
        return getVisibleLength(title) <= maxTitleLength;
    }

    // 获取称号剩余可用长度
    private int getAvailableTitleLength(String title) {
        int currentLength = getVisibleLength(title);
        return maxTitleLength - currentLength;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "set":
                return handleSetCommand(sender, args);
            case "clear": // 新增 clear 命令
                return handleClearCommand(sender);
            case "info":
                return handleInfoCommand(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /br give <玩家名> <头衔>");
            sender.sendMessage("§e注意：头衔可见长度不能超过" + maxTitleLength + "个字符（包括两边的[]）");
            sender.sendMessage("§e颜色代码不计入长度限制");
            return false;
        }

        if (!sender.hasPermission("bherank.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c找不到玩家: " + args[1]);
            return true;
        }

        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        // 检查称号长度（忽略颜色代码）
        if (!isValidTitleLength(title)) {
            int visibleLength = getVisibleLength(title);
            int availableLength = getAvailableTitleLength(title);

            sender.sendMessage("§c错误：头衔可见长度不能超过" + maxTitleLength + "个字符（包括两边的[]）");
            sender.sendMessage("§c实际可见长度: " + visibleLength + " 字符");
            sender.sendMessage("§c可用长度: " + availableLength + " 字符");
            sender.sendMessage("§e提示：颜色代码不计入长度限制");
            return true;
        }

        setPlayerTitle(target.getUniqueId(), title);

        sender.sendMessage("§a已为玩家 " + target.getName() + " 设置头衔: " + title);
        target.sendMessage("§a你的新头衔已设置: " + title);
        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /br remove <玩家名>");
            return false;
        }

        if (!sender.hasPermission("bherank.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c找不到玩家: " + args[1]);
            return true;
        }

        removePlayerTitle(target.getUniqueId());
        sender.sendMessage("§a已移除玩家 " + target.getName() + " 的头衔");
        target.sendMessage("§a你的头衔已被移除");
        return true;
    }

    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("bherank.vip")) {
            player.sendMessage("§c你需要VIP权限才能使用此命令!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /br set <头衔>");
            player.sendMessage("§e注意：头衔可见长度不能超过" + maxTitleLength + "个字符（包括两边的[]）");
            player.sendMessage("§e颜色代码不计入长度限制");
            return false;
        }

        String title = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // 检查称号长度（忽略颜色代码）
        if (!isValidTitleLength(title)) {
            int visibleLength = getVisibleLength(title);
            int availableLength = getAvailableTitleLength(title);

            player.sendMessage("§c错误：头衔可见长度不能超过" + maxTitleLength + "个字符（包括两边的[]）");
            player.sendMessage("§c实际可见长度: " + visibleLength + " 字符");
            player.sendMessage("§c可用长度: " + availableLength + " 字符");
            player.sendMessage("§e提示：颜色代码不计入长度限制");
            return true;
        }

        setPlayerTitle(player.getUniqueId(), title);
        player.sendMessage("§a你的新头衔已设置: " + title);
        return true;
    }

    // 新增：处理 clear 命令（VIP玩家清除自己的称号）
    private boolean handleClearCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("bherank.vip")) {
            player.sendMessage("§c你需要VIP权限才能使用此命令!");
            return true;
        }

        // 检查玩家是否有称号
        String currentTitle = getPlayerTitle(player.getUniqueId());
        if (currentTitle == null || currentTitle.isEmpty()) {
            player.sendMessage("§c你当前没有设置任何称号");
            return true;
        }

        // 清除称号
        removePlayerTitle(player.getUniqueId());
        player.sendMessage("§a你的称号已被清除");
        return true;
    }

    // 处理 info 指令
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bherank.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /br info <玩家名>");
            return false;
        }

        // 获取玩家对象（支持离线玩家）
        String playerName = args[1];
        UUID targetUuid = null;

        // 先尝试在线玩家
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            targetUuid = onlinePlayer.getUniqueId();
        } else {
            // 尝试通过离线玩家获取
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                    targetUuid = offlinePlayer.getUniqueId();
                    break;
                }
            }
        }

        if (targetUuid == null) {
            sender.sendMessage("§c找不到玩家: " + playerName);
            return true;
        }

        // 获取玩家称号
        String title = getPlayerTitle(targetUuid);

        if (title == null || title.isEmpty()) {
            sender.sendMessage("§6玩家 §e" + playerName + " §6没有设置称号");
        } else {
            // 计算可见长度
            int visibleLength = getVisibleLength(title);
            int rawLength = title.length();

            // 构建信息消息
            String message = "§6玩家 §e" + playerName + " §6的称号是: §f" + title + "\n" +
                    "§7可见长度: §a" + visibleLength + "§7/§c" + maxTitleLength + " §7字符\n" +
                    "§7原始长度: §e" + rawLength + " §7字符";

            sender.sendMessage(message);
        }

        return true;
    }

    // 根据玩家权限动态显示帮助
    private void sendHelp(CommandSender sender) {
        boolean hasUsePermission = sender.hasPermission("bherank.use");
        boolean hasVipPermission = sender.hasPermission("bherank.vip");

        // 如果没有任何权限，显示充值提示
        if (!hasUsePermission && !hasVipPermission) {
            sender.sendMessage("§6===== BhRank 称号系统 =====");
            sender.sendMessage("§c你没有使用此插件的权限");
            sender.sendMessage("§e想要设置个人称号？请充值§6VIP§e或§dSVIP§e！");
            sender.sendMessage("§e联系管理员或访问商店获取更多信息");
            return;
        }

        sender.sendMessage("§6===== BhRank 帮助 =====");

        // 管理员命令
        if (hasUsePermission) {
            sender.sendMessage("§e/br give <玩家> <头衔>§7 - 给玩家设置头衔");
            sender.sendMessage("§e/br remove <玩家>§7 - 移除玩家的头衔");
            sender.sendMessage("§e/br info <玩家>§7 - 查询玩家头衔信息");
        }

        // VIP命令
        if (hasVipPermission) {
            sender.sendMessage("§e/br set <头衔>§7 - 设置自己的头衔");
            sender.sendMessage("§e/br clear§7 - 清除自己的称号"); // 新增 clear 帮助
        }

        // 通用命令
        sender.sendMessage("§e/br help§7 - 显示帮助信息");
        sender.sendMessage("§6注意：§e头衔可见长度不能超过" + maxTitleLength + "个字符（包括两边的[]）");
        sender.sendMessage("§6颜色代码不计入长度限制");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> commands = new ArrayList<>();

            // 根据权限添加可用命令
            if (sender.hasPermission("bherank.use")) {
                commands.add("give");
                commands.add("remove");
                commands.add("info");
            }
            if (sender.hasPermission("bherank.vip")) {
                commands.add("set");
                commands.add("clear"); // 新增 clear 补全
            }
            commands.add("help");

            return commands;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") ||
                    args[0].equalsIgnoreCase("remove") ||
                    args[0].equalsIgnoreCase("info")) {

                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
