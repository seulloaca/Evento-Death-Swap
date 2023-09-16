package tech.sebazcrc.deathswap.util;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldEvent;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.*;

public class RecordUtil {
    private RecordUtil() {
    }

    public static void playRecord(Player p) {
        p.playSound(p.getLocation(), Sound.MUSIC_DISC_STAL, SoundCategory.MUSIC, 500.0F, 1.0F);
    }

    public static void stopRecord(Player p) {
        p.stopSound(Sound.MUSIC_DISC_STAL);
    }
}