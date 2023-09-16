package tech.sebazcrc.deathswap.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.sebazcrc.deathswap.Main;
import tech.sebazcrc.deathswap.util.RecordUtil;
import tech.sebazcrc.deathswap.util.Utils;
import tech.sebazcrc.eventlib.board.ScoreHelper;
import tech.sebazcrc.eventlib.board.ScoreStringBuilder;
import tech.sebazcrc.eventlib.library.XSound;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private List<GamePlayer> players;
    private List<GamePlayer> spectators;

    private int round;
    private int time;
    private int breakTime;

    private GameState state;

    // SCOREBOARD
    public Map<Player, Integer> currentSubString;
    private ArrayList<String> lines;

    public Game() {
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.round = 1;
        this.time = 0;
        this.state = GameState.WAITING;

        this.lines = new ArrayList<>();

        lines.add("&6&lSebazCRC Projects");
        lines.add("&e&lS&6&lebazCRC Projects");
        lines.add("&e&lS&6&lebazCRC Projects");
        lines.add("&e&lSe&6&lbazCRC Projects");
        lines.add("&e&lSeb&6&lazCRC Projects");
        lines.add("&e&lSeba&6&lzCRC Projects");
        lines.add("&e&lSebaz&6&lCRC Projects");
        lines.add("&e&lSebazC&6&lRC Projects");
        lines.add("&e&lSebazCR&6&lC Projects");
        lines.add("&e&lSebazCRC &6&lProjects");
        lines.add("&e&lSebazCRC P&6&lrojects");
        lines.add("&e&lSebazCRC Pr&6&lojects");
        lines.add("&e&lSebazCRC Pro&6&ljects");
        lines.add("&e&lSebazCRC Proj&6&lects");
        lines.add("&e&lSebazCRC Proje&6&lcts");
        lines.add("&e&lSebazCRC Projec&6&lts");
        lines.add("&e&lSebazCRC Project&6&ls");
        lines.add("&e&lSebazCRC Projects");

        this.currentSubString = new HashMap<>();

        scheduleTask();
    }

    public void scheduleTask() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for (GamePlayer player : getPlayers()) {
                if (player.getPlayer() != null) {
                    tickScoreboard(player);
                    player.setPosition(player.getPlayer().getLocation());
                }
            }

            if (isState(GameState.PLAYING)) {
                time++;

                tickMusic();
                int module = (time % (5*60));
                int reaming = (5*60) - module;

                if (reaming == 45 || reaming == 30 || reaming == 20 || reaming == 10 || reaming <= 5 || module == 0) {
                    if (module == 0) {
                        round++;
                        swapPositions();
                    } else {
                        Bukkit.broadcastMessage(Utils.format(Main.prefix + "&e¡Se cambiarán posiciones en &b" + reaming + " &esegundo(s)!"));
                        for (GamePlayer player : getPlayers()) {
                            if (player.getPlayer() != null) {
                                player.playSound(XSound.UI_BUTTON_CLICK);
                            }
                        }
                    }
                }

                if (breakTime > 0) breakTime--;
            }
        }, 0L, 20L);
    }

    private void tickMusic() {
        int module = (time % (150));
        int reaming = (150) - module;

        if (module == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                RecordUtil.playRecord(p);
            }
        }
    }

    public void searchForEnd() {
        int alive = getAlivePlayers().size();

        if (alive > 1) {

        } else if (alive == 1) {
            GamePlayer winner = getAlivePlayers().get(0);
            Bukkit.broadcastMessage(Main.prefix + Utils.format("&e¡&b" + winner.getName() + " &eha ganado la partida!"));

            for (GamePlayer gp : getPlayers()) {
                gp.playSound(XSound.BLOCK_NOTE_BLOCK_PLING, 10.0F, 100.0F);
                if (gp.getUUID() != winner.getUUID()) {
                    gp.sendTitle("&c&l¡PERDISTE!", "&7" + winner.getName() + " ha ganado la partida.");
                } else {
                    gp.sendTitle("&a&l¡GG!", "&7Ganaste la partida.");
                }
            }

            this.state = GameState.ENDED;
        } else if (alive == 0) {
            Bukkit.broadcastMessage(Utils.format(Main.prefix + "&e¡Vaya!, al parecer todos murieron a la vez."));
            this.state = GameState.ENDED;
        }
    }

    public void swapPositions() {
        List<GamePlayer> alive = randomizeList(new ArrayList<>(getAlivePlayers()));
        boolean esPar = ((alive.size() % 2) == 0);

        // 0 y 1
        // 2 y 3
        // 4 y 5

        for (int i = 0; i < (esPar ? alive.size() : alive.size()-1); i+=2) {
            GamePlayer first = alive.get(i);
            org.bukkit.Location fp = first.getPosition();

            GamePlayer second = alive.get(i+1);
            org.bukkit.Location sp = second.getPosition();

            first.swapPosition(second, sp);
            second.swapPosition(first, fp);
        }
    }

    private List<GamePlayer> randomizeList(ArrayList<GamePlayer> gamePlayers) {
        int size = gamePlayers.size();
        if (size <= 2) return gamePlayers;

        List<GamePlayer> newList = new ArrayList<>();
        while (newList.size() < size) {
            GamePlayer p = gamePlayers.get(Main.getInstance().random.nextInt(size));
            if (!newList.contains(p)) newList.add(p);
        }
        return newList;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public List<GamePlayer> getSpectators() {
        return getPlayers().stream().filter(GamePlayer::isSpectator).collect(Collectors.toList());
    }

    public List<GamePlayer> getAlivePlayers() {
        return getPlayers().stream().filter(gamePlayer -> !gamePlayer.isSpectator()).collect(Collectors.toList());
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isState(GameState s) {
        return state == s;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GamePlayer getPlayer(UUID id) {
        for (GamePlayer gp : getPlayers()) {
            if (gp.getUUID().toString().equalsIgnoreCase(id.toString())) return gp;
        }
        return null;
    }

    public void addPlayer(GamePlayer gamePlayer) {
        this.players.add(gamePlayer);
    }

    private void tickScoreboard(GamePlayer gp) {
        Player p = gp.getPlayer();
        updateScoreboard(gp);

        if (!currentSubString.containsKey(p)) {
            currentSubString.put(p, 0);
        } else {
            int plus = this.currentSubString.get(p) + 1;
            if (plus > this.lines.size() - 1)
                plus = 0;
            this.currentSubString.replace(p, plus);
        }

        ScoreHelper.getByPlayer(p).setTitle(Utils.format(lines.get(this.currentSubString.get(p))));
    }

    private void createScoreboard(Player p) {
        if (!ScoreHelper.hasScore(p)) ScoreHelper.createScore(p).setTitle("&6&lSebazCRC Projects");
    }

    private void updateScoreboard(GamePlayer gp) {
        Player p = gp.getPlayer();
        createScoreboard(p);

        ScoreHelper helper = ScoreHelper.getByPlayer(p);
        String s = getScoreboardLines(gp);

        String[] split = s.split("\n");
        List<String> lines = new ArrayList<>();

        for (String str2 : split) {
            lines.add(Utils.format(str2));
        }

        helper.setSlotsFromList(lines);

    }


    private String getScoreboardLines(GamePlayer p) {
        ScoreStringBuilder b = new ScoreStringBuilder(true);

        if (!isState(GameState.WAITING)) {
            int showing = (time % 2);
            if (showing == 1) {
                b.add("&3Jugadores: &f" + getAlivePlayers().size());
            } else {
                b.add("&3Espectadores: &f" + getSpectators().size());
            }
            b.add(" ");
            if (isState(GameState.PLAYING)) {
                b.add("&3Ronda actual:").add("&f" + round).add(" ");
                b.add("&3Tiempo:").add("&f" + getTime());
                b.add(" ");
            }
        } else {
            b.add("&6&lESPERANDO...");
            b.add(" ");
            b.add("&3Jugadores:").add("&f" + Bukkit.getOnlinePlayers().size());
            b.add(" ");
            b.add("&3Modalidad:");
            b.add("&eDeathSwap &bv1.0");
            b.add(" ");
            b.add("&3Discord:");
            b.add("&9discord.gg/4xRFFSDn4R");
            b.add(" ");
        }

        return b.build();
    }

    private String getTime() {
        int hrs = time / 3600;
        int minAndSec = time % 3600;
        int min = minAndSec / 60;
        int sec = minAndSec % 60;

        return (hrs > 9 ? hrs : "0" + hrs) + ":" + (min > 9 ? min : "0" + min) + ":" + (sec > 9 ? sec : "0" + sec);
    }

    public void setOnBreak() {
        this.breakTime = 5;
    }

    public boolean isOnBreak() {
        return breakTime > 0;
    }
}
