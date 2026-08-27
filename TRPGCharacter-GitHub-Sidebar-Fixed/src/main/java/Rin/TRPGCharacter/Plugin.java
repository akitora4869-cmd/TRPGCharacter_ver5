package Rin.TRPGCharacter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class Plugin extends JavaPlugin {

    private CharacterManager characterManager;
    private SkillManager skillManager;
    private RollManager rollManager;
    private InputManager inputManager;
    private BookManager bookManager;
    private SidebarManager sidebarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        characterManager = new CharacterManager(this);
        skillManager = new SkillManager(this, characterManager);
        rollManager = new RollManager(this);
        inputManager = new InputManager(this, characterManager, skillManager);
        bookManager = new BookManager(this, characterManager, skillManager);
        sidebarManager = new SidebarManager(this, characterManager);
        sidebarManager.start();

        getServer().getPluginManager().registerEvents(
                new ChatInputListener(this, inputManager), this
        );
        getServer().getPluginManager().registerEvents(
                new BookInteractListener(bookManager), this
        );

        registerCommands();

        getLogger().info("TRPGCharacter enabled!");
    }

    @Override
    public void onDisable() {
        if (characterManager != null) {
            characterManager.save();
        }

        getLogger().info("TRPGCharacter disabled!");
    }

    private void registerCommands() {
        PluginCommand status = getCommand("status");
        PluginCommand roll = getCommand("roll");
        PluginCommand trpgedit = getCommand("trpgedit");
        PluginCommand trpgroll = getCommand("trpgroll");

        if (status != null) {
            status.setExecutor(this::handleStatus);
        }

        if (roll != null) {
            roll.setExecutor(this::handleRoll);
        }

        if (trpgedit != null) {
            trpgedit.setExecutor(this::handleEdit);
        }

        if (trpgroll != null) {
            trpgroll.setExecutor(this::handleSheetRoll);
        }
    }

    private boolean handleStatus(CommandSender sender,
                                 Command command,
                                 String label,
                                 String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます。");
            return true;
        }

        if (args.length == 0) {
            bookManager.openSheet(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            player.getInventory().addItem(bookManager.createSheet(player));
            player.sendMessage(color("&6[TRPG] &a探索者シートの本を渡しました。"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("trpg.admin")) {
                player.sendMessage(color("&c権限がありません。"));
                return true;
            }

            reloadConfig();
            skillManager.reload();
            characterManager.reload();

            player.sendMessage(color("&6[TRPG] &a設定を再読み込みしました。"));
            return true;
        }

        player.sendMessage(color("&e/status &7- 探索者シートを開く"));
        player.sendMessage(color("&e/status give &7- 本を受け取る"));
        if (player.hasPermission("trpg.admin")) {
            player.sendMessage(color("&e/status reload &7- 設定を再読み込み"));
        }
        return true;
    }

    private boolean handleRoll(CommandSender sender,
                               Command command,
                               String label,
                               String[] args) {
        if (args.length != 1) {
            sender.sendMessage(color("&c使い方: /roll <XdY>  例: /roll 1d100"));
            return true;
        }

        rollManager.rollDice(sender, args[0]);
        return true;
    }

    private boolean handleEdit(CommandSender sender,
                               Command command,
                               String label,
                               String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length != 2) {
            return true;
        }

        String type = args[0].toLowerCase(Locale.ROOT);
        String id = args[1];

        if (type.equals("stat")) {
            if (!characterManager.isValidStat(id)) {
                player.sendMessage(color("&c能力値が見つかりません。"));
                return true;
            }

            inputManager.beginStat(player, id.toUpperCase(Locale.ROOT));
            return true;
        }

        if (type.equals("skill")) {
            if (!skillManager.hasSkill(id)) {
                player.sendMessage(color("&c技能が見つかりません。"));
                return true;
            }

            inputManager.beginSkill(player, id);
            return true;
        }

        if (type.equals("name") && id.equalsIgnoreCase("character")) {
            inputManager.beginCharacterName(player);
            return true;
        }

        if (type.equals("san") && id.equalsIgnoreCase("current")) {
            inputManager.beginCurrentSan(player);
            return true;
        }

        if (type.equals("hp") && id.equalsIgnoreCase("current")) {
            inputManager.beginCurrentHp(player);
            return true;
        }
        if (type.equals("hp") && id.equalsIgnoreCase("damage")) {
            inputManager.beginHpDamage(player);
            return true;
        }
        if (type.equals("hp") && id.equalsIgnoreCase("heal")) {
            inputManager.beginHpHeal(player);
            return true;
        }

        if (type.equals("mp") && id.equalsIgnoreCase("current")) {
            inputManager.beginCurrentMp(player);
            return true;
        }
        if (type.equals("mp") && id.equalsIgnoreCase("spend")) {
            inputManager.beginMpSpend(player);
            return true;
        }
        if (type.equals("mp") && id.equalsIgnoreCase("recover")) {
            inputManager.beginMpRecover(player);
            return true;
        }

        if (type.equals("sanloss") && id.equalsIgnoreCase("apply")) {
            inputManager.beginSanLoss(player);
            return true;
        }

        return true;
    }

    private boolean handleSheetRoll(CommandSender sender,
                                    Command command,
                                    String label,
                                    String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length != 2) {
            return true;
        }

        String type = args[0].toLowerCase(Locale.ROOT);
        String id = args[1];

        if (type.equals("stat")) {
            if (!characterManager.isValidStat(id)) {
                return true;
            }

            String stat = id.toUpperCase(Locale.ROOT);
            int target = characterManager.getStat(player, stat) * 5;
            rollManager.rollCheck(player, stat + "×5", target);
            return true;
        }

        if (type.equals("skill")) {
            SkillDefinition skill = skillManager.getSkill(id);
            if (skill == null) {
                return true;
            }

            int target = skillManager.getSkillValue(player, id);
            rollManager.rollCheck(player, skill.getName(), target);
            return true;
        }

        if (type.equals("derived")) {
            int target = characterManager.getDerived(player, id);
            String name = characterManager.getDerivedName(id);
            rollManager.rollCheck(player, name, target);
            return true;
        }

        if (type.equals("san") && id.equalsIgnoreCase("current")) {
            int target = characterManager.getCurrentSan(player);
            rollManager.rollCheck(player, "SANチェック", target);
            return true;
        }

        return true;
    }

    public BookManager getBookManager() {
        return bookManager;
    }

    public SidebarManager getSidebarManager() {
        return sidebarManager;
    }

    private String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
