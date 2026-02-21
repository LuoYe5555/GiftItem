package lyen;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GiftPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // 注册命令处理器
        if (getCommand("gift") != null) {
            getCommand("gift").setExecutor(new GiftCommandExecutor());
        } else {
            getLogger().severe("命令 /gift 未在 plugin.yml 中定义！");
        }

        getLogger().info("礼物插件已启用 (Minecraft 1.21+ / Java 21)");
    }

    @Override
    public void onDisable() {
        getLogger().info("礼物插件已关闭");
    }

    // 命令执行器类
    private static class GiftCommandExecutor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "用法: /gift all 或 /gift one");
                return true;
            }

            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }

            if (args[0].equalsIgnoreCase("all")) {
                // 只允许 OP 执行
                if (!player.isOp()) {
                    player.sendMessage(ChatColor.RED + "只有管理员才能使用此命令！");
                    return true;
                }

                // 获取玩家手中的物品
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand == null || itemInHand.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "您的手中没有物品！");
                    return true;
                }

                // 修复：安全地获取物品名称
                final String itemName = getSafeItemName(itemInHand);

                // 发送给所有在线玩家
                for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                    if (onlinePlayer != player) {
                        onlinePlayer.getInventory().addItem(itemInHand.clone()); // 添加克隆以避免引用问题
                        sendCenteredMessage(onlinePlayer, "您获得了:", itemName);
                    }
                }

                // 向发送者反馈
                player.sendMessage(ChatColor.GREEN + "已将物品发送给所有人！");
                return true;

            } else if (args[0].equalsIgnoreCase("one")) {
                // 随机选择一名在线玩家
                List<Player> onlinePlayers = new ArrayList<>(player.getServer().getOnlinePlayers());
                if (onlinePlayers.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "服务器上没有其他玩家！");
                    return true;
                }

                Random random = new Random();
                Player randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));

                // 获取玩家手中的物品
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand == null || itemInHand.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "您的手中没有物品！");
                    return true;
                }

                // 修复：安全地获取物品名称
                final String itemName = getSafeItemName(itemInHand);

                // 发送给随机玩家
                randomPlayer.getInventory().addItem(itemInHand.clone()); // 添加克隆以避免引用问题
                sendCenteredMessage(randomPlayer, "恭喜您中奖了", itemName);

                // 向发送者反馈
                player.sendMessage(ChatColor.GREEN + "已将物品发送给一位幸运玩家！");
                return true;

            } else {
                sender.sendMessage(ChatColor.RED + "用法: /gift all 或 /gift one");
                return true;
            }
        }

        // 安全获取物品名称的方法
        private String getSafeItemName(ItemStack item) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName(); // 返回自定义名称
            } else {
                // 返回原版英文名（type.name），并将其转换为更友好的格式（可选）
                // 例如：DIAMOND_SWORD -> Diamond Sword
                String rawName = item.getType().name();
                // 简单的转换逻辑（将下划线替换为空格并首字母大写）
                String friendlyName = java.util.Arrays.stream(rawName.split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .reduce((a, b) -> a + " " + b)
                        .orElse(rawName);
                return friendlyName;
            }
        }

        // 发送居中消息
        private void sendCenteredMessage(Player player, String line1, String line2) {
            int screenWidth = 60; // 假设终端宽度为60
            String centeredLine1 = centerText(line1, screenWidth);
            String centeredLine2 = centerText(line2, screenWidth);

            player.sendMessage(centeredLine1);
            player.sendMessage(centeredLine2);
        }

        // 居中文本
        private String centerText(String text, int width) {
            int padding = (width - text.length()) / 2;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                sb.append(" ");
            }
            sb.append(text);
            return sb.toString();
        }
    }
}