package tech.sebazcrc.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import tech.sebazcrc.deathswap.game.Game;
import tech.sebazcrc.deathswap.game.GamePlayer;
import tech.sebazcrc.deathswap.game.GameState;
import tech.sebazcrc.deathswap.util.RecordUtil;
import tech.sebazcrc.deathswap.util.Utils;
import tech.sebazcrc.eventlib.board.ScoreHelper;

public class SwapListeners implements Listener {

    private static SwapListeners lis;
    private Main instance;

    public SwapListeners() {
        this.instance = Main.getInstance();
        this.instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Game game = instance.getGame();

        if (game.getPlayer(p.getUniqueId()) == null) {
            game.addPlayer(new GamePlayer(p.getUniqueId(), p.getName()));
            e.setJoinMessage(Utils.format("&e¡&b" + p.getName() + " &ees un nuevo jugador en el evento, somos &b" + game.getPlayers().size() + " &ejugadores!"));

            for (Player o : Bukkit.getOnlinePlayers()) {
                o.playSound(o.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }
        } else {
            GamePlayer gp = game.getPlayer(p.getUniqueId());
            if (gp.shouldSetSpectatorMode()) {
                p.setGameMode(GameMode.SPECTATOR);
            } else if (gp.getPosition().getX() != p.getLocation().getX()) {
                p.teleport(gp.getPosition());
                gp.sendMessage("&eHas sido teletransportado a donde deberías estar.", true);
            }
        }

        if (ScoreHelper.hasScore(p)) p.setScoreboard(ScoreHelper.getByPlayer(p).getScoreboard());
        RecordUtil.playRecord(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        try {
            RecordUtil.stopRecord(e.getPlayer());
        } catch (Exception x) {}
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        GamePlayer gp = instance.getGame().getPlayer(p.getUniqueId());

        if (gp == null) return;

        if (instance.getGame().isOnBreak()) e.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player && e.getEntity() instanceof LivingEntity) {
            if (instance.getGame().isOnBreak()) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (instance.getGame().isOnBreak()) {
            e.getPlayer().sendMessage(Utils.format("&cEsta acción está deshabilitada ya que debes esperar unos segundos tras los TP."));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!e.getPlayer().isOp() && instance.getGame().isState(GameState.WAITING)) e.setCancelled(true);

        if (instance.getGame().isOnBreak()) {
            e.getPlayer().sendMessage(Utils.format("&cEsta acción está deshabilitada ya que debes esperar unos segundos tras los TP."));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (instance.getGame().isOnBreak()) {
            e.getPlayer().sendMessage(Utils.format("&cEsta acción está deshabilitada ya que debes esperar unos segundos tras los TP."));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getGameMode() == GameMode.SPECTATOR || instance.getGame().isState(GameState.ENDED) || instance.getGame().isState(GameState.WAITING) || instance.getGame().isOnBreak())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        GamePlayer gp = instance.getGame().getPlayer(p.getUniqueId());

        gp.increaseDeathCount(true);

        boolean keep = gp.getDeathCount() < 3;
        e.setKeepLevel(keep);
        e.setKeepInventory(keep);
        if (keep) e.getDrops().clear();

        Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                p.spigot().respawn();
            }
        }, 3L);
    }


    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (instance.getGame().getPlayer(e.getUniqueId()) == null && !instance.getGame().isState(GameState.WAITING)) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Utils.format("&cLamentablemente, este evento ya ha empezado."));
        }
    }

    public static SwapListeners getInstance() {
        if (lis == null) lis = new SwapListeners();
        return lis;
    }
}
