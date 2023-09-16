package tech.sebazcrc.deathswap.game;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import tech.sebazcrc.deathswap.Main;
import tech.sebazcrc.deathswap.util.Utils;
import tech.sebazcrc.eventlib.library.XSound;

import javax.rmi.CORBA.Util;
import java.util.ArrayList;
import java.util.UUID;

public class GamePlayer {

    private UUID id;
    private String name;
    private Location position;

    private int deathCount;
    private boolean shouldSetSpectatorMode;

    private ArrayList<String> pendingMessages;

    public GamePlayer(UUID id, String name) {
        this.id = id;
        this.name = name;

        this.pendingMessages = new ArrayList<>();
        this.deathCount = 0;
        this.shouldSetSpectatorMode = false;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    public boolean isSpectator() {
        return deathCount >= 3;
    }

    public void addMessage(String key) {
        this.pendingMessages.add(Utils.format(key));
    }

    public void sendPendingMessage() {
        if (pendingMessages.isEmpty()) return;
        Player p = getPlayer();
        if (p != null) {
            for (String msg : pendingMessages) {
                p.sendMessage(msg);
            }
            p.sendMessage(Utils.format("&eTienes &b" + pendingMessages.size() + " &emensajes pendientes."));
            pendingMessages.clear();
        }
    }

    public void sendMessage(String s, boolean prefix) {
        Player p = getPlayer();
        if (prefix) s = Main.prefix + s;

        if (p == null) {
            addMessage(s);
        } else {
            p.sendMessage(Utils.format(s));
        }
    }

    public void sendTitle(String t, String s) {
        sendTitle(t, s, 12, 20*8, 12);
    }

    public void sendTitle(String t, String s, int fadein, int stay, int fadeout) {
        Player p = getPlayer();
        if (p != null) {
            p.sendTitle(Utils.format(t), Utils.format(s), fadein, stay, fadeout);
        }
    }

    public void playSound(XSound sound) {
        playSound(sound, 10.0F, 1.0F);
    }

    public void playSound(XSound sound, Float volume, Float pitch) {
        Player p = getPlayer();
        if (p != null) p.playSound(p.getLocation(), sound.parseSound(), volume, pitch);
    }

    public UUID getUUID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPendingMessages() {
        return pendingMessages;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public void swapPosition(GamePlayer to, org.bukkit.Location tp) {
        Player p = getPlayer();
        sendMessage("&e¡Has cambiado de posición con &b" + to.getName() + "&e!" + (p == null ? "&c(al estar ausente has perdido &e1 &cvida)." : ""), true);

        this.position = tp;
        if (p == null) {
            this.increaseDeathCount(false);
        } else {
            p.teleport(tp);
        }
        playSound(XSound.ENTITY_ENDERMAN_TELEPORT, 10.0F, 1.0F);
    }

    public void increaseDeathCount(boolean death) {
        this.deathCount++;
        if (this.deathCount >= 3) {
            onDeath();
        } else {
            Bukkit.broadcastMessage(Utils.format(Main.prefix + "&b" + getName() + " &eha perdido &b1 &evida" + (death ? "" : " (por estar ausente)") + ", le quedan &b" + getLives() + " &evidas."));
            for (GamePlayer gp : Main.getInstance().getGame().getPlayers()) {
                gp.playSound(XSound.ENTITY_ENDER_DRAGON_GROWL, 10.0F, 10.0F);
            }
        }
    }

    private int getLives() {
        if (deathCount == 0) {
            return 3;
        } else if (deathCount == 1) {
            return 2;
        } else if (deathCount == 2) {
            return 1;
        } else {
            return 0;
        }
    }

    private void onDeath() {
        Player p = getPlayer();
        this.shouldSetSpectatorMode = p == null;

        sendMessage("&c&l¡HAS MUERTO! &7Perdiste todas tus vidas...", true);
        sendTitle("&c&l¡PERDISTE!", "&f¡No te quedan vidas!");

        Bukkit.broadcastMessage(Utils.format(Main.prefix + "&b" + getName() + " &eha perdido todas sus vidas. &cQuedan &e" + Main.getInstance().getGame().getAlivePlayers().size() + " &cjugadores."));
        for (GamePlayer gp : Main.getInstance().getGame().getPlayers()) {
            gp.playSound(XSound.ENTITY_WITHER_DEATH, 10.0F, 1.0F);
        }

        if (p != null) p.setGameMode(GameMode.SPECTATOR);

        Main.getInstance().getGame().searchForEnd();
    }

    public boolean shouldSetSpectatorMode() {
        return shouldSetSpectatorMode;
    }
}
