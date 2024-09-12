package me.sialim.playercounter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PlayerCounter extends JavaPlugin implements Listener {
    private File logFile;
    private File countFile;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        logFile = new File(getDataFolder(), "first_joins.txt");
        countFile = new File(getDataFolder(), "playercount.txt");

        try
        {
            if (!logFile.exists())
                logFile.createNewFile();

            if (!countFile.exists())
            {
                countFile.createNewFile();
                try (FileWriter writer = new FileWriter(countFile))
                {
                    writer.write("0");
                }
            }
        }
        catch (IOException e)
        {
            getLogger().severe("Could not create log and/or count file!");
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (!e.getPlayer().hasPlayedBefore())
        {
            Player p = e.getPlayer();
            if (!isPlayerLogged(p))
            {
                logPlayer(p, getNextPlayerNumber());
            }
        }
    }

    private boolean isPlayerLogged(Player p)
    {
        String pUUID = p.getUniqueId().toString();
        try
        {
            Path path = Paths.get(logFile.toURI());
            List<String> lines = Files.readAllLines(path);
            for (String line : lines)
            {
                if (line.contains(pUUID))
                    return true;
            }
        }
        catch (IOException e)
        {
            getLogger().severe("Error reading log file!");
        }
        return false;
    }

    private int getNextPlayerNumber()
    {
        try
        {
            Path path = Paths.get(countFile.toURI());
            List<String> lines = Files.readAllLines(path);

            int currentCount = Integer.parseInt(lines.get(0));
            int nextCount = currentCount + 1;

            try (FileWriter writer = new FileWriter(countFile, false))
            {
                writer.write(String.valueOf(nextCount));
            }

            return nextCount;
        }
        catch (IOException | NumberFormatException e)
        {
            getLogger().severe("Error reading or updating player count!");
            return -1;
        }
    }

    private void logPlayer(Player p, int pNumber)
    {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        String formattedDate = now.format(formatter);

        try (FileWriter writer = new FileWriter(logFile, true))
        {
            writer.write(pNumber + ": " + p.getUniqueId().toString() + " (" + p.getDisplayName() + ") at " + formattedDate + "\n");
            getLogger().info("Logged new player " + p.getUniqueId().toString() + " (" + p.getDisplayName() + ") as player #" + pNumber + " at " + formattedDate);
        }
        catch (IOException e)
        {
            getLogger().severe("Error writing to log file!");
        }
    }
}
