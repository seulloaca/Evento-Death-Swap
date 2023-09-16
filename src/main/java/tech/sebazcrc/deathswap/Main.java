package tech.sebazcrc.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tech.sebazcrc.deathswap.game.Game;
import tech.sebazcrc.deathswap.game.GamePlayer;
import tech.sebazcrc.deathswap.game.GameState;
import tech.sebazcrc.deathswap.util.RecordUtil;
import tech.sebazcrc.deathswap.util.Utils;
import tech.sebazcrc.eventlib.library.XSound;

import java.util.SplittableRandom;

public final class Main extends JavaPlugin implements CommandExecutor {

    private static Main instance;
    public SplittableRandom random;

    public static String prefix;
    private Game game;

    @Override
    public void onEnable() {
        instance = this;
        random = new SplittableRandom();

        prefix = Utils.format("&8[&3DeathSwap&8] &f➤ ");

        this.game = new Game();
        SwapListeners.getInstance();

        for (Player p : Bukkit.getOnlinePlayers()) {
            this.game.addPlayer(new GamePlayer(p.getUniqueId(), p.getName()));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("ds")) {
            if (!sender.hasPermission("deathswap.use")) {
                sender.sendMessage("No tienes permisos.");
                return false;
            }

            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("start")) {
                new BukkitRunnable() {
                    private int reaming = 5;

                    @Override
                    public void run() {
                        if (reaming > 0) {
                            for (Player on : Bukkit.getOnlinePlayers()) {
                                on.sendTitle(Utils.format("&6&l" + reaming), Utils.format( "&7¡Prepárate para jugar!"), 1, 20, 1);
                                on.sendMessage(Utils.format("&eLa partida comienza en: &b" + reaming + "&e segundos."));
                                on.playSound(on.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 10.0F);
                            }

                            reaming--;
                            return;
                        }

                        for (GamePlayer on : game.getPlayers()) {
                            if (on.getPlayer() == null) {
                                game.getPlayers().remove(on);
                                return;
                            }
                            on.sendTitle(Utils.format("&a&l¡Buena suerte!"), Utils.format( "&7¡Ha comenzado la partida!"), 8, 20*3, 8);
                            on.sendMessage(Utils.format("&a¡La partida ha comenzado!"), true);
                            on.playSound(XSound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0F, 1.0F);

                            RecordUtil.playRecord(on.getPlayer());
                        }
                        getGame().setState(GameState.PLAYING);
                        this.cancel();
                    }
                }.runTaskTimer(instance, 0L, 20L);
            } else if (args[0].equalsIgnoreCase("debugtime")) {
                game.setTime(60*4 + 50);
            } else if (args[0].equalsIgnoreCase("tpvp")) {
                for (World w : Bukkit.getWorlds()) {
                    w.setPVP(!w.getPVP());
                }
            } else if (args[0].equalsIgnoreCase("playmusic")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    try {
                        RecordUtil.stopRecord(online);
                    } catch (Exception x) {}
                    RecordUtil.playRecord(online);
                }
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Game getGame() {
        return game;
    }

    public static Main getInstance() {
        return instance;
    }
}
